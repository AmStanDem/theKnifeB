/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO.RuoloUtente;

/**
 * Contiene lo stato dell'utente attualmente autenticato sul client.
 */
public final class SessioneUtente {

    private static UtenteDTO utenteCorrente;

    private SessioneUtente() {
        // classe di utilità, non istanziabile
    }

    /**
     * Imposta l'utente loggato dopo un accesso o una registrazione andati a buon fine.
     *
     * @param utente l'utente autenticato
     */
    public static void accedi(UtenteDTO utente) {
        utenteCorrente = utente;
    }

    /**
     * Effettua l'uscita, azzerando lo stato della sessione.
     */
    public static void esci() {
        utenteCorrente = null;

    }

    /**
     * Indica se è presente un utente autenticato.
     *
     * @return {@code true} se un utente è attualmente loggato
     */
    public static boolean eLoggato() {
        return utenteCorrente != null;
    }

    /**
     * Restituisce i dati dell'utente autenticato.
     *
     * @return l'utente attualmente loggato, o {@code null} se si è in modalità guest
     */
    public static UtenteDTO ottieniUtenteCorrente() {
        return utenteCorrente;
    }

    /**
     * Verifica se l'utente autenticato ha il ruolo di gestore.
     *
     * @return {@code true} se l'utente loggato ha ruolo gestore
     */
    public static boolean eGestore() {
        return eLoggato() && utenteCorrente.ruolo() == RuoloUtente.GESTORE;
    }

    /**
     * Verifica se l'utente autenticato ha il ruolo di cliente.
     *
     * @return {@code true} se l'utente loggato ha ruolo cliente
     */
    public static boolean eCliente() {
        return eLoggato() && utenteCorrente.ruolo() == RuoloUtente.CLIENTE;
    }
}
