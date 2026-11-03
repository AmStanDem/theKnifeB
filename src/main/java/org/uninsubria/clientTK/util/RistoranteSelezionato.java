package org.uninsubria.clientTK.util;

import org.uninsubria.common.dto.RistoranteDTO;

public class RistoranteSelezionato {

    private static RistoranteDTO ristorante;

    public static void setRistorante(RistoranteDTO r) {
        ristorante = r;
    }

    public static RistoranteDTO getRistorante() {
        return ristorante;
    }
}