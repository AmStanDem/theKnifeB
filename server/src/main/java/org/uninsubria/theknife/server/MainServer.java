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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Punto di ingresso del server TheKnife.
 * Acquisisce il lock di processo, configura il database e il registro RMI,
 * importa il dataset iniziale e mantiene il server attivo fino allo shutdown.
 */
public class MainServer {

    private static final String HOST_DB_PREDEFINITO = "localhost";
    private static final int PORTA_DB_PREDEFINITA = 5432;
    private static final String NOME_DB_PREDEFINITO = "theknife";
    private static final String UTENTE_DB_PREDEFINITO = "admin";
    private static final String PASSWORD_DB_PREDEFINITA = "admin";
    private static final String FILE_BLOCCO_PROCESSO = "theknife-server.lock";

    /**
     * Avvia il server: acquisisce il lock esclusivo, legge la configurazione RMI
     * da {@code .env}, richiede i parametri database, inizializza la persistenza,
     * importa il dataset e pubblica il servizio nel registro RMI.
     * Lo shutdown chiude il registro, il servizio, il pool database e il lock.
     *
     * @param args argomenti della riga di comando, attualmente non utilizzati
     */
    public static void main(String[] args) {
        System.out.println("[SYSTEM] Controllo istanza server in corso...");
        System.out.flush();
        BloccoProcessoServer bloccoProcesso = acquisisciBloccoProcesso();
        if (bloccoProcesso == null) {
            return;
        }

        System.out.println("=================================================");
        System.out.println("      THE KNIFE - CORE SERVER INITIALIZATION     ");
        System.out.println("=================================================\n");

        int portaRmi = 1099;
        int portaDbPredefinita = PORTA_DB_PREDEFINITA;
        File fileEnv = new File(".env");
        if (fileEnv.exists()) {
            try (FileInputStream flussoFile = new FileInputStream(fileEnv)) {
                Properties proprieta = new Properties();
                proprieta.load(flussoFile);
                String proprietaPortaDb = proprieta.getProperty("DB_HOST_PORT");
                if (proprietaPortaDb != null && !proprietaPortaDb.isBlank()) {
                    try {
                        portaDbPredefinita = leggiPortaDatabase(proprietaPortaDb);
                    } catch (IllegalArgumentException e) {
                        System.err.println("[CONFIG_WARN] DB_HOST_PORT non valida; uso la porta "
                                + PORTA_DB_PREDEFINITA + ".");
                    }
                }
                String proprietaPorta = proprieta.getProperty("RMI_PORT");
                if (proprietaPorta != null && !proprietaPorta.isEmpty()) {
                    portaRmi = Integer.parseInt(proprietaPorta.trim());
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
        String hostDb = chiediValore(scanner, console, "[CONFIG] Host DB", HOST_DB_PREDEFINITO);
        int portaDb = chiediPorta(scanner, console, portaDbPredefinita);
        String nomeDb = chiediValore(scanner, console, "[CONFIG] Nome database", NOME_DB_PREDEFINITO);
        String utenteDb = chiediValore(scanner, console, "[CONFIG] Utente PostgreSQL", UTENTE_DB_PREDEFINITO);
        String passwordDb = chiediPassword(scanner, console);
        String urlDb = "jdbc:postgresql://" + hostDb + ":" + portaDb + "/" + nomeDb;

        AtomicReference<Registry> riferimentoRegistro = new AtomicReference<>();
        AtomicReference<TheKnifeServerImpl> riferimentoServer = new AtomicReference<>();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SYSTEM] Chiusura del server e del pool database...");
            annullaEsportazione(riferimentoRegistro.get());
            TheKnifeServerImpl server = riferimentoServer.get();
            annullaEsportazione(server);
            if (server != null) {
                server.close();
            }
            DatabaseConfig.close();
            bloccoProcesso.close();
        }, "theknife-server-shutdown"));

