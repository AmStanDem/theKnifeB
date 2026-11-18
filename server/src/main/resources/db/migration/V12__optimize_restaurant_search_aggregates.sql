CREATE INDEX IF NOT EXISTS idx_recensioni_ristorante_valutazione
    ON Recensioni (id_ristorante, valutazione);

DROP INDEX IF EXISTS idx_recensioni_ristorante;
