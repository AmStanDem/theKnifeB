package org.uninsubria.common.dto;
import java.io.Serializable;

public record FiltriRicercaDTO(
        String locazione, // Obbligatorio
        String tipoCucina,
        Double prezzoMax,
        Double prezzoMin,
        Boolean delivery,
        Boolean bookingOnline,
        Integer mediaStelleMinima
) implements Serializable {}