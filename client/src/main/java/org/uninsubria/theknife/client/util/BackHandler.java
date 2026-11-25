/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

/**
 * Permette a un controllore di intercettare il pulsante "indietro" del layout
 * comune prima che venga applicata la navigazione nello storico.
 * <p>
 * Le viste che hanno stati interni (ad esempio una sezione di modifica aperta
 * o un form con dati non salvati) possono implementare questa interfaccia per
 * gestire localmente il comando, evitando di uscire dalla pagina.
 * </p>
 */
public interface BackHandler {

    /**
     * Gestisce una richiesta di navigazione all'indietro.
     *
     * @return {@code true} se il controllore ha gestito la richiesta e la
     *         navigazione nello storico deve essere annullata; {@code false}
     *         per lasciare che il {@link SceneManager} torni alla pagina
     *         precedente
     */
    boolean richiestaIndietro();
}
