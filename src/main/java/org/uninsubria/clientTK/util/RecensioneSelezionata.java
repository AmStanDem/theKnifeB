package org.uninsubria.clientTK.util;

import org.uninsubria.common.dto.RecensioneDTO;

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
