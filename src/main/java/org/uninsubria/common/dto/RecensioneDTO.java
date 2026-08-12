package org.uninsubria.common.dto;
import java.io.Serializable;
import java.time.LocalDateTime;

public record RecensioneDTO(
        Integer idRecensione,
        Integer valutazione,
        String testo,
        String nomeAutore,         // Denormalizzato dalla tabella Utenti
        LocalDateTime dataCreazione,
        String rispostaGestore     // Null se il gestore non ha ancora risposto
) implements Serializable {}