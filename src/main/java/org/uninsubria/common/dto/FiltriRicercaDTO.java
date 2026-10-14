package org.uninsubria.common.dto;

import java.io.Serial;
import java.io.Serializable;

public record FiltriRicercaDTO(
        String locazione,
        Double latitudineRiferimento,
        Double longitudineRiferimento,
        Double raggioKm, // AGGIUNTO: Necessario per definire l'area della formula di Haversine
        String tipoCucina,
        Double prezzoMin,
        Double prezzoMax,
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

        // La ricerca fallisce solo se MANCANO ENTRAMBI i parametri spaziali
        if (!haLocazioneTestuale && !haCoordinateCompletate) {
            throw new IllegalArgumentException("È obbligatorio fornire una locazione testuale (es. città) OPPURE coordinate e raggio di ricerca.");
        }

        if (prezzoMin != null && prezzoMax != null && prezzoMin > prezzoMax) {
            throw new IllegalArgumentException("Il prezzo minimo non può essere superiore al massimo.");
        }
    }
}