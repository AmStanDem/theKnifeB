/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestisce il ciclo di vita delle connessioni al database del server.
 * Applica le migrazioni dello schema con Flyway e mette a disposizione dei DAO
 * un pool JDBC HikariCP condiviso tra i thread.
 */
public class DatabaseConfig {

    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 2;
    private static final long CONNECTION_TIMEOUT_MS = 30_000;
    private static final long VALIDATION_TIMEOUT_MS = 5_000;

    private static volatile HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    /**
     * Applica le migrazioni dello schema, inizializza il pool di connessioni e
     * verifica che una connessione sia valida. Se il pool è già inizializzato,
     * la chiamata non modifica la configurazione esistente.
     *
     * @param url URL JDBC del database
     * @param username nome utente del database
     * @param password password del database
     * @throws IllegalArgumentException se l'URL o il nome utente sono nulli o vuoti
     *                                   oppure la password è {@code null}
     * @throws IllegalStateException se la verifica della connessione del pool fallisce
     * @throws RuntimeException se l'applicazione delle migrazioni Flyway fallisce
     */
    public static synchronized void init(String url, String username, String password) {
        if (dataSource != null) {
            return;
        }

        if (url == null || url.isBlank() || username == null || username.isBlank() || password == null) {
            throw new IllegalArgumentException("URL, utente e password del database sono obbligatori.");
        }

        eseguiMigrazioniFlyway(url, username, password);

        HikariDataSource pool = getPool(url, username, password);
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
     * Crea il pool HikariCP con i parametri di connessione e i limiti configurati.
     *
     * @param url URL JDBC del database
     * @param username nome utente del database
     * @param password password del database
     * @return pool di connessioni configurato
     */
    private static HikariDataSource getPool(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.setPoolName("TheKnife-PostgreSQL");
        config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        config.setMinimumIdle(MINIMUM_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setValidationTimeout(VALIDATION_TIMEOUT_MS);
        config.setInitializationFailTimeout(CONNECTION_TIMEOUT_MS);

        return new HikariDataSource(config);
    }

    /**
     * Preleva una connessione JDBC dal pool condiviso.
     * Il chiamante è responsabile di chiuderla, normalmente tramite
     * {@code try-with-resources}, per restituirla al pool.
     *
     * @return connessione JDBC presa in prestito dal pool
     * @throws IllegalStateException se il pool non è stato inizializzato o è già chiuso
     * @throws SQLException se non è possibile ottenere una connessione
     */
    public static Connection getConnection() throws SQLException {
        HikariDataSource pool = dataSource;
        if (pool == null || pool.isClosed()) {
            throw new IllegalStateException("ERRORE DI SISTEMA: Tentativo di accesso al DB prima dell'inizializzazione.");
        }
        return pool.getConnection();
    }

    /**
     * Chiude il pool di connessioni, se inizializzato, e azzera il riferimento condiviso.
     * Dopo la chiusura è necessario chiamare {@link #init(String, String, String)}
     * prima di richiedere nuove connessioni.
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
                    // Flyway legge le migrazioni dal classpath del modulo server.
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