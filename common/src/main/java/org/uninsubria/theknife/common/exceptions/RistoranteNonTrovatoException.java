/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione lanciata quando un ristorante richiesto non esiste o è stato rimosso.
 */
public class RistoranteNonTrovatoException extends TheKnifeException {
    /**
     * Crea l'eccezione indicando l'identificativo del ristorante non trovato.
     *
     * @param idRistorante identificativo del ristorante
     */
    public RistoranteNonTrovatoException(Integer idRistorante) {
        super("Il ristorante con ID " + idRistorante + " non esiste o è stato rimosso.");
    }

    /**
     * Crea l'eccezione senza indicare un identificativo specifico.
     */
    public RistoranteNonTrovatoException() {
        super("Il ristorante con ID non esiste o è stato rimosso.");
    }
}