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
 * @param fasciaPrezzo fascia simbolica del ristorante
 * @param consegna indica se è disponibile la consegna a domicilio
 * @param prenotazioneOnline indica se è disponibile la prenotazione online
 * @param mediaStelle valutazione media del ristorante
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
        String fasciaPrezzo,
        Boolean consegna,
        Boolean prenotazioneOnline,
        Double mediaStelle,
        String telefono,
        String sitoWeb,
        String descrizione
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}