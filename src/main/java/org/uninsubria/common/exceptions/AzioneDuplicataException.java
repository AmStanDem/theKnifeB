package org.uninsubria.common.exceptions;

public class AzioneDuplicataException extends TheKnifeException {
    public AzioneDuplicataException(String dettaglio) {
        super("Operazione già effettuata: " + dettaglio);
    }
}