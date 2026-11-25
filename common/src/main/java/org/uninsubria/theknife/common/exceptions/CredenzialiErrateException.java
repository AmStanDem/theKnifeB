/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione lanciata quando email o password inserite non sono valide.
 */
public class CredenzialiErrateException extends TheKnifeException {
    /**
     * Crea l'eccezione con il messaggio standard relativo alle credenziali non valide.
     */
    public CredenzialiErrateException() {
        super("Le credenziali inserite non sono valide. Verifica email e password.");
    }
}