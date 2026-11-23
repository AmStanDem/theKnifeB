/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione lanciata quando viene ripetuta un'operazione già effettuata.
 */
public class AzioneDuplicataException extends TheKnifeException {
    /**
     * Crea l'eccezione descrivendo l'operazione duplicata.
     *
     * @param dettaglio descrizione dell'operazione duplicata
     */
    public AzioneDuplicataException(String dettaglio) {
        super("Operazione già effettuata: " + dettaglio);
    }
}