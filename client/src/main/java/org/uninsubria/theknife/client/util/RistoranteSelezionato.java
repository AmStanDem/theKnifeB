/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.RistoranteDTO;

/**
 * Conserva il ristorante selezionato e lo stato di ritorno alla schermata dei ristoranti gestiti.
 */
public class RistoranteSelezionato {

    private static RistoranteDTO ristorante;
    private static boolean ritornoAiMieiRistoranti;

    /**
     * Imposta il ristorante selezionato e azzera l'indicatore di ritorno ai ristoranti gestiti.
     *
     * @param r ristorante selezionato, oppure {@code null}
     */
    public static void setRistorante(RistoranteDTO r) {
        ristorante = r;
        ritornoAiMieiRistoranti = false;
    }

    /**
     * Restituisce il ristorante attualmente selezionato.
     *
     * @return ristorante selezionato oppure {@code null} se non impostato
     */
    public static RistoranteDTO getRistorante() {
        return ristorante;
    }

    /**
     * Imposta se, dopo la schermata corrente, si deve tornare ai ristoranti gestiti.
     *
     * @param ritorno {@code true} per indicare il ritorno ai ristoranti gestiti
     */
    public static void setRitornoAiMieiRistoranti(boolean ritorno) {
        ritornoAiMieiRistoranti = ritorno;
    }

    /**
     * Restituisce e azzera l'indicatore di ritorno ai ristoranti gestiti.
     *
     * @return {@code true} se era richiesto il ritorno ai ristoranti gestiti
     */
    public static boolean consumaRitornoAiMieiRistoranti() {
        boolean ritorno = ritornoAiMieiRistoranti;
        ritornoAiMieiRistoranti = false;
        return ritorno;
    }
}