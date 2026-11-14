/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

public class RicercaSessione {
    private static String localita = "";
    private static String nomeRistorante = "";

    public static void impostaRicerca(String nuovaLocalita, String nuovoNomeRistorante) {
        localita = nuovaLocalita;
        nomeRistorante = nuovoNomeRistorante;
    }

    public static String getLocalita() {
        return localita;
    }

    public static String getNomeRistorante() {
        return nomeRistorante;
    }
}
