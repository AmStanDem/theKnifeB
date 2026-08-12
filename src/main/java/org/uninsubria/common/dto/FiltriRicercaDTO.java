package org.uninsubria.common.dto;
import java.io.Serializable;

public record FiltriRicercaDTO(
        String locazione, // Obbligatorio
        String tipoCucina,
        Double prezzoMax,
        Boolean delivery,
        Boolean bookingOnline,
        Integer mediaStelleMinima
) implements Serializable {}