/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.RistoranteDTO;

public class RistoranteSelezionato {

    private static RistoranteDTO ristorante;
    private static boolean ritornoAiMieiRistoranti;

    public static void setRistorante(RistoranteDTO r) {
        ristorante = r;
        ritornoAiMieiRistoranti = false;
    }

    public static RistoranteDTO getRistorante() {
        return ristorante;
    }

    public static void setRitornoAiMieiRistoranti(boolean ritorno) {
        ritornoAiMieiRistoranti = ritorno;
    }

    public static boolean consumaRitornoAiMieiRistoranti() {
        boolean ritorno = ritornoAiMieiRistoranti;
        ritornoAiMieiRistoranti = false;
        return ritorno;
    }
}