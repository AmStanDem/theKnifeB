package org.uninsubria.common.exceptions;

public class CredenzialiErrateException extends TheKnifeException {
    public CredenzialiErrateException() {
        super("Le credenziali inserite non sono valide. Verifica email e password.");
    }
}