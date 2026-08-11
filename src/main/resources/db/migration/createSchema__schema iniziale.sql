CREATE TABLE Utenti (

                        id_utente SERIAL PRIMARY KEY,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        nome VARCHAR(100) NOT NULL,
                        cognome VARCHAR(100) NOT NULL,
                        ruolo VARCHAR(20) NOT NULL,
                        data_registrazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_ruolo CHECK (ruolo IN ('cliente', 'gestore'))
);

CREATE TABLE Ristoranti (
                            id_ristorante SERIAL PRIMARY KEY,
                            id_gestore INT NOT NULL,
                            nome VARCHAR(255) NOT NULL,
                            indirizzo VARCHAR(255) NOT NULL,
                            nazione VARCHAR(100) NOT NULL,
                            latitudine NUMERIC(10, 7) NOT NULL,
                            longitudine NUMERIC(10, 7) NOT NULL,
                            tipo_cucina VARCHAR(100) NOT NULL,
                            prezzo_medio NUMERIC(6, 2) NOT NULL,
                            delivery BOOLEAN NOT NULL DEFAULT FALSE,
                            booking_online BOOLEAN NOT NULL DEFAULT FALSE,
                            data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_ristorante_gestore FOREIGN KEY (id_gestore)
                                REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                            CONSTRAINT chk_prezzo_medio CHECK (prezzo_medio >= 0)
);

CREATE TABLE Recensioni (
                            id_recensione SERIAL PRIMARY KEY,
                            id_utente INT NOT NULL,
                            id_ristorante INT NOT NULL,
                            valutazione INT NOT NULL,
                            testo TEXT NOT NULL,
                            data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            data_modifica TIMESTAMP,
                            CONSTRAINT fk_recensione_utente FOREIGN KEY (id_utente)
                                REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                            CONSTRAINT fk_recensione_ristorante FOREIGN KEY (id_ristorante)
                                REFERENCES Ristoranti(id_ristorante) ON DELETE CASCADE,
                            CONSTRAINT chk_valutazione CHECK (valutazione BETWEEN 1 AND 5)
);

CREATE TABLE Preferiti (
                           id_preferito SERIAL PRIMARY KEY,
                           id_utente INT NOT NULL,
                           id_ristorante INT NOT NULL,
                           data_aggiunta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_preferiti_utente FOREIGN KEY (id_utente)
                               REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                           CONSTRAINT fk_preferiti_ristorante FOREIGN KEY (id_ristorante)
                               REFERENCES Ristoranti(id_ristorante) ON DELETE CASCADE,
                           CONSTRAINT uq_preferito_unico UNIQUE (id_utente, id_ristorante)
);

CREATE TABLE Risposte_Recensioni (
                                     id_recensione INT PRIMARY KEY,
                                     testo TEXT NOT NULL,
                                     data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_risposta_recensione FOREIGN KEY (id_recensione)
                                         REFERENCES Recensioni(id_recensione) ON DELETE CASCADE
);

CREATE INDEX idx_ristoranti_gestore ON Ristoranti(id_gestore);
CREATE INDEX idx_recensioni_ristorante ON Recensioni(id_ristorante);
CREATE INDEX idx_recensioni_utente ON Recensioni(id_utente);
CREATE INDEX idx_preferiti_utente ON Preferiti(id_utente);
CREATE INDEX idx_preferiti_ristorante ON Preferiti(id_ristorante);

CREATE OR REPLACE FUNCTION verifica_gestore_risposta()
    RETURNS TRIGGER AS $$
DECLARE
    v_ruolo_utente VARCHAR(20);
BEGIN
    SELECT u.ruolo INTO v_ruolo_utente
    FROM Recensioni rec
             JOIN Ristoranti ris ON rec.id_ristorante = ris.id_ristorante
             JOIN Utenti u ON ris.id_gestore = u.id_utente
    WHERE rec.id_recensione = NEW.id_recensione;

    IF v_ruolo_utente IS NULL OR v_ruolo_utente <> 'gestore' THEN
        RAISE EXCEPTION 'Errore: Solo il gestore del ristorante puÃ² inserire una risposta a questa recensione.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_verifica_gestore_risposta
    BEFORE INSERT OR UPDATE ON Risposte_Recensioni
    FOR EACH ROW
EXECUTE FUNCTION verifica_gestore_risposta();