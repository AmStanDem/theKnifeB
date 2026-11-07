/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.rmi;

import org.uninsubria.theknife.common.dto.DataChangedEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObserver extends Remote {
    void onDataChanged(DataChangedEvent event) throws RemoteException;
}
