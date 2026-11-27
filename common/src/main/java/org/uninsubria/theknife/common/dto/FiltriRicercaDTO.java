/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import org.uninsubria.theknife.common.util.FasciaPrezzo;

/**
 * Filtri utilizzati per cercare i ristoranti.
 * È obbligatoria una locazione testuale oppure una coppia di coordinate con il relativo raggio.
 *
 * @param locazione nome della località da utilizzare come punto di ricerca
 * @param latitudineRiferimento latitudine del punto centrale della ricerca
 * @param longitudineRiferimento longitudine del punto centrale della ricerca
 * @param raggioKm raggio di ricerca in chilometri
 * @param tipoCucina tipologia di cucina da cercare
 * @param prezzoMin limite inferiore simbolico; {@code *} indica qualsiasi fascia
 * @param prezzoMax limite superiore simbolico; {@code *} indica qualsiasi fascia
 * @param mediaStelleMinima valutazione media minima compresa tra 0.0 e 5.0
 * @param consegna indica se cercare ristoranti con consegna a domicilio
 * @param prenotazioneOnline indica se cercare ristoranti con prenotazione online
 */
public record FiltriRicercaDTO(
        String locazione,
        Double latitudineRiferimento,
        Double longitudineRiferimento,
        Double raggioKm,
        String tipoCucina,
        String prezzoMin,
        String prezzoMax,
        Double mediaStelleMinima, // Aggiunto per risolvere l'errore di compilazione nel DAO
        Boolean consegna,
        Boolean prenotazioneOnline
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Crea i filtri di ricerca e ne verifica la coerenza.
     *
     * @throws IllegalArgumentException se non è fornita una locazione valida,
     *         se il prezzo minimo supera quello massimo o se la valutazione non è compresa tra 0.0 e 5.0
     */
    public FiltriRicercaDTO {
        boolean haLocazioneTestuale = locazione != null && !locazione.trim().isEmpty();
        boolean haCoordinateCompletate = latitudineRiferimento != null &&
                longitudineRiferimento != null &&
                raggioKm != null;

        if (!haLocazioneTestuale && !haCoordinateCompletate) {
            throw new IllegalArgumentException("È obbligatorio fornire una locazione testuale (es. città) OPPURE coordinate e raggio di ricerca.");
        }

        int livelloMin = FasciaPrezzo.livello(prezzoMin);
        int livelloMax = FasciaPrezzo.livello(prezzoMax);
        if ((prezzoMin != null && livelloMin == 0 && !FasciaPrezzo.QUALSIASI.equals(prezzoMin))
                || (prezzoMax != null && livelloMax == 0 && !FasciaPrezzo.QUALSIASI.equals(prezzoMax))
                || (livelloMin > 0 && livelloMax > 0 && livelloMin > livelloMax)) {
            throw new IllegalArgumentException("Intervallo di fasce di prezzo non valido.");
        }

        if (mediaStelleMinima != null && (mediaStelleMinima < 0.0 || mediaStelleMinima > 5.0)) {
            throw new IllegalArgumentException("La valutazione media deve essere compresa tra 0.0 e 5.0.");
        }
    }
}