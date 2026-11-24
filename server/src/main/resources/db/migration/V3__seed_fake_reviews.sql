-- ==============================================================================
-- THE KNIFE - GENERAZIONE RECENSIONI FITTIZIE (V13)
-- ==============================================================================
-- Genera recensioni fittizie a partire dagli utenti clienti e ristoranti
-- presenti nel database, escludendo le coppie (utente, ristorante) già recensite
-- e rispettando il vincolo UNIQUE (id_utente, id_ristorante).
--
-- Include anche risposte dei gestori per alcune recensioni.
-- ==============================================================================

-- ==============================================================================
-- 1. RECENSIONI FITTIZIE
-- ==============================================================================
-- Per ogni cliente, genera una recensione per un ristorante diverso da quelli
-- già recensiti e non ancora coperto per quel cliente.
-- Le valutazioni includono mezze stelle; le date sono distribuite negli ultimi
-- mesi per simulare un'attività realistica.

WITH -- RIFERIMENTI CLIENTE
    clienti_disponibili AS (
        SELECT id_utente, nome, cognome FROM Utenti WHERE ruolo = 'cliente'
    ),
    ristoranti_disponibili AS (
        SELECT id_ristorante, nome FROM RistorantiTheKnife
    ),
    coppie_esistenti AS (
        SELECT id_utente, id_ristorante FROM Recensioni
    ),
    -- Ogni cliente recensisce un ristorante casuale che non ha già recensito
    nuove_recensioni AS (
        SELECT
            c.id_utente,
            r.id_ristorante,
            -- Valutazioni con mezze stelle realistiche
            CASE
                WHEN c.id_utente = 3 AND r.id_ristorante = 2 THEN 4.5
                WHEN c.id_utente = 3 AND r.id_ristorante = 3 THEN 5.0
                WHEN c.id_utente = 3 AND r.id_ristorante = 4 THEN 3.5
                WHEN c.id_utente = 4 AND r.id_ristorante = 1 THEN 4.0
                WHEN c.id_utente = 4 AND r.id_ristorante = 3 THEN 4.5
                WHEN c.id_utente = 4 AND r.id_ristorante = 4 THEN 3.0
                WHEN c.id_utente = 5 AND r.id_ristorante = 1 THEN 5.0
                WHEN c.id_utente = 5 AND r.id_ristorante = 2 THEN 4.0
                WHEN c.id_utente = 5 AND r.id_ristorante = 3 THEN 4.5
                WHEN c.id_utente = 5 AND r.id_ristorante = 4 THEN 2.5
                WHEN c.id_utente = 6 AND r.id_ristorante = 1 THEN 3.5
                WHEN c.id_utente = 6 AND r.id_ristorante = 2 THEN 5.0
                WHEN c.id_utente = 6 AND r.id_ristorante = 3 THEN 4.0
                WHEN c.id_utente = 6 AND r.id_ristorante = 4 THEN 3.5
                WHEN c.id_utente = 7 AND r.id_ristorante = 1 THEN 4.5
                WHEN c.id_utente = 7 AND r.id_ristorante = 2 THEN 3.5
                WHEN c.id_utente = 7 AND r.id_ristorante = 3 THEN 5.0
                WHEN c.id_utente = 7 AND r.id_ristorante = 4 THEN 4.0
                WHEN c.id_utente = 8 AND r.id_ristorante = 1 THEN 3.0
                WHEN c.id_utente = 8 AND r.id_ristorante = 2 THEN 4.5
                WHEN c.id_utente = 8 AND r.id_ristorante = 3 THEN 2.0
                WHEN c.id_utente = 8 AND r.id_ristorante = 4 THEN 4.0
            END AS valutazione,
            -- Testi realistici delle recensioni
            CASE
                -- Mario Rossi (id=3)
                WHEN c.id_utente = 3 AND r.id_ristorante = 2
                    THEN 'Tortellini in brodo eccezionali, mi hanno riportato ai sapori della nonna. Prezzo onesto per la qualità offerta. Tornerò sicuramente.'
                WHEN c.id_utente = 3 AND r.id_ristorante = 3
                    THEN 'Un''esperienza culinaria senza pari. Il menu degustazione è un viaggio attraverso sapori sorprendenti. Servizio impeccabile, ogni piatto era una scoperta.'
                WHEN c.id_utente = 3 AND r.id_ristorante = 4
                    THEN 'Buon hamburger, carne di qualità. Però per un fast food i prezzi sono un po'' alti. Le patatine erano eccellenti.'
                -- Alberto Neri (id=4)
                WHEN c.id_utente = 4 AND r.id_ristorante = 1
                    THEN 'Location elegante e piatti raffinati. Ho apprezzato molto l''abbinamento vini proposto dal sommelier. Unico neo: l''attesa tra le portate.'
                WHEN c.id_utente = 4 AND r.id_ristorante = 3
                    THEN 'Semplicemente strepitoso. Il roast beef era perfetto, cottura impeccabile. Un ristorante che merita tutte le stelle che ha.'
                WHEN c.id_utente = 4 AND r.id_ristorante = 4
                    THEN 'Panino buono ma niente di trascendentale. Per mangiare in piedi su un tavolino non ne vale la pena. Consegna rapida.'
                -- Cliente Demo1 (id=5)
                WHEN c.id_utente = 5 AND r.id_ristorante = 1
                    THEN 'Una serata indimenticabile! Il risotto ai porcini era divino e il servizio attento a ogni dettaglio. Consigliatissimo per occasioni speciali.'
                WHEN c.id_utente = 5 AND r.id_ristorante = 2
                    THEN 'Cucina casalinga fatta con amore. I gnocchi al ragù sono i migliori che abbia mai mangiato. Ambiente familiare e accogliente.'
                WHEN c.id_utente = 5 AND r.id_ristorante = 3
                    THEN 'Mai mangiato così bene a Londra. Il dessert al cioccolato era pura poesia. Prezzo alto ma giustificato dalla qualità.'
                WHEN c.id_utente = 5 AND r.id_ristorante = 4
                    THEN 'Hamburger asciutto e patatine mollicce. Non era la loro serata migliore. Il servizio però è stato veloce e gentile.'
                -- Cliente Demo2 (id=6)
                WHEN c.id_utente = 6 AND r.id_ristorante = 1
                    THEN 'Buono ma non eccezionale per il prezzo. I sapori ci sono ma mi aspettavo di più considerando la reputazione. Servizio cortese.'
                WHEN c.id_utente = 6 AND r.id_ristorante = 2
                    THEN 'Semplicemente perfetto. Lasagne fatte come si deve, strato dopo strato. Un vero gioiello della tradizione emiliana.'
                WHEN c.id_utente = 6 AND r.id_ristorante = 3
                    THEN 'Ambiente raffinato, personale preparato. Ho apprezzato la carta dei vini molto ben fornita. Esperienza da ripetere.'
                WHEN c.id_utente = 6 AND r.id_ristorante = 4
                    THEN 'Buon rapporto qualità-prezzo. Il cheeseburger classico è solido e soddisfacente. Per una pausa pranzo veloce va benissimo.'
                -- Cliente Demo3 (id=7)
                WHEN c.id_utente = 7 AND r.id_ristorante = 1
                    THEN 'Piatti creativi e presentazione curatissima. Il foie gras con riduzione di melograno era un''esplosione di gusto. Un''eccellenza.'
                WHEN c.id_utente = 7 AND r.id_ristorante = 2
                    THEN 'Buona trattoria, porzioni generose. Il tagliere di salumi locali merita tanto. Il vino della casa è piacevole.'
                WHEN c.id_utente = 7 AND r.id_ristorante = 3
                    THEN 'Esperienza stellare dall''inizio alla fine. Il servizio è impeccabile, ogni portata racconta una storia. Un must per gli appassionati.'
                WHEN c.id_utente = 7 AND r.id_ristorante = 4
                    THEN 'Hamburger vegetariano gustoso e ben bilanciato. Opzione senza glutine disponibile. Locale pulito e personale simpatico.'
                -- Cliente Demo4 (id=8)
                WHEN c.id_utente = 8 AND r.id_ristorante = 1
                    THEN 'Prezzo troppo alto per quello che offre. Il piatto principale era freddo e abbiamo aspettato 40 minuti. Ci sono alternative migliori.'
                WHEN c.id_utente = 8 AND r.id_ristorante = 2
                    THEN 'Acqua in bocca! Le tagliatelle al tartufo sono pura poesia. Prezzo onesto, ambiente caloroso. Diventerà il mio ristorante di fiducia.'
                WHEN c.id_utente = 8 AND r.id_ristorante = 3
                    THEN 'Non all''altezza delle aspettative. Porzioni minuscole e prezzo esorbitante. Il servizio era freddo e distaccato. Peccato.'
                WHEN c.id_utente = 8 AND r.id_ristorante = 4
                    THEN 'Il miglior fast food della zona. Ingredienti freschi, carne saporita, panino morbido. Le patatine speziate sono fantastiche!'
            END AS testo,
            -- Date distribuite negli ultimi 12 mesi
            CASE
                WHEN c.id_utente = 3 AND r.id_ristorante = 2 THEN CURRENT_TIMESTAMP - INTERVAL '10 days'
                WHEN c.id_utente = 3 AND r.id_ristorante = 3 THEN CURRENT_TIMESTAMP - INTERVAL '2 months'
                WHEN c.id_utente = 3 AND r.id_ristorante = 4 THEN CURRENT_TIMESTAMP - INTERVAL '3 months'
                WHEN c.id_utente = 4 AND r.id_ristorante = 1 THEN CURRENT_TIMESTAMP - INTERVAL '15 days'
                WHEN c.id_utente = 4 AND r.id_ristorante = 3 THEN CURRENT_TIMESTAMP - INTERVAL '20 days'
                WHEN c.id_utente = 4 AND r.id_ristorante = 4 THEN CURRENT_TIMESTAMP - INTERVAL '4 months'
                WHEN c.id_utente = 5 AND r.id_ristorante = 1 THEN CURRENT_TIMESTAMP - INTERVAL '5 days'
                WHEN c.id_utente = 5 AND r.id_ristorante = 2 THEN CURRENT_TIMESTAMP - INTERVAL '1 month'
                WHEN c.id_utente = 5 AND r.id_ristorante = 3 THEN CURRENT_TIMESTAMP - INTERVAL '6 months'
                WHEN c.id_utente = 5 AND r.id_ristorante = 4 THEN CURRENT_TIMESTAMP - INTERVAL '7 months'
                WHEN c.id_utente = 6 AND r.id_ristorante = 1 THEN CURRENT_TIMESTAMP - INTERVAL '2 months'
                WHEN c.id_utente = 6 AND r.id_ristorante = 2 THEN CURRENT_TIMESTAMP - INTERVAL '45 days'
                WHEN c.id_utente = 6 AND r.id_ristorante = 3 THEN CURRENT_TIMESTAMP - INTERVAL '8 months'
                WHEN c.id_utente = 6 AND r.id_ristorante = 4 THEN CURRENT_TIMESTAMP - INTERVAL '9 months'
                WHEN c.id_utente = 7 AND r.id_ristorante = 1 THEN CURRENT_TIMESTAMP - INTERVAL '3 months'
                WHEN c.id_utente = 7 AND r.id_ristorante = 2 THEN CURRENT_TIMESTAMP - INTERVAL '14 days'
                WHEN c.id_utente = 7 AND r.id_ristorante = 3 THEN CURRENT_TIMESTAMP - INTERVAL '10 months'
                WHEN c.id_utente = 7 AND r.id_ristorante = 4 THEN CURRENT_TIMESTAMP - INTERVAL '11 months'
                WHEN c.id_utente = 8 AND r.id_ristorante = 1 THEN CURRENT_TIMESTAMP - INTERVAL '6 days'
                WHEN c.id_utente = 8 AND r.id_ristorante = 2 THEN CURRENT_TIMESTAMP - INTERVAL '3 days'
                WHEN c.id_utente = 8 AND r.id_ristorante = 3 THEN CURRENT_TIMESTAMP - INTERVAL '12 days'
                WHEN c.id_utente = 8 AND r.id_ristorante = 4 THEN CURRENT_TIMESTAMP - INTERVAL '5 months'
            END AS data_creazione
        FROM clienti_disponibili c
        CROSS JOIN ristoranti_disponibili r
        WHERE NOT EXISTS (
            SELECT 1 FROM coppie_esistenti e
            WHERE e.id_utente = c.id_utente AND e.id_ristorante = r.id_ristorante
        )
    )
