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
        int limite
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
    }
}
