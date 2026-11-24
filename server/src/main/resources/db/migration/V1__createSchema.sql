-- ==============================================================================
-- THE KNIFE - SCHEMA COMPLETO DEL DATABASE (V1 consolidato)
-- ==============================================================================
-- Include tutte le evoluzioni dello schema dalle versioni V1, V3, V4, V5,
-- V6, V7, V8, V11, V12 in un unico script iniziale.
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
                        data_nascita DATE,
                        domicilio VARCHAR(255) NOT NULL,
                        ruolo VARCHAR(20) NOT NULL,
                        data_registrazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_ruolo CHECK (ruolo IN ('cliente', 'gestore'))
);

-- ==============================================================================
-- 2. DOMINIO CORE: RISTORANTI E TIPOLOGIE CUCINA (3NF)
-- ==============================================================================
CREATE TABLE RistorantiTheKnife (
                                    id_ristorante SERIAL PRIMARY KEY,
                                    id_gestore INT NOT NULL,
                                    nome VARCHAR(255) NOT NULL,
                                    indirizzo VARCHAR(255) NOT NULL,
                                    citta VARCHAR(100) NOT NULL,
                                    nazione VARCHAR(100) NOT NULL,
                                    latitudine NUMERIC(10, 7) NOT NULL,
                                    longitudine NUMERIC(10, 7) NOT NULL,
                                    prezzo_medio NUMERIC(6, 2),
                                    delivery BOOLEAN NOT NULL DEFAULT FALSE,
                                    booking_online BOOLEAN NOT NULL DEFAULT FALSE,
                                    data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    prezzo_stimato BOOLEAN NOT NULL DEFAULT FALSE,
                                    telefono VARCHAR(64),
                                    sito_web TEXT,
                                    descrizione TEXT,
                                    CONSTRAINT fk_ristorante_gestore FOREIGN KEY (id_gestore)
                                        REFERENCES Utenti(id_utente) ON DELETE RESTRICT
);

CREATE TABLE Tipologia_Cucina (
                                  id_tipologia SERIAL PRIMARY KEY,
                                  nome VARCHAR(100) UNIQUE NOT NULL
);

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
-- 3. FEEDBACK: RECENSIONI (con mezze stelle, versione ottimistica)
-- ==============================================================================
CREATE TABLE Recensioni (
                            id_recensione SERIAL PRIMARY KEY,
                            id_utente INT NOT NULL,
                            id_ristorante INT NOT NULL,
                            valutazione NUMERIC(2, 1) NOT NULL,
                            testo TEXT NOT NULL,
                            data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            data_modifica TIMESTAMP,
                            versione INTEGER NOT NULL DEFAULT 0,
                            CONSTRAINT fk_recensione_utente FOREIGN KEY (id_utente)
                                REFERENCES Utenti(id_utente) ON DELETE CASCADE,
                            CONSTRAINT fk_recensione_ristorante FOREIGN KEY (id_ristorante)
                                REFERENCES RistorantiTheKnife(id_ristorante) ON DELETE CASCADE,
                            CONSTRAINT chk_valutazione CHECK (
                                valutazione BETWEEN 1 AND 5
                                AND valutazione * 2 = TRUNC(valutazione * 2)
                            ),
                            CONSTRAINT chk_recensioni_versione_non_negativa CHECK (versione >= 0),
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
                                          REFERENCES Utenti(id_utente) ON DELETE RESTRICT
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
-- 6. ESTENSIONI E INDICI
-- ==============================================================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE INDEX IF NOT EXISTS idx_ristoranti_gestore ON RistorantiTheKnife(id_gestore);
CREATE INDEX IF NOT EXISTS idx_ristoranti_citta ON RistorantiTheKnife(LOWER(citta));
CREATE INDEX IF NOT EXISTS idx_rt_tipologia ON Ristoranti_Tipologie(id_tipologia);
CREATE INDEX IF NOT EXISTS idx_recensioni_utente ON Recensioni(id_utente);
CREATE INDEX IF NOT EXISTS idx_preferiti_ristorante ON Preferiti(id_ristorante);
CREATE INDEX IF NOT EXISTS idx_risposte_gestore ON Risposte_Recensioni(id_gestore);

CREATE INDEX IF NOT EXISTS idx_ristoranti_nome_trgm
    ON RistorantiTheKnife USING GIN (LOWER(nome) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_ristoranti_indirizzo_trgm
    ON RistorantiTheKnife USING GIN (LOWER(indirizzo) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_ristoranti_citta_trgm
    ON RistorantiTheKnife USING GIN (LOWER(citta) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_ristoranti_nazione_trgm
    ON RistorantiTheKnife USING GIN (LOWER(nazione) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_tipologia_cucina_nome_trgm
    ON Tipologia_Cucina USING GIN (LOWER(nome) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ristoranti_posizione_gist
    ON RistorantiTheKnife USING GIST (
        (ST_SetSRID(
            ST_MakePoint(longitudine::double precision, latitudine::double precision),
            4326
        )::geography)
    );

CREATE INDEX IF NOT EXISTS idx_recensioni_ristorante_valutazione
    ON Recensioni (id_ristorante, valutazione);