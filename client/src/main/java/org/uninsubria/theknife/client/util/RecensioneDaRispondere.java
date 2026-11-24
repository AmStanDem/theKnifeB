/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.RecensioneDTO;

/**
 * Conserva temporaneamente la recensione selezionata per inserire una risposta.
 */
public final class RecensioneDaRispondere {

    private static RecensioneDTO recensione;

    /**
     * Impedisce l'istanziazione della classe di utilità.
     */
    private RecensioneDaRispondere() {
    }

    /**
     * Imposta la recensione da gestire.
     *
     * @param selezionata recensione selezionata, oppure {@code null} per non selezionarne alcuna
     */
    public static void imposta(RecensioneDTO selezionata) {
        recensione = selezionata;
    }

    /**
     * Restituisce la recensione attualmente selezionata.
     *
     * @return recensione da gestire oppure {@code null} se non è stata impostata
     */
    public static RecensioneDTO get() {
        return recensione;
    }

    /**
     * Rimuove la recensione attualmente conservata.
     */
    public static void pulisci() {
        recensione = null;
    }
}
