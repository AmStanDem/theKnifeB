-- ==============================================================================
-- THE KNIFE - SCHEMA INIZIALE DEL DATABASE (V1)
-- ==============================================================================

DROP TABLE IF EXISTS Preferiti CASCADE;
DROP TABLE IF EXISTS Risposte_Recensioni CASCADE;
DROP TABLE IF EXISTS Recensioni CASCADE;
DROP TABLE IF EXISTS Ristoranti_Tipologie CASCADE;
DROP TABLE IF EXISTS Tipologia_Cucina CASCADE;
DROP TABLE IF EXISTS RistorantiTheKnife CASCADE;
DROP TABLE IF EXISTS Utenti CASCADE;

-- ==============================================================================
-- 1. IDENTITÀ E ACCESSI
-- ==============================================================================
CREATE TABLE Utenti (
                        id_utente SERIAL PRIMARY KEY,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        nome VARCHAR(100) NOT NULL,
                        cognome VARCHAR(100) NOT NULL,
                        data_nascita DATE,                  -- Facoltativo come da specifiche
                        domicilio VARCHAR(255) NOT NULL,    -- Obbligatorio come da specifiche
                        ruolo VARCHAR(20) NOT NULL,         -- Cliente o Gestore
                        data_registrazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_ruolo CHECK (ruolo IN ('cliente', 'gestore'))
);

-- ==============================================================================
-- 2. DOMINIO CORE: RISTORANTI E TIPOLOGIE CUCINA (3NF)
-- ==============================================================================
-- Tabella principale denominata "RistorantiTheKnife" come da specifiche.
CREATE TABLE RistorantiTheKnife (
                                    id_ristorante SERIAL PRIMARY KEY,
                                    id_gestore INT NOT NULL,
                                    nome VARCHAR(255) NOT NULL,
                                    indirizzo VARCHAR(255) NOT NULL,
                                    citta VARCHAR(100) NOT NULL,
                                    nazione VARCHAR(100) NOT NULL,
                                    latitudine NUMERIC(10, 7) NOT NULL,
                                    longitudine NUMERIC(10, 7) NOT NULL,
                                    prezzo_medio NUMERIC(6, 2) NOT NULL,
                                    delivery BOOLEAN NOT NULL DEFAULT FALSE,
                                    booking_online BOOLEAN NOT NULL DEFAULT FALSE,
                                    data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    CONSTRAINT fk_ristorante_gestore FOREIGN KEY (id_gestore)
                                        REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                                    CONSTRAINT chk_prezzo_medio CHECK (prezzo_medio >= 0)
);

-- Tabella di Dominio per le Cucine
CREATE TABLE Tipologia_Cucina (
                                  id_tipologia SERIAL PRIMARY KEY,
                                  nome VARCHAR(100) UNIQUE NOT NULL -- UNIQUE previene duplicati come "Italiana" e "Italiana"
);

-- Tabella Ponte (Relazione Molti-a-Molti)
CREATE TABLE Ristoranti_Tipologie (
                                      id_ristorante INT NOT NULL,
                                      id_tipologia INT NOT NULL,
                                      PRIMARY KEY (id_ristorante, id_tipologia),
                                      CONSTRAINT fk_rt_ristorante FOREIGN KEY (id_ristorante)
                                          REFERENCES RistorantiTheKnife(id_ristorante) ON DELETE CASCADE,
                                      CONSTRAINT fk_rt_tipologia FOREIGN KEY (id_tipologia)
                                          REFERENCES Tipologia_Cucina(id_tipologia) ON DELETE CASCADE
);

-- ==============================================================================
-- 3. FEEDBACK: RECENSIONI
-- ==============================================================================
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
                                REFERENCES RistorantiTheKnife(id_ristorante) ON DELETE CASCADE,
                            CONSTRAINT chk_valutazione CHECK (valutazione BETWEEN 1 AND 5),
                            CONSTRAINT uq_recensione_unica UNIQUE (id_utente, id_ristorante)
);

-- ==============================================================================
-- 4. INTERAZIONI: RISPOSTE ALLE RECENSIONI
-- ==============================================================================
CREATE TABLE Risposte_Recensioni (
                                     id_recensione INT PRIMARY KEY,
                                     id_gestore INT NOT NULL,
                                     testo TEXT NOT NULL,
                                     data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_risposta_recensione FOREIGN KEY (id_recensione)
                                         REFERENCES Recensioni(id_recensione) ON DELETE CASCADE,
                                     CONSTRAINT fk_risposta_gestore FOREIGN KEY (id_gestore)
                                         REFERENCES Utenti(id_utente) ON DELETE CASCADE
);

-- ==============================================================================
-- 5. RELAZIONI MOLTI-A-MOLTI: PREFERITI
-- ==============================================================================
CREATE TABLE Preferiti (
                           id_utente INT NOT NULL,
                           id_ristorante INT NOT NULL,
                           data_aggiunta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id_utente, id_ristorante),
                           CONSTRAINT fk_preferiti_utente FOREIGN KEY (id_utente)
                               REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                           CONSTRAINT fk_preferiti_ristorante FOREIGN KEY (id_ristorante)
                               REFERENCES RistorantiTheKnife(id_ristorante) ON DELETE CASCADE
);

-- ==============================================================================
-- INDICI PRESTAZIONALI
-- ==============================================================================
CREATE INDEX idx_ristoranti_gestore ON RistorantiTheKnife(id_gestore);
CREATE INDEX idx_ristoranti_citta ON RistorantiTheKnife(LOWER(citta));
CREATE INDEX idx_rt_tipologia ON Ristoranti_Tipologie(id_tipologia);
CREATE INDEX idx_recensioni_ristorante ON Recensioni(id_ristorante);
CREATE INDEX idx_recensioni_utente ON Recensioni(id_utente);
CREATE INDEX idx_preferiti_ristorante ON Preferiti(id_ristorante);