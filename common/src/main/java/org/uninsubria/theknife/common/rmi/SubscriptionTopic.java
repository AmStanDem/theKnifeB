/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.rmi;

/**
 * Ambiti disponibili per le sottoscrizioni alle notifiche di modifica.
 */
public enum SubscriptionTopic {
    /** Eventi relativi a un singolo ristorante, identificato dalla destinazione della sottoscrizione. */
    RISTORANTE,
    /** Eventi relativi a un singolo utente, identificato dalla destinazione della sottoscrizione. */
    UTENTE,
    /** Eventi del catalogo dei ristoranti, esclusi gli aggiornamenti dei preferiti. */
    CATALOGO
}
