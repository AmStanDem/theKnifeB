package org.uninsubria.common.dto;

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
