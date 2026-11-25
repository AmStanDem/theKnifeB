/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

/**
 * Conserva i criteri di località e nome del ristorante durante la sessione del client.
 */
public class RicercaSessione {
    private static String localita = "";
    private static String nomeRistorante = "";

    /**
     * Imposta la località e il nome del ristorante della ricerca corrente.
     *
     * @param nuovaLocalita località della ricerca
     * @param nuovoNomeRistorante nome del ristorante cercato
     */
    public static void impostaRicerca(String nuovaLocalita, String nuovoNomeRistorante) {
        localita = nuovaLocalita;
        nomeRistorante = nuovoNomeRistorante;
    }

    /**
     * Restituisce la località della ricerca corrente.
     *
     * @return località memorizzata
     */
    public static String ottieniLocalita() {
        return localita;
    }

    /**
     * Restituisce il nome del ristorante della ricerca corrente.
     *
     * @return nome del ristorante memorizzato
     */
    public static String ottieniNomeRistorante() {
        return nomeRistorante;
    }
}
