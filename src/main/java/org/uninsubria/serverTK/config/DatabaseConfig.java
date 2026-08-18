package org.uninsubria.serverTK.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestore centralizzato per la configurazione e l'accesso al database PostgreSQL.
 * Implementa un pattern Singleton Thread-Safe per l'erogazione delle connessioni JDBC.
 * Gestisce l'allineamento automatico dello schema tramite Flyway.
 */
public class DatabaseConfig {

    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean isInitialized = false;

    // Costruttore privato per prevenire l'instanziazione multipla (Singleton)
    private DatabaseConfig() {
    }

    /**
     * Inizializza l'infrastruttura di persistenza.
     * Viene chiamato dal MainServer durante la fase di bootstrapping.
     *
     * @param url      L'URL JDBC completo (es. jdbc:postgresql://localhost:5433/theknife)
     * @param username L'utente del database
     * @param password La password del database
     */
    public static synchronized void init(String url, String username, String password) {
        if (isInitialized) {
            return;
        }

        jdbcUrl = url;
        dbUser = username;
        dbPassword = password;

        // 1. Fail-Fast: Verifichiamo subito se il database è raggiungibile
        testConnessione();

        // 2. Automazione: Allineiamo lo schema relazionale al codice
        eseguiMigrazioniFlyway();

        isInitialized = true;
        System.out.println("[DB_INFO] DatabaseConfig inizializzato e bloccato con successo.");
    }

    /**
     * Eroga una nuova connessione JDBC isolata.
     * I vari DAO (Data Access Object) richiameranno questo metodo per ogni singola transazione.
     *
     * @return Oggetto Connection attivo e pronto all'uso
     * @throws SQLException Se l'erogazione della connessione fallisce
     */
    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            throw new IllegalStateException("ERRORE DI SISTEMA: Tentativo di accesso al DB prima dell'inizializzazione.");
        }
        // Nelle architetture avanzate, qui si potrebbe restituire una connessione presa da un pool (es. HikariCP).
        // Per mantenere il sistema leggero e nativo, deleghiamo al DriverManager.
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    /**
     * Tenta di aprire e chiudere una connessione. Se fallisce, fa crashare volontariamente
     * il processo d'avvio per non lasciare il server in uno stato inconsistente.
     */
    private static void testConnessione() {
        System.out.println("[DB_INFO] Ping di verifica verso PostgreSQL in corso su: " + jdbcUrl);
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            System.out.println("[DB_INFO] Handshake con PostgreSQL completato. Rete e credenziali valide.");
        } catch (SQLException e) {
            System.err.println("[DB_FATAL] Impossibile stabilire il contatto con PostgreSQL.");
            System.err.println("[DB_FATAL] Dettaglio: " + e.getMessage());
            throw new RuntimeException("Bootstrapping del database fallito", e);
        }
    }

    /**
     * Avvia il motore Flyway per scansionare la cartella resources/db/migration
     * ed eseguire eventuali script SQL mancanti (es. V1__schema_iniziale.sql).
     */
    private static void eseguiMigrazioniFlyway() {
        System.out.println("[DB_INFO] Avvio motore di migrazione Flyway...");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(jdbcUrl, dbUser, dbPassword)
                    // Assicurati che i tuoi script .sql siano in src/main/resources/db/migration
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true) // Crea la tabella di storico se il DB è già pre-esistente
                    .load();

            // Esegue l'aggiornamento
            int migrazioniApplicate = flyway.migrate().migrationsExecuted;
            System.out.println("[DB_INFO] Schema allineato. Migrazioni applicate in questa sessione: " + migrazioniApplicate);

        } catch (FlywayException e) {
            System.err.println("[DB_FATAL] Errore critico durante l'esecuzione degli script SQL di Flyway.");
            System.err.println("[DB_FATAL] Dettaglio: " + e.getMessage());
            throw new RuntimeException("Migrazione schema fallita", e);
        }
    }
}