package org.uninsubria.common.dto;

import java.io.Serializable;

public record RistoranteDTO(
        Integer idRistorante,
        String nome,
        String indirizzo,
        String nazione,
        String citta,
        Double latitudine,
        Double longitudine,
        String tipoCucina,
        Double prezzoMedio,
        Boolean delivery,
        Boolean bookingOnline,
        Double mediaStelle // Valore aggregato, non presente come colonna fissa nel DB
) implements Serializable {}