        Registry registro;
        try {
            registro = LocateRegistry.createRegistry(portaRmi);
            riferimentoRegistro.set(registro);
        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Porta RMI " + portaRmi
                    + " non disponibile: " + e.getMessage());
            System.err.println("[SUGGERIMENTO] Arresta l'altra istanza del server oppure libera la porta.");
            System.exit(1);
            return;
        }

        if (hostDb.isBlank() || nomeDb.isBlank() || utenteDb.isBlank()) {
            System.err.println("[FATAL ERROR] Host, nome database e utente PostgreSQL non possono essere vuoti.");
            System.exit(1);
            return;
        }

        // Inizializzazione della persistenza con i parametri inseriti all'avvio.
        try {
            System.out.println("\n[SYSTEM] Tentativo di connessione al database in corso...");
            DatabaseConfig.init(urlDb, utenteDb, passwordDb);
        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Inizializzazione database fallita: " + e.getMessage());
            System.exit(1);
            return;
        }

        org.uninsubria.theknife.server.utils.CsvImporter.importaSeNecessario();

        // Esposizione dei servizi tramite RMI.
        try {
            TheKnifeServerImpl servizioServer = new TheKnifeServerImpl();
            riferimentoServer.set(servizioServer);
            ITheKnifeServer stub = (ITheKnifeServer) UnicastRemoteObject.exportObject(servizioServer, 0);
            registro.rebind("TheKnifeServer", stub);

            System.out.println("[RMI] Registro in ascolto sulla porta " + portaRmi);
            System.out.println("[SYSTEM] Server THE KNIFE operativo e pronto a ricevere connessioni.\n");

        } catch (Exception e) {
            System.err.println("[FATAL ERROR] Impossibile avviare il registro RMI: " + e.getMessage());
            System.exit(1);
        }

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void annullaEsportazione(Remote remoto) {
        if (remoto == null) {
            return;
        }
        try {
            UnicastRemoteObject.unexportObject(remoto, true);
        } catch (java.rmi.NoSuchObjectException ignored) {
            // The object may not have been exported if startup failed partway through.
        }
    }

    private static BloccoProcessoServer acquisisciBloccoProcesso() {
        Path percorsoBlocco = Paths.get(
                System.getProperty("java.io.tmpdir"),
                FILE_BLOCCO_PROCESSO
        );
        try {
            FileChannel canale = FileChannel.open(
                    percorsoBlocco,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.WRITE
            );
            try {
                FileLock lock = canale.tryLock();
                if (lock == null) {
                    canale.close();
                    stampaIstanzaAttiva(percorsoBlocco);
                    return null;
                }
                String infoBlocco = "PID=" + ProcessHandle.current().pid()
                        + System.lineSeparator()
                        + "PORTA_RMI=1099"
                        + System.lineSeparator();
                java.nio.ByteBuffer bufferInfoBlocco = java.nio.ByteBuffer.wrap(
                        infoBlocco.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                while (bufferInfoBlocco.hasRemaining()) {
                    int byteScritti = canale.write(bufferInfoBlocco);
                    if (byteScritti == 0) {
                        throw new IOException("Impossibile scrivere il contenuto del lock.");
                    }
                }
                System.out.println("[SYSTEM] Lock server acquisito: " + percorsoBlocco);
                System.out.flush();
                return new BloccoProcessoServer(canale, lock, percorsoBlocco);
            } catch (OverlappingFileLockException e) {
                canale.close();
                stampaIstanzaAttiva(percorsoBlocco);
                return null;
            } catch (IOException e) {
                canale.close();
                throw e;
            }
        } catch (IOException e) {
            System.err.println("[FATAL ERROR] Impossibile creare il lock del server in "
                    + percorsoBlocco + ": " + e.getMessage());
            System.err.flush();
            return null;
        }
    }

    private static void stampaIstanzaAttiva(Path percorsoBlocco) {
        String messaggio = "[FATAL ERROR] Un'altra istanza del server è già attiva.";
        String suggerimento = "[SUGGERIMENTO] Lock: " + percorsoBlocco
                + ". Usa Ctrl+C sull'istanza attiva; non Ctrl+Z, che sospende il processo.";
        System.out.println(messaggio);
        System.out.println(suggerimento);
        System.out.flush();
        System.err.println(messaggio);
        System.err.println(suggerimento);
        System.err.flush();
    }

    private record BloccoProcessoServer(FileChannel canale, FileLock lock, Path path) {

        public void close() {
                try {
                    if (lock.isValid()) {
                        lock.release();
                    }
                } catch (IOException e) {
                    System.err.println("[CONFIG_WARN] Impossibile rilasciare il lock del server: "
                            + e.getMessage());
                } finally {
                    try {
                        canale.close();
                    } catch (IOException e) {
                        System.err.println("[CONFIG_WARN] Impossibile chiudere il file di lock "
                                + path + ": " + e.getMessage());
                    }
                }
            }
        }

    private static String chiediValore(Scanner scanner, Console console, String etichetta, String valorePredefinito) {
        String messaggio = etichetta + " [" + valorePredefinito + "]: ";
        String valore = console != null
                ? console.readLine(messaggio)
                : chiediDaScanner(scanner, messaggio);
        return valore == null || valore.isBlank() ? valorePredefinito : valore.trim();
    }

    private static int chiediPorta(Scanner scanner, Console console, int portaPredefinita) {
        while (true) {
            String valore = chiediValore(scanner, console, "[CONFIG] Porta PostgreSQL",
                    Integer.toString(portaPredefinita));
            try {
                return leggiPortaDatabase(valore);
            } catch (IllegalArgumentException ignored) {
                // Keep prompting until a valid TCP porta is provided.
            }
            System.out.println("[CONFIG] Porta non valida. Inserisci un valore compreso tra 1 e 65535.");
        }
    }

    private static int leggiPortaDatabase(String valore) {
        int porta;
        try {
            porta = Integer.parseInt(valore.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La porta deve essere numerica.", e);
        }
        if (porta < 1 || porta > 65535) {
            throw new IllegalArgumentException("La porta deve essere compresa tra 1 e 65535.");
        }
        return porta;
    }

    private static String chiediPassword(Scanner scanner, Console console) {
        String messaggio = "[CONFIG] Password PostgreSQL [admin predefinita]: ";
        String valore;
        if (console != null) {
            char[] password = console.readPassword(messaggio);
            valore = password == null ? "" : new String(password);
        } else {
            valore = chiediDaScanner(scanner, messaggio);
        }
        return valore.isBlank() ? PASSWORD_DB_PREDEFINITA : valore;
    }

    private static String chiediDaScanner(Scanner scanner, String messaggioPrompt) {
        System.out.print(messaggioPrompt);
        System.out.flush();
        return scanner.nextLine();
    }
}
