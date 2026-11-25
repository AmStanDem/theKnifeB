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
     * @param prezzoMin prezzo minimo
     * @param prezzoMax prezzo massimo
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
            Double prezzoMin,
            Double prezzoMax,
            Double mediaStelleMinima,
            boolean consegna,
            boolean prenotazioneOnline
    ) {
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
     * @param prezzoMin prezzo minimo
     * @param prezzoMax prezzo massimo
     * @param mediaStelleMinima valutazione media minima
     * @param consegna indica se filtrare per disponibilità della consegna a domicilio
     * @param prenotazioneOnline indica se filtrare per disponibilità della prenotazione online
     */
    public static void imposta(
            String testo,
            GeocodingService.Luogo luogo,
            List<String> tipologieCucina,
            Double prezzoMin,
            Double prezzoMax,
            Double mediaStelleMinima,
            boolean consegna,
            boolean prenotazioneOnline
    ) {
        CRITERI.set(new Criteri(
                testo,
                luogo == null ? null : luogo.localita(),
                luogo == null ? null : luogo.latitudine(),
                luogo == null ? null : luogo.longitudine(),
                tipologieCucina,
                prezzoMin,
                prezzoMax,
                mediaStelleMinima,
                consegna,
                prenotazioneOnline
        ));
    }

    /**
     * Restituisce i criteri memorizzati e li rimuove dallo stato condiviso.
     *
     * @return criteri della ricerca iniziale oppure {@code null} se non ne sono stati impostati
     */
    public static Criteri consuma() {
        return CRITERI.getAndSet(null);
    }
}
