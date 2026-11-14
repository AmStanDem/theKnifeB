/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.mindrot.jbcrypt.BCrypt;
import org.uninsubria.theknife.server.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CsvImporter {
    private static final int BATCH_SIZE = 1_000;

    public static void importaSeNecessario() {
        if (datiGiaPresenti()) {
            int aggiornati = aggiornaPrezziMancanti();
            System.out.printf("[ETL_INFO] Ristoranti già presenti; stimate le fasce prezzo per %d record.%n",
                    aggiornati);
            return;
        }

        System.out.println("[ETL_INFO] Inizio importazione massiva del dataset Michelin con normalizzazione 3NF...");

        try (InputStream is = CsvImporter.class.getResourceAsStream("/dataset/michelin_my_maps_clean.csv")) {
            if (is == null) {
                throw new IllegalStateException(
                        "File CSV non trovato nel classpath: /dataset/michelin_my_maps_clean.csv");
            }

            long startTime = System.nanoTime();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 CSVParser csv = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader);
                 Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false); // Avvio transazione esplicita

                try {
                    creaTabelleTemporanee(conn);
                    int count = caricaStaging(csv, conn);
                    if (count == 0) {
                        throw new SQLException("Il CSV non contiene ristoranti con coordinate valide.");
                    }
                    popolaTabelle(conn, count);
                    conn.commit();
                    double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
                    System.out.printf("[ETL_SUCCESS] Importati %d ristoranti in %.1f secondi.%n",
                            count, elapsedSeconds);
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            System.err.println("[ETL_ERROR] Importazione annullata; rollback eseguito: " + e.getMessage());
            throw new IllegalStateException("Importazione CSV del dataset fallita.", e);
        }
    }

    private static boolean datiGiaPresenti() {
        String sql = "SELECT EXISTS (SELECT 1 FROM RistorantiTheKnife OFFSET 10)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossibile verificare se il dataset è già stato importato.", e);
        }
        return false;
    }

    private static void creaTabelleTemporanee(Connection conn) throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TEMP TABLE tmp_dataset_restaurants (
                        row_no BIGINT PRIMARY KEY,
                        id_ristorante BIGINT,
                        nome TEXT NOT NULL,
                        indirizzo TEXT NOT NULL,
                        citta TEXT NOT NULL,
                        nazione TEXT NOT NULL,
                        latitudine NUMERIC(10, 7) NOT NULL,
                        longitudine NUMERIC(10, 7) NOT NULL,
                        prezzo_medio NUMERIC(6, 2)
                    ) ON COMMIT DROP
                    """);
            stmt.execute("""
                    CREATE TEMP TABLE tmp_dataset_cuisines (
                        row_no BIGINT NOT NULL,
                        nome TEXT NOT NULL,
                        PRIMARY KEY (row_no, nome)
                    ) ON COMMIT DROP
                    """);
        }
    }

    private static int caricaStaging(CSVParser csv, Connection conn) throws Exception {
        String sqlRestaurant = """
                INSERT INTO tmp_dataset_restaurants
                    (row_no, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlCuisine = """
                INSERT INTO tmp_dataset_cuisines (row_no, nome) VALUES (?, ?)
                ON CONFLICT (row_no, nome) DO NOTHING
                """;
        int imported = 0;
        int pendingRestaurants = 0;
        int pendingCuisines = 0;

        try (PreparedStatement restaurants = conn.prepareStatement(sqlRestaurant);
             PreparedStatement cuisines = conn.prepareStatement(sqlCuisine)) {
            for (CSVRecord record : csv) {
                if (record.size() < 7) {
                    continue;
                }
                try {
                    String name = pulisciStringa(record.get(0));
                    String address = pulisciStringa(record.get(1));
                    String location = pulisciStringa(record.get(2));
                    String[] locationParts = location.split(",", 2);
                    String city = locationParts[0].isBlank() ? "Sconosciuta" : locationParts[0].trim();
                    String country = locationParts.length > 1 && !locationParts[1].isBlank()
                            ? locationParts[1].trim()
                            : "Non specificata";
                    String rawCuisines = pulisciStringa(record.get(4));
                    double longitude = Double.parseDouble(pulisciStringa(record.get(5)));
                    double latitude = Double.parseDouble(pulisciStringa(record.get(6)));
                    Double estimatedPrice = PrezzoDatasetEstimator.stimaInEuro(
                            record.get(3), country, name, address);

                    if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                            || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                        continue;
                    }

                    long rowNo = ++imported;
                    restaurants.setLong(1, rowNo);
                    restaurants.setString(2, name.isEmpty() ? "Sconosciuto" : name);
                    restaurants.setString(3, address.isEmpty() ? "Indirizzo non disponibile" : address);
                    restaurants.setString(4, city);
                    restaurants.setString(5, country);
                    restaurants.setDouble(6, latitude);
                    restaurants.setDouble(7, longitude);
                    restaurants.setObject(8, estimatedPrice);
                    restaurants.addBatch();
                    pendingRestaurants++;

                    for (String cuisine : rawCuisines.split(",")) {
                        String cleanCuisine = cuisine.trim();
                        if (!cleanCuisine.isEmpty()) {
                            cuisines.setLong(1, rowNo);
                            cuisines.setString(2, cleanCuisine);
                            cuisines.addBatch();
                            pendingCuisines++;
                        }
                    }

                    if (pendingRestaurants >= BATCH_SIZE) {
                        restaurants.executeBatch();
                        pendingRestaurants = 0;
                    }
                    if (pendingCuisines >= BATCH_SIZE) {
                        cuisines.executeBatch();
                        pendingCuisines = 0;
                    }
                } catch (NumberFormatException e) {
                    // Skip rows without valid coordinates, matching the previous importer behavior.
                }
            }
            if (pendingRestaurants > 0) {
                restaurants.executeBatch();
            }
            if (pendingCuisines > 0) {
                cuisines.executeBatch();
            }
        }

        return imported;
    }

    private static void popolaTabelle(Connection conn, int expectedRestaurants) throws SQLException {
        String passwordHash = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt(12));

        try (PreparedStatement managers = conn.prepareStatement("""
                     INSERT INTO Utenti (email, password, nome, cognome, domicilio, ruolo)
                     SELECT 'dataset.manager.' || MD5(area.nazione || '|' || area.citta)
                                || '@example.invalid',
                            ?, 'Gestore', 'Dataset', area.domicilio, 'gestore'
                     FROM (
                         SELECT LOWER(nazione) AS nazione, LOWER(citta) AS citta, MIN(citta) AS domicilio
                         FROM tmp_dataset_restaurants
                         GROUP BY LOWER(nazione), LOWER(citta)
                     ) area
                     ON CONFLICT (email) DO NOTHING
                     """)) {
            managers.setString(1, passwordHash);
            managers.executeUpdate();
        }

        try (var stmt = conn.createStatement();
             ResultSet invalidManager = stmt.executeQuery("""
                     SELECT r.citta
                     FROM tmp_dataset_restaurants r
                     JOIN Utenti u ON u.email =
                         'dataset.manager.' || MD5(LOWER(r.nazione) || '|' || LOWER(r.citta))
                         || '@example.invalid'
                     WHERE u.ruolo <> 'gestore' OR u.nome <> 'Gestore' OR u.cognome <> 'Dataset'
                     LIMIT 1
                     """)) {
            if (invalidManager.next()) {
                throw new SQLException("Account gestore dataset non valido per la città: "
                        + invalidManager.getString(1));
            }
        }

        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                    UPDATE tmp_dataset_restaurants
                    SET id_ristorante = nextval(
                        pg_get_serial_sequence('ristorantitheknife', 'id_ristorante')
                    )
                    """);
            stmt.executeUpdate("""
                    INSERT INTO Tipologia_Cucina (nome)
                    SELECT DISTINCT nome
                    FROM tmp_dataset_cuisines
                    ON CONFLICT (nome) DO NOTHING
                    """);
            int insertedRestaurants = stmt.executeUpdate("""
                    INSERT INTO RistorantiTheKnife
                        (id_ristorante, id_gestore, nome, indirizzo, citta, nazione,
                         latitudine, longitudine, prezzo_medio, delivery, booking_online, prezzo_stimato)
                    SELECT r.id_ristorante, u.id_utente, r.nome, r.indirizzo, r.citta, r.nazione,
                           r.latitudine, r.longitudine, r.prezzo_medio, FALSE, FALSE,
                           r.prezzo_medio IS NOT NULL
                    FROM tmp_dataset_restaurants r
                    JOIN Utenti u ON u.email =
                        'dataset.manager.' || MD5(LOWER(r.nazione) || '|' || LOWER(r.citta))
                        || '@example.invalid'
                    """);
            if (insertedRestaurants != expectedRestaurants) {
                throw new SQLException("Attesi " + expectedRestaurants + " ristoranti, inseriti "
                        + insertedRestaurants + ".");
            }
            stmt.executeUpdate("""
                    INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia)
                    SELECT DISTINCT r.id_ristorante, c.id_tipologia
                    FROM tmp_dataset_restaurants r
                    JOIN tmp_dataset_cuisines tc ON tc.row_no = r.row_no
                    JOIN Tipologia_Cucina c ON c.nome = tc.nome
                    ON CONFLICT DO NOTHING
                    """);
        }
    }

    private static int aggiornaPrezziMancanti() {
        String sql = """
                UPDATE RistorantiTheKnife r
                SET prezzo_medio = ?, prezzo_stimato = TRUE
                FROM Utenti u
                WHERE r.id_gestore = u.id_utente
                  AND u.email = 'dataset.manager.' || MD5(LOWER(?) || '|' || LOWER(?))
                      || '@example.invalid'
                  AND r.nome = ?
                  AND r.indirizzo = ?
                  AND LOWER(r.citta) = LOWER(?)
                  AND LOWER(r.nazione) = LOWER(?)
                  AND r.prezzo_medio IS NULL
                """;
        int updated = 0;
        int pending = 0;

        try (InputStream is = CsvImporter.class.getResourceAsStream("/dataset/michelin_my_maps_clean.csv")) {
            if (is == null) {
                throw new IllegalStateException("File CSV non trovato: /dataset/michelin_my_maps_clean.csv");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 CSVParser csv = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader);
                 Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement statement = conn.prepareStatement(sql)) {
                for (CSVRecord record : csv) {
                    if (record.size() < 7) {
                        continue;
                    }
                    String name = pulisciStringa(record.get(0));
                    String address = pulisciStringa(record.get(1));
                    String[] locationParts = pulisciStringa(record.get(2)).split(",", 2);
                    String city = locationParts[0].isBlank() ? "Sconosciuta" : locationParts[0].trim();
                    String country = locationParts.length > 1 && !locationParts[1].isBlank()
                            ? locationParts[1].trim() : "Non specificata";
                    Double estimate = PrezzoDatasetEstimator.stimaInEuro(
                            record.get(3), country, name, address);
                    if (estimate == null) {
                        continue;
                    }

                    statement.setDouble(1, estimate);
                    statement.setString(2, country);
                    statement.setString(3, city);
                    statement.setString(4, name);
                    statement.setString(5, address);
                    statement.setString(6, city);
                    statement.setString(7, country);
                    statement.addBatch();
                    if (++pending >= BATCH_SIZE) {
                        updated += contaAggiornamenti(statement.executeBatch());
                        pending = 0;
                    }
                }
                if (pending > 0) {
                    updated += contaAggiornamenti(statement.executeBatch());
                }
            }
            return updated;
        } catch (Exception e) {
            throw new IllegalStateException("Aggiornamento delle stime prezzo del dataset fallito.", e);
        }
    }

    private static int contaAggiornamenti(int[] risultati) {
        int count = 0;
        for (int risultato : risultati) {
            if (risultato > 0) {
                count += risultato;
            } else if (risultato == PreparedStatement.SUCCESS_NO_INFO) {
                count++;
            }
        }
        return count;
    }

    private static String pulisciStringa(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}