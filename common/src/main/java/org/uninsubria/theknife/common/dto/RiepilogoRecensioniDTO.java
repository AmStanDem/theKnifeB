/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Riepilogo sintetico delle recensioni di un ristorante.
 *
 * @param idRistorante identificativo del ristorante
 * @param mediaStelle valutazione media delle recensioni
 * @param numeroRecensioni numero totale di recensioni
 */
public record RiepilogoRecensioniDTO(
        Integer idRistorante,
        double mediaStelle,
        int numeroRecensioni
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
