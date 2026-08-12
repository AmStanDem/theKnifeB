package org.uninsubria.common.exceptions;

public class RistoranteNonTrovatoException extends TheKnifeException {
    public RistoranteNonTrovatoException(Integer idRistorante) {
        super("Il ristorante con ID " + idRistorante + " non esiste o è stato rimosso.", null);
    }
}