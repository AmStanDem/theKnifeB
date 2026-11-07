/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.RecensioneDTO;

public final class RecensioneSelezionata {

    private static RecensioneDTO recensione;

    private RecensioneSelezionata() {
    }

    public static void imposta(RecensioneDTO recensioneDaModificare) {
        recensione = recensioneDaModificare;
    }

    public static RecensioneDTO get() {
        return recensione;
    }

    public static void pulisci() {
        recensione = null;
    }
}
