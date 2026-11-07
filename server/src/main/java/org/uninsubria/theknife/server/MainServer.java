/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server;

import org.uninsubria.theknife.server.config.DatabaseConfig;
import org.uninsubria.theknife.server.rmi.TheKnifeServerImpl;
import org.uninsubria.theknife.common.rmi.ITheKnifeServer;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Punto di ingresso principale (Entry Point) per l'infrastruttura di backend.
 * Richiede all'avvio i parametri del database, proponendo valori predefiniti.
 */
public class MainServer {

    private static final String DEFAULT_DB_HOST = "localhost";
    private static final int DEFAULT_DB_PORT = 5432;
    private static final String DEFAULT_DB_NAME = "theknife";
    private static final String DEFAULT_DB_USER = "admin";
    private static final String DEFAULT_DB_PASSWORD = "admin";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      THE KNIFE - CORE SERVER INITIALIZATION     ");
        System.out.println("=================================================\n");

        int rmiPort = 1099;
        int defaultDbPort = DEFAULT_DB_PORT;
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (FileInputStream fis = new FileInputStream(envFile)) {
                Properties props = new Properties();
                props.load(fis);
                String dbPortProp = props.getProperty("DB_HOST_PORT");
                if (dbPortProp != null && !dbPortProp.isBlank()) {
                    try {
                        defaultDbPort = parseDbPort(dbPortProp);
                    } catch (IllegalArgumentException e) {
                        System.err.println("[CONFIG_WARN] DB_HOST_PORT non valida; uso la porta "
                                + DEFAULT_DB_PORT + ".");
                    }
                }
                String portProp = props.getProperty("RMI_PORT");
                if (portProp != null && !portProp.isEmpty()) {
                    rmiPort = Integer.parseInt(portProp.trim());
                }
            } catch (IOException e) {
                System.err.println("[CONFIG_WARN] Impossibile leggere RMI_PORT da .env; uso la porta 1099.");
            } catch (NumberFormatException e) {
                System.err.println("[CONFIG_WARN] RMI_PORT non valida; uso la porta 1099.");
            }
        }

        System.out.println("[CONFIG] Configurazione del database (premi Invio per usare il valore predefinito).");
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);
        String dbHost = prompt(scanner, console, "[CONFIG] Host DB", DEFAULT_DB_HOST);
        int dbPort = promptPort(scanner, console, defaultDbPort);
        String dbName = prompt(scanner, console, "[CONFIG] Nome database", DEFAULT_DB_NAME);
        String dbUser = prompt(scanner, console, "[CONFIG] Utente PostgreSQL", DEFAULT_DB_USER);
        String dbPassword = promptPassword(scanner, console);
        String dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        if (dbHost.isBlank() || dbName.isBlank() || dbUser.isBlank()) {
            System.err.println("[FATAL ERROR] Host, nome database e utente PostgreSQL non possono essere vuoti.");
            System.exit(1);
            return;
        }

        // Inizializzazione della persistenza con i parametri inseriti all'avvio.
        try {
            System.out.println("\n[SYSTEM] Tentativo di connessione al database in corso...");
            DatabaseConfig.init(dbUrl, dbUser, dbPassword);
        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Inizializzazione database fallita: " + e.getMessage());
            System.exit(1);
            return;
        }

        AtomicReference<Registry> registryRef = new AtomicReference<>();
        AtomicReference<TheKnifeServerImpl> serverRef = new AtomicReference<>();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SYSTEM] Chiusura del server e del pool database...");
            unexport(registryRef.get());
            unexport(serverRef.get());
            DatabaseConfig.close();
        }, "theknife-server-shutdown"));

        org.uninsubria.theknife.server.utils.CsvImporter.importaSeNecessario();

        // Esposizione dei servizi tramite RMI.
        try {
            TheKnifeServerImpl serverService = new TheKnifeServerImpl();
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registryRef.set(registry);
            ITheKnifeServer stub = (ITheKnifeServer) UnicastRemoteObject.exportObject(serverService, 0);
            serverRef.set(serverService);
            registry.rebind("TheKnifeServer", stub);

            System.out.println("[RMI] Registro creato e in ascolto sulla porta " + rmiPort);
            System.out.println("[SYSTEM] Server THE KNIFE operativo e pronto a ricevere connessioni.\n");

        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Impossibile avviare il registro RMI: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void unexport(Remote remote) {
        if (remote == null) {
            return;
        }
        try {
            UnicastRemoteObject.unexportObject(remote, true);
        } catch (java.rmi.NoSuchObjectException ignored) {
            // The object may not have been exported if startup failed partway through.
        }
    }

    private static String prompt(Scanner scanner, Console console, String label, String defaultValue) {
        String prompt = label + " [" + defaultValue + "]: ";
        String value = console != null
                ? console.readLine(prompt)
                : promptFromScanner(scanner, prompt);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int promptPort(Scanner scanner, Console console, int defaultPort) {
        while (true) {
            String value = prompt(scanner, console, "[CONFIG] Porta PostgreSQL",
                    Integer.toString(defaultPort));
            try {
                return parseDbPort(value);
            } catch (IllegalArgumentException ignored) {
                // Keep prompting until a valid TCP port is provided.
            }
            System.out.println("[CONFIG] Porta non valida. Inserisci un valore compreso tra 1 e 65535.");
        }
    }

    private static int parseDbPort(String value) {
        int port;
        try {
            port = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La porta deve essere numerica.", e);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("La porta deve essere compresa tra 1 e 65535.");
        }
        return port;
    }

    private static String promptPassword(Scanner scanner, Console console) {
        String prompt = "[CONFIG] Password PostgreSQL [admin predefinita]: ";
        String value;
        if (console != null) {
            char[] password = console.readPassword(prompt);
            value = password == null ? "" : new String(password);
        } else {
            value = promptFromScanner(scanner, prompt);
        }
        return value.isBlank() ? DEFAULT_DB_PASSWORD : value;
    }

    private static String promptFromScanner(Scanner scanner, String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return scanner.nextLine();
    }
}
