/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.exceptions;

/**
 * Eccezione di dominio sollevata quando un'operazione (es. modifica o eliminazione)
 * fallisce perché la recensione target non esiste nel database oppure
 * l'utente richiedente non ne è il legittimo proprietario (violazione IDOR).
 */
public class RecensioneNonTrovataException extends Exception {

    /**
     * Costruisce l'eccezione con un messaggio descrittivo.
     *
     * @param message Il messaggio di errore da mostrare all'utente (es. tramite un Alert in JavaFX).
     */
    public RecensioneNonTrovataException(String message) {
        super(message);
    }

    /**
     * Costruisce l'eccezione mantenendo lo stack trace originale dell'errore.
     * Utile per loggare cause più profonde (es. una SQLException imprevista)
     * mantenendo però il significato di business.
     *
     * @param message Il messaggio di errore di business.
     * @param cause L'eccezione tecnica scatenante.
     */
    public RecensioneNonTrovataException(String message, Throwable cause) {
        super(message, cause);
    }
}