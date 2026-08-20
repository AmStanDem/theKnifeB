package org.uninsubria.serverTK;

import org.uninsubria.serverTK.config.DatabaseConfig;
import org.uninsubria.serverTK.rmi.TheKnifeServerImpl;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.Scanner;

/**
 * Punto di ingresso principale (Entry Point) per l'infrastruttura di backend.
 * Supporta l'autofill delle configurazioni tramite file .env o input interattivo.
 */
public class MainServer {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      THE KNIFE - CORE SERVER INITIALIZATION     ");
        System.out.println("=================================================\n");

        String dbUrl = null;
        String dbUser = null;
        String dbPassword = null;
        int rmiPort = 1099; // Porta di default

        // 1. Fase di Bootstrapping: Ricerca automatica del file .env
        File envFile = new File(".env");
        if (envFile.exists()) {
            System.out.println("[CONFIG] File .env rilevato. Caricamento variabili d'ambiente in corso...");
            try (FileInputStream fis = new FileInputStream(envFile)) {
                Properties props = new Properties();
                props.load(fis);

                dbUrl = props.getProperty("DB_URL");
                dbUser = props.getProperty("DB_USER");
                dbPassword = props.getProperty("DB_PASSWORD");

                String portProp = props.getProperty("RMI_PORT");
                if (portProp != null && !portProp.isEmpty()) {
                    rmiPort = Integer.parseInt(portProp);
                }
            } catch (IOException e) {
                System.err.println("[CONFIG_WARN] Impossibile leggere il file .env, fallback manuale.");
            }
        }

        // 2. Fallback Mechanism: Se mancano i dati (file assente o incompleto), chiedi interattivamente
        if (dbUrl == null || dbUser == null || dbPassword == null) {
            System.out.println("[CONFIG] Variabili mancanti. Passaggio all'inserimento manuale...\n");
            Scanner scanner = new Scanner(System.in);

            System.out.print("[CONFIG] Inserisci il DB_URL (es. jdbc:postgresql://localhost:5433/theknife): ");
            dbUrl = scanner.nextLine().trim();

            System.out.print("[CONFIG] Inserisci l'username PostgreSQL: ");
            dbUser = scanner.nextLine().trim();

            Console console = System.console();
            if (console != null) {
                char[] passChars = console.readPassword("[CONFIG] Inserisci la password PostgreSQL: ");
                dbPassword = new String(passChars);
            } else {
                System.out.print("[CONFIG] Inserisci la password PostgreSQL: ");
                dbPassword = scanner.nextLine().trim();
            }
        }

        // 3. Inizializzazione della Persistenza (DatabaseConfig)
        try {
            System.out.println("\n[SYSTEM] Tentativo di connessione al database in corso...");
            // Passiamo direttamente il DB_URL
            DatabaseConfig.init(dbUrl, dbUser, dbPassword);
        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Inizializzazione database fallita: " + e.getMessage());
            System.exit(1);
        }

        org.uninsubria.serverTK.utils.CsvImporter.importaSeNecessario();

        // 4. Esposizione dei Servizi (RMI Registry)
        try {
            TheKnifeServerImpl serverService = new TheKnifeServerImpl();
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind("TheKnifeService", serverService);

            System.out.println("[RMI] Registro creato e in ascolto sulla porta " + rmiPort);
            System.out.println("[SYSTEM] Server THE KNIFE operativo e pronto a ricevere connessioni.\n");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[SYSTEM] Segnale di arresto ricevuto. Chiusura sicura del server...");
            }));

        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Impossibile avviare il registro RMI: " + e.getMessage());
            System.exit(1);
        }
    }
}