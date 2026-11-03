package org.uninsubria.clientTK.util;

import org.uninsubria.common.dto.RecensioneDTO;

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
