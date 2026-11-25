/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Rappresentazione dei dati principali di un ristorante.
 *
 * @param idRistorante identificativo univoco del ristorante
 * @param nome nome del ristorante
 * @param indirizzo indirizzo fisico del ristorante
 * @param citta città in cui si trova il ristorante
 * @param nazione nazione in cui si trova il ristorante
 * @param latitudine latitudine della posizione del ristorante
 * @param longitudine longitudine della posizione del ristorante
 * @param tipologieCucina tipologie di cucina offerte
 * @param prezzoMedio prezzo medio indicativo
 * @param consegna indica se è disponibile la consegna a domicilio
 * @param prenotazioneOnline indica se è disponibile la prenotazione online
 * @param mediaStelle valutazione media del ristorante
 * @param prezzoStimato indica se il prezzo medio è stato stimato
 * @param telefono numero di telefono del ristorante, se disponibile
 * @param sitoWeb sito web del ristorante, se disponibile
 * @param descrizione descrizione del ristorante, se disponibile
 */
public record RistoranteDTO(
        Integer idRistorante,
        String nome,
        String indirizzo,
        String citta,
        String nazione,
        Double latitudine,
        Double longitudine,
        List<String> tipologieCucina,
        Double prezzoMedio,
        Boolean consegna,
        Boolean prenotazioneOnline,
        Double mediaStelle,
        boolean prezzoStimato,
        String telefono,
        String sitoWeb,
        String descrizione
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Crea un ristorante senza informazioni aggiuntive di contatto o descrizione.
     *
     * @param idRistorante identificativo univoco del ristorante
     * @param nome nome del ristorante
     * @param indirizzo indirizzo fisico del ristorante
     * @param citta città in cui si trova il ristorante
     * @param nazione nazione in cui si trova il ristorante
     * @param latitudine latitudine della posizione del ristorante
     * @param longitudine longitudine della posizione del ristorante
     * @param tipologieCucina tipologie di cucina offerte
     * @param prezzoMedio prezzo medio indicativo
     * @param consegna indica se è disponibile la consegna a domicilio
     * @param prenotazioneOnline indica se è disponibile la prenotazione online
     * @param mediaStelle valutazione media del ristorante
     */
    public RistoranteDTO(
            Integer idRistorante,
            String nome,
            String indirizzo,
            String citta,
            String nazione,
            Double latitudine,
            Double longitudine,
            List<String> tipologieCucina,
            Double prezzoMedio,
            Boolean consegna,
            Boolean prenotazioneOnline,
            Double mediaStelle
    ) {
        this(idRistorante, nome, indirizzo, citta, nazione, latitudine, longitudine,
                tipologieCucina, prezzoMedio, consegna, prenotazioneOnline, mediaStelle, false);
    }

    /**
     * Crea un ristorante specificando se il prezzo medio è stimato.
     *
     * @param idRistorante identificativo univoco del ristorante
     * @param nome nome del ristorante
     * @param indirizzo indirizzo fisico del ristorante
     * @param citta città in cui si trova il ristorante
     * @param nazione nazione in cui si trova il ristorante
     * @param latitudine latitudine della posizione del ristorante
     * @param longitudine longitudine della posizione del ristorante
     * @param tipologieCucina tipologie di cucina offerte
     * @param prezzoMedio prezzo medio indicativo
     * @param consegna indica se è disponibile la consegna a domicilio
     * @param prenotazioneOnline indica se è disponibile la prenotazione online
     * @param mediaStelle valutazione media del ristorante
     * @param prezzoStimato indica se il prezzo medio è stato stimato
     */
    public RistoranteDTO(
            Integer idRistorante,
            String nome,
            String indirizzo,
            String citta,
            String nazione,
            Double latitudine,
            Double longitudine,
            List<String> tipologieCucina,
            Double prezzoMedio,
            Boolean consegna,
            Boolean prenotazioneOnline,
            Double mediaStelle,
            boolean prezzoStimato
    ) {
        this(idRistorante, nome, indirizzo, citta, nazione, latitudine, longitudine,
                tipologieCucina, prezzoMedio, consegna, prenotazioneOnline, mediaStelle,
                prezzoStimato, null, null, null);
    }

    /**
     * Crea un ristorante includendo i dati di contatto.
     *
     * @param idRistorante identificativo univoco del ristorante
     * @param nome nome del ristorante
     * @param indirizzo indirizzo fisico del ristorante
     * @param citta città in cui si trova il ristorante
     * @param nazione nazione in cui si trova il ristorante
     * @param latitudine latitudine della posizione del ristorante
     * @param longitudine longitudine della posizione del ristorante
     * @param tipologieCucina tipologie di cucina offerte
     * @param prezzoMedio prezzo medio indicativo
     * @param consegna indica se è disponibile la consegna a domicilio
     * @param prenotazioneOnline indica se è disponibile la prenotazione online
     * @param mediaStelle valutazione media del ristorante
     * @param prezzoStimato indica se il prezzo medio è stato stimato
     * @param telefono numero di telefono del ristorante
     * @param sitoWeb sito web del ristorante
     */
    public RistoranteDTO(
            Integer idRistorante,
            String nome,
            String indirizzo,
            String citta,
            String nazione,
            Double latitudine,
            Double longitudine,
            List<String> tipologieCucina,
            Double prezzoMedio,
            Boolean consegna,
            Boolean prenotazioneOnline,
            Double mediaStelle,
            boolean prezzoStimato,
            String telefono,
            String sitoWeb
    ) {
        this(idRistorante, nome, indirizzo, citta, nazione, latitudine, longitudine,
                tipologieCucina, prezzoMedio, consegna, prenotazioneOnline, mediaStelle,
                prezzoStimato, telefono, sitoWeb, null);
    }
}