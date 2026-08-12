package org.uninsubria.common.exceptions;

import java.util.List;

public class DatiMancantiException extends TheKnifeException {
    private final List<String> campiMancanti;

    public DatiMancantiException(List<String> campiMancanti) {
        super("Impossibile completare l'operazione. Controlla i campi obbligatori.", null);
        this.campiMancanti = campiMancanti;
    }

    public List<String> getCampiMancanti() {
        return campiMancanti;
    }
}