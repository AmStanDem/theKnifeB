package org.uninsubria.common.exceptions;

public class OperazioneNonConsentitaException extends TheKnifeException {
    public OperazioneNonConsentitaException(String motivazione) {
        super("Accesso negato: " + motivazione, null);
    }
}