INSERT INTO Recensioni (id_utente, id_ristorante, valutazione, testo, data_creazione)
SELECT id_utente, id_ristorante, valutazione, testo, data_creazione
FROM nuove_recensioni
WHERE valutazione IS NOT NULL
ORDER BY id_utente, id_ristorante;

-- ==============================================================================
-- 2. RISPOSTE DEI GESTORI
-- ==============================================================================
-- Alcune recensioni ricevono una risposta dal gestore, simulando un'interazione
-- realistica. Usiamo i gestori associati al ristorante.

WITH -- MAPPA RISTORANTE -> GESTORE
    recensioni_da_rispondere AS (
        SELECT
            rec.id_recensione,
            rc.id_gestore,
            rec.valutazione,
            ROW_NUMBER() OVER (
                PARTITION BY
                    CASE
                        WHEN rec.valutazione >= 4.5 THEN 'alta'
                        WHEN rec.valutazione >= 4.0 THEN 'medio_alta'
                        WHEN rec.valutazione < 3.0 THEN 'bassa'
                        ELSE 'altre'
                    END
                ORDER BY rec.id_recensione
            ) AS rn,
            CASE
                WHEN rec.valutazione >= 4.5 THEN 'alta'
                WHEN rec.valutazione >= 4.0 THEN 'medio_alta'
                WHEN rec.valutazione < 3.0 THEN 'bassa'
                ELSE 'altre'
            END AS fascia
        FROM Recensioni rec
        JOIN RistorantiTheKnife rc ON rc.id_ristorante = rec.id_ristorante
        LEFT JOIN Risposte_Recensioni risp ON risp.id_recensione = rec.id_recensione
        WHERE risp.id_recensione IS NULL
    ),
    recensioni_selezionate AS (
        SELECT id_recensione, id_gestore, fascia
        FROM recensioni_da_rispondere
        WHERE (fascia = 'alta' AND rn <= 5)
           OR (fascia = 'medio_alta' AND rn <= 4)
           OR (fascia = 'bassa' AND rn <= 3)
    )
INSERT INTO Risposte_Recensioni (id_recensione, id_gestore, testo)
SELECT
    id_recensione,
    id_gestore,
    CASE
        WHEN fascia = 'alta'
            THEN 'Grazie di cuore per le splendide parole! Lavoriamo ogni giorno con passione per offrire il meglio ai nostri ospiti. Ci vediamo presto!'
        WHEN fascia = 'medio_alta'
            THEN 'Ti ringraziamo per la recensione positiva! Siamo felici che la tua esperienza sia stata piacevole. Ti aspettiamo per farti scoprire le altre specialità del nostro menu.'
        WHEN fascia = 'bassa'
            THEN 'Ci dispiace molto che la tua esperienza non sia stata all''altezza delle aspettative. Il tuo feedback è prezioso e ci aiuterà a migliorare. Ti invitiamo a contattarci direttamente per parlarne.'
    END
FROM recensioni_selezionate;