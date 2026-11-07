/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.RecensioneDTO;

public final class RecensioneDaRispondere {

    private static RecensioneDTO recensione;

    private RecensioneDaRispondere() {
    }

    public static void imposta(RecensioneDTO selezionata) {
        recensione = selezionata;
    }

    public static RecensioneDTO get() {
        return recensione;
    }

    public static void pulisci() {
        recensione = null;
    }
}
