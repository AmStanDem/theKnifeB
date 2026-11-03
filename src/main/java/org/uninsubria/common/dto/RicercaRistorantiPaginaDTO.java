package org.uninsubria.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record RicercaRistorantiPaginaDTO(
        String localita,
        String testo,
        List<String> tipologieCucina,
        Double prezzoMin,
        Double prezzoMax,
        Double mediaStelleMinima,
        int offset,
        int limite,
        Double latitudineRiferimento,
        Double longitudineRiferimento,
        Double raggioKm,
        boolean delivery,
        boolean bookingOnline
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public RicercaRistorantiPaginaDTO {

        localita = localita == null ? "" : localita.trim();
        tipologieCucina = tipologieCucina == null ? List.of() : List.copyOf(tipologieCucina);
        if (prezzoMin != null && prezzoMax != null && prezzoMin > prezzoMax) {
            throw new IllegalArgumentException("Il prezzo minimo non può superare il prezzo massimo.");
        }
        if (mediaStelleMinima != null && (mediaStelleMinima < 0 || mediaStelleMinima > 5)) {
            throw new IllegalArgumentException("La valutazione deve essere compresa tra 0 e 5.");
        }
        if (offset < 0 || limite < 1 || limite > 100) {
            throw new IllegalArgumentException("Paginazione non valida.");
        }
        boolean haLatitudine = latitudineRiferimento != null;
        boolean haLongitudine = longitudineRiferimento != null;
        boolean haRaggio = raggioKm != null;
        if (haLatitudine != haLongitudine || haLatitudine != haRaggio) {
            throw new IllegalArgumentException("Per la ricerca geografica servono coordinate e raggio.");
        }
        if (haLatitudine && (!Double.isFinite(latitudineRiferimento)
                || latitudineRiferimento < -90 || latitudineRiferimento > 90
                || !Double.isFinite(longitudineRiferimento)
                || longitudineRiferimento < -180 || longitudineRiferimento > 180
                || !Double.isFinite(raggioKm) || raggioKm <= 0 || raggioKm > 500)) {
            throw new IllegalArgumentException("Coordinate o raggio geografico non validi.");
        }
    }
}
