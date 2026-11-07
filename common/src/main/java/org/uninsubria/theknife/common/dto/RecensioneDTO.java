/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record RecensioneDTO(
        Integer idRecensione,
        Double valutazione,
        String testo,
        String nomeAutore,         // Denormalizzato dalla tabella Utenti
        LocalDateTime dataCreazione,
        String rispostaGestore,    // Null se il gestore non ha ancora risposto
        Integer idRistorante,
        String nomeRistorante
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public RecensioneDTO(
            Integer idRecensione,
            Double valutazione,
            String testo,
            String nomeAutore,
            LocalDateTime dataCreazione,
            String rispostaGestore
    ) {
        this(idRecensione, valutazione, testo, nomeAutore, dataCreazione,
                rispostaGestore, null, null);
    }
}