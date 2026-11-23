/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.rmi;

import org.uninsubria.theknife.common.dto.DataChangedEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Osservatore remoto notificato dal server quando i dati sottoscritti cambiano.
 */
public interface RemoteObserver extends Remote {
    /**
     * Riceve la notifica di una modifica ai dati.
     *
     * @param event evento che descrive la modifica avvenuta
     * @throws RemoteException se la comunicazione RMI non è disponibile
     */
    void onDataChanged(DataChangedEvent event) throws RemoteException;
}
