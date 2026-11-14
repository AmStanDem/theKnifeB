/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
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
        Double mediaStelle,
        boolean prezzoStimato
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public RistoranteDTO(
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
    ) {
        this(idRistorante, nome, indirizzo, citta, nazione, latitudine, longitudine,
                tipologieCucina, prezzoMedio, delivery, bookingOnline, mediaStelle, false);
    }
}