/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione lanciata quando un utente tenta un'operazione non consentita.
 */
public class OperazioneNonConsentitaException extends TheKnifeException {
    /**
     * Crea l'eccezione con la motivazione dell'errore.
     *
     * @param motivazione motivo per cui l'operazione non è consentita
     */
    public OperazioneNonConsentitaException(String motivazione) {
        super("Accesso negato: " + motivazione);
    }
}