package org.uninsubria.common.dto;

import java.io.Serializable;
import java.util.List;

public record RistoranteDTO(
        Integer idRistorante,
        String nome,
        String indirizzo,
        String citta,
        String nazione,
        Double latitudine,
        Double longitudine,
        List<String> tipologieCucina,
        Double prezzoMedio,
        Boolean delivery,
        Boolean bookingOnline,
        Double mediaStelle
) implements Serializable {}