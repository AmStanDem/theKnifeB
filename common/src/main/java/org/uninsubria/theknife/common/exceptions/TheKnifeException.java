/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione base per gli errori applicativi gestiti dal sistema TheKnife.
 */
public class TheKnifeException extends Exception {
    /**
     * Crea un'eccezione con il messaggio descrittivo dell'errore.
     *
     * @param message descrizione dell'errore verificato
     */
    public TheKnifeException(String message) {
        super(message);
    }
}
