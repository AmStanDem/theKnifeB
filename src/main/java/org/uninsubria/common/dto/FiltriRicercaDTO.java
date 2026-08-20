package org.uninsubria.common.dto;

import java.io.Serializable;

public record FiltriRicercaDTO(
        String locazione,
        Double latitudineRiferimento,
        Double longitudineRiferimento,
        String tipoCucina,
        Double prezzoMin,
        Double prezzoMax,
        Boolean delivery,
        Boolean bookingOnline,
        Integer mediaStelleMinima
) implements Serializable {

    public FiltriRicercaDTO {
        if (locazione == null || locazione.trim().isEmpty()) {
            throw new IllegalArgumentException("La locazione è un parametro obbligatorio.");
        }
        if (prezzoMin != null && prezzoMax != null && prezzoMin > prezzoMax) {
            throw new IllegalArgumentException("Il prezzo minimo non può essere superiore al massimo.");
        }
    }
}