/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione lanciata quando uno o più dati richiesti non sono stati forniti.
 */
public class DatiMancantiException extends TheKnifeException {

    /**
     * Crea l'eccezione con il dettaglio dei dati mancanti.
     *
     * @param message descrizione dei dati mancanti
     */
    public DatiMancantiException(String message) {
        super(message);
    }
}