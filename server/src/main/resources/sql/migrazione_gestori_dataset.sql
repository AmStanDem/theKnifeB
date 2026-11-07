-- Eseguire con psql dalla root del repository:
--   psql "$DATABASE_URL" -f server/src/main/resources/sql/migrazione_gestori_dataset.sql
-- La procedura abbina i record al CSV originale usando nome, indirizzo,
-- località e coordinate; modifica solo corrispondenze univoche.

\set ON_ERROR_STOP on
\prompt 'ID utente del gestore temporaneo usato per importare il CSV: ' id_gestore_fittizio

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

LOCK TABLE Utenti, RistorantiTheKnife, Recensioni, Risposte_Recensioni
    IN SHARE ROW EXCLUSIVE MODE;

CREATE TEMP TABLE migrazione_parametri (
    id_gestore_fittizio INT NOT NULL
) ON COMMIT DROP;

INSERT INTO migrazione_parametri VALUES (:'id_gestore_fittizio'::integer);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM Utenti u
        JOIN migrazione_parametri p ON p.id_gestore_fittizio = u.id_utente
        WHERE u.ruolo = 'gestore'
    ) THEN
        RAISE EXCEPTION 'L''ID indicato non corrisponde a un account gestore';
    END IF;
END $$;

CREATE TEMP TABLE csv_dataset (
    source_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome TEXT,
    indirizzo TEXT,
    localita TEXT,
    prezzo TEXT,
    cucine TEXT,
    longitudine TEXT,
    latitudine TEXT,
    telefono TEXT,
    url TEXT,
    sito_web TEXT,
    premio TEXT,
    stella_verde TEXT,
    servizi TEXT,
    descrizione TEXT
) ON COMMIT DROP;

\copy csv_dataset (nome, indirizzo, localita, prezzo, cucine, longitudine, latitudine, telefono, url, sito_web, premio, stella_verde, servizi, descrizione) FROM 'server/src/main/resources/dataset/michelin_my_maps_clean.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')

CREATE TEMP TABLE corrispondenze ON COMMIT DROP AS
SELECT
    s.source_id,
    r.id_ristorante,
    COALESCE(NULLIF(BTRIM(SPLIT_PART(s.localita, ',', 1)), ''), 'Sconosciuta') AS nuova_citta,
    COALESCE(NULLIF(BTRIM(SPLIT_PART(s.localita, ',', 2)), ''), 'Non specificata') AS nuova_nazione
