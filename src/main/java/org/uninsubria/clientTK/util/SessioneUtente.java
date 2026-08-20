package org.uninsubria.clientTK.util;

import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;

/**
 * Contiene lo stato dell'utente attualmente autenticato sul client.
 * <p>
 * Essendo un client desktop mono-utente per istanza (una sola persona usa
 * l'applicazione client alla volta), è sufficiente un semplice singleton
 * con campi statici: non ci sono problemi di concorrenza da gestire qui,
 * quella riguarda solo il serverTK che serve più client in parallelo.
 *
 * @author TheKnife Team
 */
public final class SessioneUtente {

    private static UtenteDTO utenteCorrente;

    private SessioneUtente() {
        // classe di utilità, non istanziabile
    }

    /**
     * Imposta l'utente loggato dopo un login o una registrazione andati a buon fine.
     *
     * @param utente l'utente autenticato
     */
    public static void login(UtenteDTO utente) {
        utenteCorrente = utente;
    }

    /**
     * Effettua il logout, azzerando lo stato della sessione.
     */
    public static void logout() {
        utenteCorrente = null;
    }

    /**
     * @return {@code true} se un utente è attualmente loggato
     */
    public static boolean isLoggato() {
        return utenteCorrente != null;
    }

    /**
     * @return l'utente attualmente loggato, o {@code null} se si è in modalità guest
     */
    public static UtenteDTO getUtenteCorrente() {
        return utenteCorrente;
    }

    /**
     * @return {@code true} se l'utente loggato ha ruolo gestore
     */
    public static boolean isGestore() {
        return isLoggato() && utenteCorrente.ruolo() == RuoloUtente.GESTORE;
    }

    /**
     * @return {@code true} se l'utente loggato ha ruolo cliente
     */
    public static boolean isCliente() {
        return isLoggato() && utenteCorrente.ruolo() == RuoloUtente.CLIENTE;
    }
}
