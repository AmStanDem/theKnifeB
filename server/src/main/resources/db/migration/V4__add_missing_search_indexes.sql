CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE INDEX IF NOT EXISTS idx_risposte_gestore
    ON Risposte_Recensioni(id_gestore);

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

ALTER TABLE RistorantiTheKnife
    DROP CONSTRAINT IF EXISTS fk_ristorante_gestore,
    ADD CONSTRAINT fk_ristorante_gestore
        FOREIGN KEY (id_gestore) REFERENCES Utenti(id_utente) ON DELETE RESTRICT;

ALTER TABLE Risposte_Recensioni
    DROP CONSTRAINT IF EXISTS fk_risposta_gestore,
    ADD CONSTRAINT fk_risposta_gestore
        FOREIGN KEY (id_gestore) REFERENCES Utenti(id_utente) ON DELETE RESTRICT;
