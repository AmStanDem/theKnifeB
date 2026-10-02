package org.uninsubria.common.dto;
import org.uninsubria.common.enums.RuoloUtente;
import java.io.Serializable;
import java.time.LocalDate;

public record UtenteDTO(
        Integer idUtente,
        String nome,
        String cognome,
        String email,
        LocalDate dataNascita,
        String domicilio,
        RuoloUtente ruolo
) implements Serializable {}