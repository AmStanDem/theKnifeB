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
import java.sql.Statement;

public class CsvImporter {

    private static final int BATCH_SIZE = 500; // Ottimizzazione della memoria I/O

    public static void importaSeNecessario() {
        if (datiGiaPresenti()) {
            System.out.println("[ETL_INFO] Ristoranti già presenti nel database. Importazione CSV saltata.");
            return;
        }

        System.out.println("[ETL_INFO] Inizio importazione massiva del dataset Michelin con normalizzazione 3NF...");

        try (InputStream is = CsvImporter.class.getResourceAsStream("/dataset/michelin_my_maps.csv")) {

            if (is == null) {
                System.err.println("[ETL_ERROR] File CSV non trovato nel classpath: /dataset/michelin_my_maps.csv");
                return;
            }

            String sqlRistorante = "INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio, delivery, booking_online) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlUpsertCucina = "INSERT INTO Tipologia_Cucina (nome) VALUES (?) ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome RETURNING id_tipologia";
            String sqlPonte = "INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) VALUES (?, ?) ON CONFLICT DO NOTHING";

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Connection conn = DatabaseConfig.getConnection()) {

                conn.setAutoCommit(false); // Avvio transazione esplicita

                try (PreparedStatement stmtRest = conn.prepareStatement(sqlRistorante, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement stmtCucina = conn.prepareStatement(sqlUpsertCucina);
                     PreparedStatement stmtPonte = conn.prepareStatement(sqlPonte)) {

                    String line;
                    boolean isFirstLine = true;
                    int count = 0;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        // Regex per splittare sulle virgole ignorando quelle all'interno delle virgolette ("")
                        String[] colonne = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                        if (colonne.length < 7) continue;

                        try {
                            String nome = pulisciStringa(colonne[0]);
                            String indirizzo = pulisciStringa(colonne[1]);
                            String citta = pulisciStringa(colonne[2]);
                            String cucineRaw = pulisciStringa(colonne[4]);
                            double longitudine = Double.parseDouble(pulisciStringa(colonne[5]));
                            double latitudine = Double.parseDouble(pulisciStringa(colonne[6]));

                            // 1. Inserimento Ristorante
                            stmtRest.setInt(1, 1); // Assegnazione al gestore fittizio con ID 1
                            stmtRest.setString(2, nome.isEmpty() ? "Sconosciuto" : nome);
                            stmtRest.setString(3, indirizzo.isEmpty() ? "Indirizzo non disponibile" : indirizzo);
                            stmtRest.setString(4, citta.isEmpty() ? "Sconosciuta" : citta);
                            stmtRest.setString(5, "Non specificata"); // Il dataset non prevede nazione
                            stmtRest.setDouble(6, latitudine);
                            stmtRest.setDouble(7, longitudine);
                            stmtRest.setDouble(8, 100.00); // Prezzo medio di fallback
                            stmtRest.setBoolean(9, false);
                            stmtRest.setBoolean(10, false);

                            stmtRest.executeUpdate();

                            // 2. Recupero ID Ristorante generato
                            int idRistorante;
                            try (ResultSet rsKeys = stmtRest.getGeneratedKeys()) {
                                if (rsKeys.next()) {
                                    idRistorante = rsKeys.getInt(1);
                                } else {
                                    continue;
                                }
                            }

                            // 3. Inserimento Cucine multiple e Tabella Ponte
                            if (!cucineRaw.isEmpty()) {
                                String[] tipologie = cucineRaw.split(",");
                                for (String tipo : tipologie) {
                                    String cleanTipo = tipo.trim();
                                    if (cleanTipo.isEmpty()) continue;

                                    // Upsert Cucina
                                    stmtCucina.setString(1, cleanTipo);
                                    int idCucina;
                                    try (ResultSet rsCucina = stmtCucina.executeQuery()) {
                                        if (rsCucina.next()) {
                                            idCucina = rsCucina.getInt(1);
                                        } else {
                                            continue;
                                        }
                                    }

                                    // Collegamento
                                    stmtPonte.setInt(1, idRistorante);
                                    stmtPonte.setInt(2, idCucina);
                                    stmtPonte.executeUpdate();
                                }
                            }

                            count++;

                            // Flush a blocchi per liberare memoria ed evitare transazioni infinite
                            if (count % BATCH_SIZE == 0) {
                                conn.commit();
                            }

                        } catch (NumberFormatException e) {
                            // Si ignora il singolo record se le coordinate sono assenti o corrotte nel CSV
                        }
                    }

                    conn.commit(); // Commit dei record residui
                    System.out.println("[ETL_SUCCESS] Importati con successo " + count + " ristoranti con cucina N:M normalizzata.");

                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("[ETL_ERROR] Fallimento durante l'inserimento. Rollback eseguito. " + e.getMessage());
                } finally {
                    conn.setAutoCommit(true);
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

    private static String pulisciStringa(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}