ALTER TABLE Recensioni
    ALTER COLUMN valutazione TYPE NUMERIC(2, 1)
        USING valutazione::NUMERIC(2, 1);

ALTER TABLE Recensioni
    DROP CONSTRAINT chk_valutazione;

ALTER TABLE Recensioni
    ADD CONSTRAINT chk_valutazione
        CHECK (
            valutazione BETWEEN 1 AND 5
            AND valutazione * 2 = TRUNC(valutazione * 2)
        );
