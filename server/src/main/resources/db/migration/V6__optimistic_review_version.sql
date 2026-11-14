ALTER TABLE Recensioni
    ADD COLUMN versione INTEGER NOT NULL DEFAULT 0
        CONSTRAINT chk_recensioni_versione_non_negativa CHECK (versione >= 0);
