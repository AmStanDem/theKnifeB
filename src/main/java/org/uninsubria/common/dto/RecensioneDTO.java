package org.uninsubria.common.dto;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record RecensioneDTO(
        Integer idRecensione,
        Integer valutazione,
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
            Integer valutazione,
            String testo,
            String nomeAutore,
            LocalDateTime dataCreazione,
            String rispostaGestore
    ) {
        this(idRecensione, valutazione, testo, nomeAutore, dataCreazione,
                rispostaGestore, null, null);
    }
}