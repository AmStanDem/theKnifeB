/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

public class UtenteGiaEsistenteException extends TheKnifeException {
    public UtenteGiaEsistenteException(String email) {
        super("Impossibile procedere: l'email " + email + " è già registrata nel sistema.");
    }
}