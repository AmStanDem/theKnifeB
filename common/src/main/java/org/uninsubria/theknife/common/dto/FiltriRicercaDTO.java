/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Filtri utilizzati per cercare i ristoranti.
 * È obbligatoria una locazione testuale oppure una coppia di coordinate con il relativo raggio.
 *
 * @param locazione nome della località da utilizzare come punto di ricerca
 * @param latitudineRiferimento latitudine del punto centrale della ricerca
 * @param longitudineRiferimento longitudine del punto centrale della ricerca
 * @param raggioKm raggio di ricerca in chilometri
 * @param tipoCucina tipologia di cucina da cercare
 * @param prezzoMin prezzo minimo accettato
 * @param prezzoMax prezzo massimo accettato
 * @param mediaStelleMinima valutazione media minima compresa tra 0.0 e 5.0
 * @param delivery indica se cercare ristoranti con consegna a domicilio
 * @param bookingOnline indica se cercare ristoranti con prenotazione online
 * @throws IllegalArgumentException se non è fornita una locazione valida,
 * se il prezzo minimo supera quello massimo o se la valutazione non è compresa tra 0.0 e 5.0
 */
public record FiltriRicercaDTO(
        String locazione,
        Double latitudineRiferimento,
        Double longitudineRiferimento,
        Double raggioKm,
        String tipoCucina,
        Double prezzoMin,
        Double prezzoMax,
        Double mediaStelleMinima, // Aggiunto per risolvere l'errore di compilazione nel DAO
        Boolean delivery,
        Boolean bookingOnline
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public FiltriRicercaDTO {
        boolean haLocazioneTestuale = locazione != null && !locazione.trim().isEmpty();
        boolean haCoordinateCompletate = latitudineRiferimento != null &&
                longitudineRiferimento != null &&
                raggioKm != null;

        if (!haLocazioneTestuale && !haCoordinateCompletate) {
            throw new IllegalArgumentException("È obbligatorio fornire una locazione testuale (es. città) OPPURE coordinate e raggio di ricerca.");
        }

        if (prezzoMin != null && prezzoMax != null && prezzoMin > prezzoMax) {
            throw new IllegalArgumentException("Il prezzo minimo non può essere superiore al massimo.");
        }

        if (mediaStelleMinima != null && (mediaStelleMinima < 0.0 || mediaStelleMinima > 5.0)) {
            throw new IllegalArgumentException("La valutazione media deve essere compresa tra 0.0 e 5.0.");
        }
    }
}