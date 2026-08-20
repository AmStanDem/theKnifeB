package org.uninsubria.serverTK.utils;

import org.uninsubria.serverTK.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CsvImporter {

    private static final int BATCH_SIZE = 500; // Ottimizzazione della memoria e del network I/O

    public static void importaSeNecessario() {
        if (datiGiaPresenti()) {
            System.out.println("[ETL_INFO] Ristoranti già presenti nel database. Importazione CSV saltata.");
            return;
        }

        System.out.println("[ETL_INFO] Inizio importazione massiva del dataset Michelin...");

        // Legge il file direttamente dal Classpath (funziona sia su IDE che da JAR compilato)
        try (InputStream is = CsvImporter.class.getResourceAsStream("/dataset/michelin_my_maps.csv")) {

            if (is == null) {
                System.err.println("[ETL_ERROR] File CSV non trovato nel classpath: /dataset/michelin_my_maps.csv");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Connection conn = DatabaseConfig.getConnection()) {

                // Disabilita l'autocommit per gestire il batch in un'unica transazione
                conn.setAutoCommit(false);

                String sql = "INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, tipo_cucina, prezzo_medio, delivery, booking_online) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    String line;
                    boolean isFirstLine = true;
                    int count = 0;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false; // Salta l'intestazione
                            continue;
                        }

                        // Regex per splittare sulle virgole ignorando quelle all'interno delle virgolette ("")
                        String[] colonne = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                        if (colonne.length < 7) continue; // Salta righe malformate

                        try {
                            String nome = pulisciStringa(colonne[0]);
                            String indirizzo = pulisciStringa(colonne[1]);
                            String citta = pulisciStringa(colonne[2]);
                            String tipoCucina = pulisciStringa(colonne[4]);

                            // Estrazione Coordinate
                            double longitudine = Double.parseDouble(pulisciStringa(colonne[5]));
                            double latitudine = Double.parseDouble(pulisciStringa(colonne[6]));

                            stmt.setInt(1, 1); // Assegniamo bulk al gestore con ID 1
                            stmt.setString(2, nome.isEmpty() ? "Sconosciuto" : nome);
                            stmt.setString(3, indirizzo.isEmpty() ? "Indirizzo non disponibile" : indirizzo);
                            stmt.setString(4, citta.isEmpty() ? "Sconosciuta" : citta);
                            stmt.setString(5, "Non specificata"); // Nazione assente nel CSV base
                            stmt.setDouble(6, latitudine);
                            stmt.setDouble(7, longitudine);
                            stmt.setString(8, tipoCucina.isEmpty() ? "Generica" : tipoCucina);
                            stmt.setDouble(9, 100.00); // Prezzo medio di fallback
                            stmt.setBoolean(10, false);
                            stmt.setBoolean(11, false);

                            stmt.addBatch();
                            count++;

                            // Esegue il flush del batch ogni 500 record
                            if (count % BATCH_SIZE == 0) {
                                stmt.executeBatch();
                            }
                        } catch (NumberFormatException e) {
                            // Ignora silenziosamente la singola riga se le coordinate sono sporche/mancanti
                        }
                    }

                    // Esegue i record residui
                    stmt.executeBatch();
                    conn.commit();
                    System.out.println("[ETL_SUCCESS] Importati con successo " + count + " ristoranti.");
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("[ETL_ERROR] Fallimento durante l'inserimento batch. Rollback eseguito. " + e.getMessage());
                } finally {
                    conn.setAutoCommit(true); // Ripristina lo stato del connection pool
                }
            }
        } catch (Exception e) {
            System.err.println("[ETL_ERROR] Errore imprevisto durante l'importazione: " + e.getMessage());
        }
    }

    private static boolean datiGiaPresenti() {
        String sql = "SELECT COUNT(*) FROM RistorantiTheKnife";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Ritorna true se ci sono più dei 4 ristoranti inseriti dal file V2 di Flyway
                return rs.getInt(1) > 10;
            }
        } catch (SQLException e) {
            System.err.println("[ETL_WARN] Impossibile verificare la presenza dei ristoranti: " + e.getMessage());
        }
        return false;
    }

    /**
     * Rimuove i doppi apici (") di escape che il formato CSV aggiunge attorno ai campi contenenti virgole.
     */
    private static String pulisciStringa(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}