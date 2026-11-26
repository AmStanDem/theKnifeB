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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Importa e aggiorna nel database il dataset dei ristoranti Michelin incluso
 * nelle risorse dell'applicazione.
 */
public class CsvImporter {
    private static final int BATCH_SIZE = 1_000;

    /**
     * Paese associato alle città-stato che nel dataset compaiono senza il
     * secondo componente nella colonna {@code Location}.
     */
    private static final Map<String, String> CITTA_STATO = Map.of(
            "singapore", "Singapore",
            "hong kong", "Hong Kong SAR China",
            "dubai", "United Arab Emirates",
            "abu dhabi", "United Arab Emirates",
            "macau", "Macau SAR China",
            "luxembourg", "Luxembourg");

    /**
     * Etichette di paese incoerenti presenti nel dataset da normalizzare.
     */
    private static final Map<String, String> ALIAS_PAESE = Map.of(
            "czech republic", "Czechia");

    /**
     * Importa il dataset se non sono già presenti ristoranti e, in caso contrario,
     * aggiorna i prezzi mancanti, i dati di contatto e la distribuzione dei servizi
     * per i ristoranti del dataset.
     *
     * @throws IllegalStateException se il file CSV non è disponibile o se
     *         l'importazione o l'aggiornamento dei dati fallisce
     */
    public static void importaSeNecessario() {
        long importStartTime = System.nanoTime();
        if (datiGiaPresenti()) {
            int aggiornati = aggiornaPrezziMancanti();
            int contattiAggiornati = aggiornaContattiDataset();
            int serviziAggiornati = distribuisciServiziDataset();
            double elapsedSeconds = (System.nanoTime() - importStartTime) / 1_000_000_000.0;
            System.out.printf("[ETL_INFO] Ristoranti già presenti; aggiornati prezzi per %d record, dati dataset per %d record e servizi per %d record in %.1f secondi.%n",
                    aggiornati, contattiAggiornati, serviziAggiornati, elapsedSeconds);
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
                        prezzo_medio NUMERIC(6, 2),
                        telefono TEXT,
                        sito_web TEXT,
                        descrizione TEXT
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
                    (row_no, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio,
                     telefono, sito_web, descrizione)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                    String[] localita = analizzaLocalita(record.get(2));
                    String city = localita[0];
                    String country = localita[1];
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
                    restaurants.setString(9, normalizzaTelefono(valoreDataset(record, 7)));
                    restaurants.setString(10, normalizzaUrl(valoreDataset(record, 9)));
                    restaurants.setString(11, valoreDataset(record, 13));
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
                         latitudine, longitudine, prezzo_medio, delivery, booking_online, prezzo_stimato,
                         telefono, sito_web, descrizione)
                    SELECT r.id_ristorante, u.id_utente, r.nome, r.indirizzo, r.citta, r.nazione,
                           r.latitudine, r.longitudine, r.prezzo_medio,
                           r.row_no % 2 = 0, (r.row_no / 2) % 2 = 0,
                           r.prezzo_medio IS NOT NULL, r.telefono, r.sito_web, r.descrizione
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

    private static int distribuisciServiziDataset() {
        String sql = """
                WITH dataset AS (
                    SELECT r.id_ristorante,
                           ROW_NUMBER() OVER (ORDER BY r.id_ristorante) AS posizione
                    FROM RistorantiTheKnife r
                    JOIN Utenti u ON u.id_utente = r.id_gestore
                    WHERE u.email LIKE 'dataset.manager.%@example.invalid'
                )
                UPDATE RistorantiTheKnife r
                SET delivery = dataset.posizione % 2 = 0,
                    booking_online = (dataset.posizione / 2) % 2 = 0
                FROM dataset
                WHERE r.id_ristorante = dataset.id_ristorante
                  AND (
                      r.delivery IS DISTINCT FROM (dataset.posizione % 2 = 0)
                      OR r.booking_online IS DISTINCT FROM ((dataset.posizione / 2) % 2 = 0)
                  )
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossibile distribuire i servizi del dataset.", e);
        }
    }

    private static String valoreDataset(CSVRecord record, int indice) {
        return indice < record.size() ? pulisciStringa(record.get(indice)) : null;
    }

    private static int aggiornaContattiDataset() {
        String sql = """
                UPDATE RistorantiTheKnife r
                SET telefono = ?, sito_web = ?, descrizione = ?
                WHERE r.nome = ? AND r.indirizzo = ?
                  AND LOWER(r.citta) = LOWER(?) AND LOWER(r.nazione) = LOWER(?)
                  AND ROUND(r.longitudine, 7) = ROUND(?::numeric, 7)
                  AND ROUND(r.latitudine, 7) = ROUND(?::numeric, 7)
                  AND (r.telefono IS DISTINCT FROM ?
                       OR r.sito_web IS DISTINCT FROM ?
                       OR r.descrizione IS DISTINCT FROM ?)
                """;
        int aggiornati = 0;
        int pending = 0;
        try (InputStream is = CsvImporter.class.getResourceAsStream("/dataset/michelin_my_maps_clean.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csv = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader);
             Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CSVRecord record : csv) {
                if (record.size() < 10) {
                    continue;
                }
                String[] localita = analizzaLocalita(record.get(2));
                String city = localita[0];
                String country = localita[1];
                stmt.setString(1, normalizzaTelefono(valoreDataset(record, 7)));
                stmt.setString(2, normalizzaUrl(valoreDataset(record, 9)));
                stmt.setString(3, valoreDataset(record, 13));
                stmt.setString(4, pulisciStringa(record.get(0)));
                stmt.setString(5, pulisciStringa(record.get(1)));
                stmt.setString(6, city);
                stmt.setString(7, country);
                stmt.setString(8, pulisciStringa(record.get(5)));
                stmt.setString(9, pulisciStringa(record.get(6)));
                stmt.setString(10, normalizzaTelefono(valoreDataset(record, 7)));
                stmt.setString(11, normalizzaUrl(valoreDataset(record, 9)));
                stmt.setString(12, valoreDataset(record, 13));
                stmt.addBatch();

                if (++pending >= BATCH_SIZE) {
                    aggiornati += contaAggiornamenti(stmt.executeBatch());
                    pending = 0;
                }
            }
            if (pending > 0) {
                aggiornati += contaAggiornamenti(stmt.executeBatch());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile aggiornare i contatti dal dataset.", e);
        }
        return aggiornati;
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

    /**
     * Scompone la colonna {@code Location} in città e paese, normalizzando le
     * etichette di paese e assegnando il paese alle città-stato che nel dataset
     * compaiono senza il secondo componente.
     *
     * @param location valore grezzo della colonna {@code Location}
     * @return array di due elementi: città e paese (mai {@code null})
     */
    private static String[] analizzaLocalita(String location) {
        String pulita = pulisciStringa(location);
        int separatore = pulita.indexOf(',');
        if (separatore < 0) {
            String paese = CITTA_STATO.get(pulita.toLowerCase(Locale.ROOT));
            if (paese != null) {
                return new String[] {pulita, paese};
            }
            return new String[] {
                    pulita.isBlank() ? "Sconosciuta" : pulita,
                    "Non specificata"};
        }
        String citta = pulita.substring(0, separatore).trim();
        String paese = pulita.substring(separatore + 1).trim();
        paese = ALIAS_PAESE.getOrDefault(paese.toLowerCase(Locale.ROOT), paese);
        return new String[] {
                citta.isBlank() ? "Sconosciuta" : citta,
                paese.isBlank() ? "Non specificata" : paese};
    }

    /**
     * Normalizza un numero di telefono. I valori in notazione scientifica
     * (effetto dell'apertura del CSV in Excel) vengono espansi in cifre; gli
     * altri valori vengono ripuliti da separatori e prefissi mantenendo le sole
     * cifre.
     *
     * @param valore valore grezzo del numero di telefono
     * @return numero di telefono con le sole cifre, oppure {@code null} se assente
     */
    private static String normalizzaTelefono(String valore) {
        String pulito = pulisciStringa(valore);
        if (pulito.isEmpty()) {
            return null;
        }
        if (pulito.matches("[+-]?\\d+(\\.\\d+)?[eE][+-]?\\d+")) {
            try {
                return new BigDecimal(pulito).toBigIntegerExact().toString();
            } catch (ArithmeticException e) {
                // Valore non intero: si ripiega sulla sola estrazione delle cifre.
            }
        }
        return pulito.replaceAll("\\D", "");
    }

    /**
     * Normalizza l'URL del sito web correggendo gli schemi malformati
     * (``httpss://``, ``http://httpss//``, schema mancante).
     *
     * @param valore valore grezzo dell'URL
     * @return URL con schema valido, oppure {@code null} se assente
     */
    private static String normalizzaUrl(String valore) {
        String url = pulisciStringa(valore);
        if (url.isEmpty()) {
            return null;
        }
        String[][] schemiMalformati = {
                {"http://httpss//", "https://"},
                {"https://httpss//", "https://"},
                {"httpss://", "https://"},
                {"httpss//", "https://"},
                {"https//", "https://"},
                {"http//", "http://"},
        };
        for (String[] schema : schemiMalformati) {
            String prefisso = schema[0];
            if (url.regionMatches(true, 0, prefisso, 0, prefisso.length())) {
                url = schema[1] + url.substring(prefisso.length());
                break;
            }
        }
        if (!url.regionMatches(true, 0, "http://", 0, 7)
                && !url.regionMatches(true, 0, "https://", 0, 8)) {
            url = "https://" + url;
        }
        return url;
    }
}