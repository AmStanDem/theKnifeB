/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.rmi.ITheKnifeServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Punto di accesso unico allo stub RMI di {@link ITheKnifeServer}.
 * <p>
 * I controller del client usano {@link #ottieniServer()} invece di accedere
 * direttamente al registro RMI. Lo stub viene recuperato al primo utilizzo e
 * riutilizzato fino a quando la connessione non viene invalidata o riconfigurata.
 *
 * @author TheKnife Team
 */
public final class ServerConnection {

    /** Nome con cui il server espone il servizio remoto nel registro. */
    private static final String NOME_SERVIZIO = "TheKnifeServer";

    private record PuntoAccesso(String host, int porta, ITheKnifeServer server) {}

    private static volatile PuntoAccesso puntoAccesso = new PuntoAccesso("localhost", 1099, null);

    private ServerConnection() {
        // classe di utilità, non istanziabile
    }

    /**
     * Configura host e porta del server a cui connettersi.
     * Deve essere chiamato prima del primo {@link #ottieniServer()}, tipicamente
     * dalla schermata di login/avvio del client.
     *
     * @param nuovoHost host del server RMI
     * @param nuovaPorta porta del server RMI
     * @throws IllegalArgumentException se host o porta non sono validi
     */
    public static void configura(String nuovoHost, int nuovaPorta) {
        if (nuovoHost == null || nuovoHost.isBlank() || nuovaPorta < 1 || nuovaPorta > 65535) {
            throw new IllegalArgumentException("Host e porta RMI non validi.");
        }
        puntoAccesso = new PuntoAccesso(nuovoHost.trim(), nuovaPorta, null);
    }

    /**
     * Invalida lo stub memorizzato, mantenendo la configurazione di host e porta.
     */
    public static void invalidaConnessione() {
        PuntoAccesso corrente = puntoAccesso;
        puntoAccesso = new PuntoAccesso(corrente.host(), corrente.porta(), null);
    }

    /**
     * Restituisce lo stub remoto del server, connettendosi se non è già
     * stata stabilita una connessione attiva.
     *
     * @return lo stub di {@link ITheKnifeServer}
     * @throws RemoteException se la connessione al registro o la lookup falliscono
     * @throws NotBoundException se il servizio non è registrato sul registro indicato
     */
    public static ITheKnifeServer ottieniServer() throws RemoteException, NotBoundException {
        while (true) {
            PuntoAccesso corrente = puntoAccesso;
            if (corrente.server() != null) {
                return corrente.server();
            }

            Registry registro = LocateRegistry.getRegistry(corrente.host(), corrente.porta());
            ITheKnifeServer stub = (ITheKnifeServer) registro.lookup(NOME_SERVIZIO);
            if (puntoAccesso == corrente) {
                puntoAccesso = new PuntoAccesso(corrente.host(), corrente.porta(), stub);
                return stub;
            }
        }
    }
}
