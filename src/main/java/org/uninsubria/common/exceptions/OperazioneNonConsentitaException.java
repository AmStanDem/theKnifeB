package org.uninsubria.common.exceptions;

public class OperazioneNonConsentitaException extends RuntimeException {
    public OperazioneNonConsentitaException(String motivazione) {
        super("Accesso negato: " + motivazione);
    }
}
