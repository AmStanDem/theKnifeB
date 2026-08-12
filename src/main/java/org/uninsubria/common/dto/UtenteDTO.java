package org.uninsubria.common.dto;
import org.uninsubria.common.enums.RuoloUtente;
import java.io.Serializable;

public record UtenteDTO(
        Integer idUtente,
        String nome,
        String cognome,
        String email,
        RuoloUtente ruolo
) implements Serializable {}