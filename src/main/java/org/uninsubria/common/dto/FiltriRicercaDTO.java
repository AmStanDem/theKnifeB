package org.uninsubria.common.dto;

import java.io.Serializable;

public record FiltriRicercaDTO(
        String locazione,
        String tipoCucina,
        Double prezzoMin,
        Double prezzoMax,
        Boolean delivery,
        Boolean bookingOnline,
        Integer mediaStelleMinima
) implements Serializable {}