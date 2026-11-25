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
    public static void impostaRistorante(RistoranteDTO r) {
        ristorante = r;
        ritornoAiMieiRistoranti = false;
    }

    /**
     * Restituisce il ristorante attualmente selezionato.
     *
     * @return ristorante selezionato oppure {@code null} se non impostato
     */
    public static RistoranteDTO ottieniRistorante() {
        return ristorante;
    }

    /**
     * Imposta se, dopo la schermata corrente, si deve tornare ai ristoranti gestiti.
     *
     * @param ritorno {@code true} per indicare il ritorno ai ristoranti gestiti
     */
    public static void impostaRitornoAiMieiRistoranti(boolean ritorno) {
        ritornoAiMieiRistoranti = ritorno;
    }

    /**
     * Indica se, dopo la schermata corrente, si deve tornare ai ristoranti gestiti.
     * <p>
     * Il valore resta attivo finché non viene selezionato un nuovo ristorante
     * tramite {@link #impostaRistorante(RistoranteDTO)}: in questo modo il pulsante
     * di ritorno resta visibile anche quando si rientra nella scheda del
     * ristorante utilizzando il pulsante "indietro".
     * </p>
     *
     * @return {@code true} se è richiesto il ritorno ai ristoranti gestiti
     */
    public static boolean deveTornareAiMieiRistoranti() {
        return ritornoAiMieiRistoranti;
    }
}