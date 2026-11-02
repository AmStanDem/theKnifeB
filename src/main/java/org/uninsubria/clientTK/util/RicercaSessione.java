package org.uninsubria.clientTK.util;

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
