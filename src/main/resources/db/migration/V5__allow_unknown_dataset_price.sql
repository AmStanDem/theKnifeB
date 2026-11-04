ALTER TABLE RistorantiTheKnife
    ALTER COLUMN prezzo_medio DROP NOT NULL;

UPDATE RistorantiTheKnife r
SET prezzo_medio = NULL
FROM Utenti u
WHERE u.id_utente = r.id_gestore
  AND u.email LIKE 'dataset.manager.%@example.invalid'
  AND u.nome = 'Gestore'
  AND u.cognome = 'Dataset';

INSERT INTO Tipologia_Cucina (nome)
SELECT 'Japanese'
WHERE EXISTS (
    SELECT 1
    FROM RistorantiTheKnife r
    JOIN Utenti u ON u.id_utente = r.id_gestore
    WHERE r.nome = 'Tokumaru'
      AND r.indirizzo = '406 Kameyacho, Nakagyo-ku, Kyoto, 604-0941, Japan'
      AND u.email LIKE 'dataset.manager.%@example.invalid'
)
ON CONFLICT (nome) DO NOTHING;

DELETE FROM Ristoranti_Tipologie rt
USING RistorantiTheKnife r, Utenti u, Tipologia_Cucina t
WHERE rt.id_ristorante = r.id_ristorante
  AND r.id_gestore = u.id_utente
  AND rt.id_tipologia = t.id_tipologia
  AND r.nome = 'Tokumaru'
  AND r.indirizzo = '406 Kameyacho, Nakagyo-ku, Kyoto, 604-0941, Japan'
  AND u.email LIKE 'dataset.manager.%@example.invalid'
  AND t.nome <> 'Japanese';

INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia)
SELECT r.id_ristorante, t.id_tipologia
FROM RistorantiTheKnife r
JOIN Utenti u ON u.id_utente = r.id_gestore
CROSS JOIN Tipologia_Cucina t
WHERE r.nome = 'Tokumaru'
  AND r.indirizzo = '406 Kameyacho, Nakagyo-ku, Kyoto, 604-0941, Japan'
  AND u.email LIKE 'dataset.manager.%@example.invalid'
  AND t.nome = 'Japanese'
ON CONFLICT DO NOTHING;
