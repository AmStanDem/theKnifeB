package org.uninsubria.common.dto;

import java.io.Serial;
import java.io.Serializable;

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