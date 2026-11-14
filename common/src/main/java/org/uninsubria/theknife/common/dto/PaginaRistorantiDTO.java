/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record PaginaRistorantiDTO(
        List<RistoranteDTO> ristoranti,
        boolean hasMore
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public PaginaRistorantiDTO {
        ristoranti = List.copyOf(ristoranti);
    }
}
