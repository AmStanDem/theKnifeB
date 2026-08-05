-- 1. Creazione Tabella UTENTI
CREATE TABLE Utenti (
                        id_utente SERIAL PRIMARY KEY,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        nome VARCHAR(100) NOT NULL,
                        cognome VARCHAR(100) NOT NULL,
                        ruolo VARCHAR(20) NOT NULL,
                        data_registrazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Vincolo di dominio sul ruolo
                        CONSTRAINT chk_ruolo CHECK (ruolo IN ('cliente', 'gestore'))
);

-- 2. Creazione Tabella RISTORANTI
-- 2. Creazione Tabella RISTORANTI (Aggiornata con prezzo medio)
CREATE TABLE Ristoranti (
                            id_ristorante SERIAL PRIMARY KEY,
                            id_gestore INT NOT NULL,
                            nome VARCHAR(255) NOT NULL,
                            indirizzo VARCHAR(255) NOT NULL,
                            nazione VARCHAR(100) NOT NULL,
                            latitudine NUMERIC(10, 7) NOT NULL,
                            longitudine NUMERIC(10, 7) NOT NULL,
                            tipo_cucina VARCHAR(100) NOT NULL,

    -- Sostituzione di fascia_prezzo (1-5) con il prezzo medio monetario
                            prezzo_medio NUMERIC(6, 2) NOT NULL,

                            delivery BOOLEAN NOT NULL DEFAULT FALSE,
                            booking_online BOOLEAN NOT NULL DEFAULT FALSE,
                            data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Vincoli strutturali
                            CONSTRAINT fk_ristorante_gestore FOREIGN KEY (id_gestore)
                                REFERENCES Utenti(id_utente) ON DELETE CASCADE,

    -- Impedisce l'inserimento di prezzi medi negativi
                            CONSTRAINT chk_prezzo_medio CHECK (prezzo_medio >= 0)
);

-- 3. Creazione Tabella RECENSIONI
CREATE TABLE Recensioni (
                            id_recensione SERIAL PRIMARY KEY,
                            id_utente INT NOT NULL,
                            id_ristorante INT NOT NULL,
                            valutazione INT NOT NULL,
                            testo TEXT NOT NULL,
                            data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            data_modifica TIMESTAMP,

    -- Vincoli strutturali
                            CONSTRAINT fk_recensione_utente FOREIGN KEY (id_utente)
                                REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                            CONSTRAINT fk_recensione_ristorante FOREIGN KEY (id_ristorante)
                                REFERENCES Ristoranti(id_ristorante) ON DELETE CASCADE,
                            CONSTRAINT chk_valutazione CHECK (valutazione BETWEEN 1 AND 5)
);

-- 4. Creazione Tabella PREFERITI (Associazione M:N)
CREATE TABLE Preferiti (
                           id_preferito SERIAL PRIMARY KEY,
                           id_utente INT NOT NULL,
                           id_ristorante INT NOT NULL,
                           data_aggiunta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Vincoli strutturali
                           CONSTRAINT fk_preferiti_utente FOREIGN KEY (id_utente)
                               REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                           CONSTRAINT fk_preferiti_ristorante FOREIGN KEY (id_ristorante)
                               REFERENCES Ristoranti(id_ristorante) ON DELETE CASCADE,

    -- Impedisce duplicati logici (stesso ristorante salvato più volte dallo stesso utente)
                           CONSTRAINT uq_preferito_unico UNIQUE (id_utente, id_ristorante)
);

-- 5. Creazione Tabella RISPOSTE_RECENSIONI (Associazione 1:1 rigida)
CREATE TABLE Risposte_Recensioni (
                                     id_recensione INT PRIMARY KEY,
                                     testo TEXT NOT NULL,
                                     data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- id_recensione funge sia da PK che da FK, garantendo 1 sola risposta possibile
                                     CONSTRAINT fk_risposta_recensione FOREIGN KEY (id_recensione)
                                         REFERENCES Recensioni(id_recensione) ON DELETE CASCADE
);