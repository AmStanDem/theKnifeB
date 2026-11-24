/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.RecensioneDTO;

/**
 * Conserva temporaneamente la recensione selezionata per la modifica.
 */
public final class RecensioneSelezionata {

    public enum OrigineModifica {
        RISTORANTE,
        MIE_RECENSIONI
    }

    private static RecensioneDTO recensione;
    private static OrigineModifica origineModifica;

    /**
     * Impedisce l'istanziazione della classe di utilità.
     */
    private RecensioneSelezionata() {
    }

    /**
     * Imposta la recensione da modificare.
     *
     * @param recensioneDaModificare recensione selezionata, oppure {@code null}
     */
    public static void imposta(RecensioneDTO recensioneDaModificare) {
        imposta(recensioneDaModificare, OrigineModifica.MIE_RECENSIONI);
    }

    /**
     * Imposta la recensione da modificare e la schermata a cui tornare.
     *
     * @param recensioneDaModificare recensione selezionata
     * @param origine schermata da cui è stata avviata la modifica
     */
    public static void imposta(
            RecensioneDTO recensioneDaModificare,
            OrigineModifica origine
    ) {
        recensione = recensioneDaModificare;
        origineModifica = origine;
    }

    /**
     * Restituisce la recensione attualmente selezionata.
     *
     * @return recensione da modificare oppure {@code null} se non è stata impostata
     */
    public static RecensioneDTO get() {
        return recensione;
    }

    /**
     * Restituisce la schermata da cui è stata avviata la modifica.
     *
     * @return origine della modifica oppure {@code null} se non impostata
     */
    public static OrigineModifica getOrigineModifica() {
        return origineModifica;
    }

    /**
     * Rimuove la recensione attualmente conservata.
     */
    public static void pulisci() {
        recensione = null;
        origineModifica = null;
    }
}
