package org.uninsubria.serverTK.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestore centralizzato per la configurazione e l'accesso al database PostgreSQL.
 * Pubblica un pool bounded, thread-safe, per le connessioni JDBC.
 * Gestisce l'allineamento automatico dello schema tramite Flyway.
 */
public class DatabaseConfig {

    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 2;
    private static final long CONNECTION_TIMEOUT_MS = 30_000;
    private static final long VALIDATION_TIMEOUT_MS = 5_000;

    private static volatile HikariDataSource dataSource;

    // Costruttore privato per prevenire l'instanziazione multipla (Singleton)
    private DatabaseConfig() {
    }

    /**
     * Inizializza l'infrastruttura di persistenza.
     * Viene chiamato dal MainServer durante la fase di bootstrapping.
     *
     * @param url      L'URL JDBC completo, con la porta pubblicata del database (es. localhost:5432)
     * @param username L'utente del database
     * @param password La password del database
     */
    public static synchronized void init(String url, String username, String password) {
        if (dataSource != null) {
            return;
        }

        if (url == null || url.isBlank() || username == null || username.isBlank() || password == null) {
            throw new IllegalArgumentException("URL, utente e password del database sono obbligatori.");
        }

        eseguiMigrazioniFlyway(url, username, password);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("TheKnife-PostgreSQL");
        config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        config.setMinimumIdle(MINIMUM_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setValidationTimeout(VALIDATION_TIMEOUT_MS);
        config.setInitializationFailTimeout(CONNECTION_TIMEOUT_MS);

        HikariDataSource pool = new HikariDataSource(config);
        try (Connection connection = pool.getConnection()) {
            if (!connection.isValid((int) (VALIDATION_TIMEOUT_MS / 1_000))) {
                throw new SQLException("Il database non ha superato la verifica di validità.");
            }
        } catch (SQLException e) {
            pool.close();
            throw new IllegalStateException("Bootstrapping del pool database fallito.", e);
        }
        dataSource = pool;
        System.out.printf("[DB_INFO] HikariCP pronto (pool massimo: %d connessioni).%n",
                MAXIMUM_POOL_SIZE);
    }

    /**
     * Eroga una nuova connessione JDBC isolata.
     * I vari DAO (Data Access Object) richiameranno questo metodo per ogni singola transazione.
     *
     * @return Oggetto Connection attivo e pronto all'uso
     * @throws SQLException Se l'erogazione della connessione fallisce
     */
    public static Connection getConnection() throws SQLException {
        HikariDataSource pool = dataSource;
        if (pool == null || pool.isClosed()) {
            throw new IllegalStateException("ERRORE DI SISTEMA: Tentativo di accesso al DB prima dell'inizializzazione.");
        }
        return pool.getConnection();
    }

    /**
     * Chiude il pool durante lo shutdown del server.
     */
    public static synchronized void close() {
        HikariDataSource pool = dataSource;
        dataSource = null;
        if (pool != null) {
            pool.close();
        }
    }

    /**
     * Avvia il motore Flyway per scansionare la cartella resources/db/migration
     * ed eseguire eventuali script SQL mancanti (es. V1__schema_iniziale.sql).
     */
    private static void eseguiMigrazioniFlyway(String url, String username, String password) {
        System.out.println("[DB_INFO] Avvio motore di migrazione Flyway...");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
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