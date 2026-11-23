/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione lanciata quando una modifica usa una versione obsoleta del dato.
 */
public class ModificaObsoletaException extends TheKnifeException {
    /**
     * Crea l'eccezione con il dettaglio del conflitto rilevato.
     *
     * @param message descrizione dell'obsolescenza o del conflitto di modifica
     */
    public ModificaObsoletaException(String message) {
        super(message);
    }
}
