/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Conserva temporaneamente i criteri della ricerca iniziale da trasferire alla schermata dei risultati.
 */
public final class RicercaIniziale {

    /**
     * Raggio di ricerca predefinito, espresso in chilometri.
     */
    public static final double RAGGIO_RICERCA_KM = 25.0;

    /**
     * Criteri della ricerca iniziale.
     *
     * @param testo testo libero della ricerca
     * @param localita nome della località selezionata
     * @param latitudine latitudine della località selezionata
     * @param longitudine longitudine della località selezionata
     * @param tipologieCucina tipologie di cucina selezionate
     * @param prezzoMin fascia simbolica minima
     * @param prezzoMax fascia simbolica massima
     * @param mediaStelleMinima valutazione media minima
     * @param consegna indica se filtrare per disponibilità della consegna a domicilio
     * @param prenotazioneOnline indica se filtrare per disponibilità della prenotazione online
     */
    public record Criteri(
            String testo,
            String localita,
            Double latitudine,
            Double longitudine,
            List<String> tipologieCucina,
            String prezzoMin,
            String prezzoMax,
            String simboloValuta,
            Double mediaStelleMinima,
            boolean consegna,
            boolean prenotazioneOnline
    ) {
        /**
         * Copia in modo immutabile l'elenco delle tipologie di cucina.
         *
         * @param testo testo libero della ricerca
         * @param localita nome della località selezionata
         * @param latitudine latitudine della località selezionata
         * @param longitudine longitudine della località selezionata
         * @param tipologieCucina tipologie di cucina selezionate, normalizzate a lista vuota se {@code null}
         * @param prezzoMin fascia simbolica minima
         * @param prezzoMax fascia simbolica massima
         * @param mediaStelleMinima valutazione media minima
         * @param consegna indica se filtrare per disponibilità della consegna a domicilio
         * @param prenotazioneOnline indica se filtrare per disponibilità della prenotazione online
         */
        public Criteri {
            tipologieCucina = tipologieCucina == null ? List.of() : List.copyOf(tipologieCucina);
        }
    }

    private static final AtomicReference<Criteri> CRITERI = new AtomicReference<>();

    /**
     * Impedisce l'istanziazione della classe di utilità.
     */
    private RicercaIniziale() {
    }

    /**
     * Memorizza il testo e la località per la ricerca iniziale senza filtri aggiuntivi.
     *
     * @param testo testo libero della ricerca
     * @param luogo località selezionata, oppure {@code null}
     */
    public static void imposta(String testo, GeocodingService.Luogo luogo) {
        imposta(testo, luogo, List.of(), null, null, null, false, false);
    }

    /**
     * Memorizza i criteri completi della ricerca iniziale.
     *
     * @param testo testo libero della ricerca
     * @param luogo località selezionata, oppure {@code null}
     * @param tipologieCucina tipologie di cucina selezionate
     * @param prezzoMin fascia simbolica minima
     * @param prezzoMax fascia simbolica massima
     * @param mediaStelleMinima valutazione media minima
     * @param consegna indica se filtrare per disponibilità della consegna a domicilio
     * @param prenotazioneOnline indica se filtrare per disponibilità della prenotazione online
     */
    public static void imposta(
            String testo,
            GeocodingService.Luogo luogo,
            List<String> tipologieCucina,
            String prezzoMin,
            String prezzoMax,
            Double mediaStelleMinima,
            boolean consegna,
            boolean prenotazioneOnline
    ) {
        String simbolo = primoSimbolo(prezzoMin, prezzoMax);
        imposta(testo, luogo, tipologieCucina, prezzoMin, prezzoMax,
                mediaStelleMinima, consegna, prenotazioneOnline, simbolo);
    }

    public static void imposta(
            String testo,
            GeocodingService.Luogo luogo,
            List<String> tipologieCucina,
            String prezzoMin,
            String prezzoMax,
            Double mediaStelleMinima,
            boolean consegna,
            boolean prenotazioneOnline,
            String simboloValuta
    ) {
        CRITERI.set(new Criteri(
                testo,
                luogo == null ? null : luogo.localita(),
                luogo == null ? null : luogo.latitudine(),
                luogo == null ? null : luogo.longitudine(),
                tipologieCucina,
                prezzoMin,
                prezzoMax,
                simboloValuta,
                mediaStelleMinima,
                consegna,
                prenotazioneOnline
        ));
    }

    private static String primoSimbolo(String fasciaMinima, String fasciaMassima) {
        String fascia = fasciaMinima != null && !fasciaMinima.equals("*")
                ? fasciaMinima : fasciaMassima;
        return fascia == null || fascia.equals("*") ? "¤" : fascia.substring(0, 1);
    }

    /**
     * Restituisce i criteri memorizzati e li rimuove dallo stato condiviso.
     *
     * @return criteri della ricerca iniziale oppure {@code null} se non ne sono stati impostati
     */
    public static Criteri consuma() {
        return CRITERI.getAndSet(null);
    }

    /**
     * Elimina i criteri memorizzati senza restituirli.
     * <p>
     * Va invocato al termine di una sessione per non applicare la ricerca di
     * un utente precedente a un utente successivo.
     * </p>
     */
    public static void pulisci() {
        CRITERI.set(null);
    }
}
