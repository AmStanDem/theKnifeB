package org.uninsubria.clientTK.util;

import org.uninsubria.common.rmi.ITheKnifeServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Punto di accesso unico allo stub RMI di {@link ITheKnifeServer}.
 * <p>
 * I org.uninsubria.menuprova.controller non devono mai creare direttamente il registry RMI: chiamano
 * {@link #getServer()}, che si occupa di stabilire la connessione al primo
 * utilizzo e di riutilizzarla per tutta la sessione del client.
 *
 * @author TheKnife Team
 */
public final class ServerConnection {

    /** Nome con cui il serverTK espone il servizio remoto nel registry. */
    private static final String SERVICE_NAME = "TheKnifeServer";

    private record Endpoint(String host, int port, ITheKnifeServer server) {}

    private static volatile Endpoint endpoint = new Endpoint("localhost", 1099, null);

    private ServerConnection() {
        // classe di utilità, non istanziabile
    }

    /**
     * Configura host e porta del serverTK a cui connettersi.
     * Deve essere chiamato prima del primo {@link #getServer()}, tipicamente
     * dalla schermata di login/avvio del client.
     *
     * @param nuovoHost host del server RMI
     * @param nuovaPorta porta del server RMI
     */
    public static void configura(String nuovoHost, int nuovaPorta) {
        if (nuovoHost == null || nuovoHost.isBlank() || nuovaPorta < 1 || nuovaPorta > 65535) {
            throw new IllegalArgumentException("Host e porta RMI non validi.");
        }
        endpoint = new Endpoint(nuovoHost.trim(), nuovaPorta, null);
    }

    /**
     * Restituisce lo stub remoto del server, connettendosi se non è già
     * stata stabilita una connessione attiva.
     *
     * @return lo stub di {@link ITheKnifeServer}
     * @throws RemoteException se la connessione al registry o la lookup falliscono
     * @throws NotBoundException se il servizio non è registrato sul registry indicato
     */
    public static ITheKnifeServer getServer() throws RemoteException, NotBoundException {
        while (true) {
            Endpoint current = endpoint;
            if (current.server() != null) {
                return current.server();
            }

            Registry registry = LocateRegistry.getRegistry(current.host(), current.port());
            ITheKnifeServer stub = (ITheKnifeServer) registry.lookup(SERVICE_NAME);
            if (endpoint == current) {
                endpoint = new Endpoint(current.host(), current.port(), stub);
                return stub;
            }
        }
    }
}