FROM csv_dataset s
CROSS JOIN migrazione_parametri p
JOIN RistorantiTheKnife r
  ON LOWER(BTRIM(r.nome)) =
     LOWER(COALESCE(NULLIF(BTRIM(s.nome), ''), 'Sconosciuto'))
 AND LOWER(BTRIM(r.indirizzo)) =
     LOWER(COALESCE(NULLIF(BTRIM(s.indirizzo), ''), 'Indirizzo non disponibile'))
 AND LOWER(BTRIM(r.citta)) =
     LOWER(COALESCE(NULLIF(BTRIM(s.localita), ''), 'Sconosciuta'))
 AND ROUND(r.longitudine, 7) = ROUND(NULLIF(BTRIM(s.longitudine), '')::numeric, 7)
 AND ROUND(r.latitudine, 7) = ROUND(NULLIF(BTRIM(s.latitudine), '')::numeric, 7)
 AND r.id_gestore = p.id_gestore_fittizio;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM corrispondenze) THEN
        RAISE EXCEPTION 'Nessun ristorante riconciliato; nessun dato è stato modificato';
    END IF;

    IF EXISTS (
        SELECT source_id FROM corrispondenze
        GROUP BY source_id HAVING COUNT(*) > 1
    ) OR EXISTS (
        SELECT id_ristorante FROM corrispondenze
        GROUP BY id_ristorante HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Riconciliazione ambigua: almeno una corrispondenza non è univoca';
    END IF;
END $$;

CREATE TEMP TABLE migrazione_target (
    id_ristorante INT PRIMARY KEY,
    id_gestore_attuale INT NOT NULL,
    nuova_citta TEXT NOT NULL,
    nuova_nazione TEXT NOT NULL,
    nuovo_gestore INT
) ON COMMIT DROP;

INSERT INTO migrazione_target (
    id_ristorante, id_gestore_attuale, nuova_citta, nuova_nazione
)
SELECT c.id_ristorante, p.id_gestore_fittizio, c.nuova_citta, c.nuova_nazione
FROM corrispondenze c
CROSS JOIN migrazione_parametri p;

CREATE TEMP TABLE aree_dataset ON COMMIT DROP AS
SELECT
    LOWER(BTRIM(t.nuova_nazione)) AS nazione_key,
    LOWER(BTRIM(t.nuova_citta)) AS citta_key,
    MIN(t.nuova_citta) AS citta,
    'dataset.manager.' || MD5(
        LOWER(BTRIM(t.nuova_nazione)) || '|' || LOWER(BTRIM(t.nuova_citta))
    ) || '@example.invalid' AS email
FROM migrazione_target t
GROUP BY 1, 2;

INSERT INTO Utenti (email, password, nome, cognome, domicilio, ruolo)
SELECT
    a.email,
    crypt(gen_random_uuid()::text, gen_salt('bf', 12)),
    'Gestore',
    'Dataset',
    a.citta,
    'gestore'
FROM aree_dataset a
ON CONFLICT (email) DO NOTHING;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM aree_dataset a
        LEFT JOIN Utenti u ON u.email = a.email
        WHERE u.id_utente IS NULL
           OR u.ruolo <> 'gestore'
           OR u.nome <> 'Gestore'
           OR u.cognome <> 'Dataset'
    ) THEN
        RAISE EXCEPTION 'Collisione email: account esistente non riconosciuto come account dataset';
    END IF;
END $$;

UPDATE migrazione_target t
SET nuovo_gestore = u.id_utente
FROM aree_dataset a
JOIN Utenti u ON u.email = a.email
WHERE a.nazione_key = LOWER(BTRIM(t.nuova_nazione))
  AND a.citta_key = LOWER(BTRIM(t.nuova_citta));

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM migrazione_target WHERE nuovo_gestore IS NULL
    ) THEN
        RAISE EXCEPTION 'Alcuni ristoranti non hanno un gestore di area';
    END IF;
END $$;

-- Aggiorna solo risposte scritte dall'account temporaneo; risposte già associate
-- ad altri gestori restano invariate.
UPDATE Risposte_Recensioni rr
SET id_gestore = t.nuovo_gestore
FROM Recensioni rec
JOIN migrazione_target t ON t.id_ristorante = rec.id_ristorante
WHERE rr.id_recensione = rec.id_recensione
  AND rr.id_gestore = t.id_gestore_attuale;

UPDATE RistorantiTheKnife r
SET id_gestore = t.nuovo_gestore,
    citta = t.nuova_citta,
    nazione = t.nuova_nazione
FROM migrazione_target t
WHERE r.id_ristorante = t.id_ristorante
  AND r.id_gestore = t.id_gestore_attuale;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM migrazione_target t
        JOIN RistorantiTheKnife r USING (id_ristorante)
        WHERE r.id_gestore <> t.nuovo_gestore
    ) THEN
        RAISE EXCEPTION 'Verifica finale fallita: ristoranti rimasti non riassegnati';
    END IF;
END $$;

SELECT
    COUNT(*) AS ristoranti_riassegnati,
    COUNT(DISTINCT nuovo_gestore) AS gestori_dataset_creati
FROM migrazione_target;

SELECT COUNT(*) AS ristoranti_non_riconosciuti_ancora_al_gestore_origine
FROM RistorantiTheKnife r
CROSS JOIN migrazione_parametri p
WHERE r.id_gestore = p.id_gestore_fittizio
  AND NOT EXISTS (
      SELECT 1
      FROM migrazione_target t
      WHERE t.id_ristorante = r.id_ristorante
  );

-- Il gestore temporaneo non viene cancellato automaticamente: potrebbe avere
-- ristoranti o risposte non provenienti dal CSV.
COMMIT;
