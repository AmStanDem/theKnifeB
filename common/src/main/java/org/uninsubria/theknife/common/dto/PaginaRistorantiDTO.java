/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Pagina dei risultati di una ricerca di ristoranti.
 *
 * @param ristoranti ristoranti inclusi nella pagina
 * @param hasMore indica se sono disponibili ulteriori pagine di risultati
 */
public record PaginaRistorantiDTO(
        List<RistoranteDTO> ristoranti,
        boolean hasMore
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Crea una pagina copiando l'elenco dei ristoranti in una lista non modificabile.
     *
     * @throws NullPointerException se l'elenco dei ristoranti è {@code null} o contiene elementi {@code null}
     */
    public PaginaRistorantiDTO {
        ristoranti = List.copyOf(ristoranti);
    }
}
