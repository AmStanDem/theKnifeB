package org.uninsubria.common.exceptions;

public class UtenteGiaEsistenteException extends TheKnifeException {
    public UtenteGiaEsistenteException(String email) {
        super("Impossibile procedere: l'email " + email + " è già registrata nel sistema.", null);
    }
}