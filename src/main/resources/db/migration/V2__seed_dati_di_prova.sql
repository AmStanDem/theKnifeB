-- ==============================================================================
-- THE KNIFE - SEED DATI DI PROVA E RELAZIONI N:M (V2)
-- ==============================================================================

-- 0. PULIZIA IDEMPOTENTE
-- Svuota le tabelle e resetta i contatori SERIAL senza distruggere la struttura
TRUNCATE TABLE Utenti RESTART IDENTITY CASCADE;
TRUNCATE TABLE Tipologia_Cucina RESTART IDENTITY CASCADE;

-- ==============================================================================
-- 1. UTENTI
-- password: password123
-- ==============================================================================
INSERT INTO Utenti (email, password, nome, cognome, data_nascita, domicilio, ruolo) VALUES
                                                                                        ('massimo.bottura@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Massimo', 'Bottura', '1962-09-30', 'Modena', 'gestore'),
                                                                                        ('gordon.ramsay@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Gordon', 'Ramsay', '1966-11-08', 'Londra', 'gestore'),
                                                                                        ('mario.rossi@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Mario', 'Rossi', '2001-05-20', 'Milano', 'cliente'),
                                                                                        ('alberto.cliente@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Alberto', 'Neri', '1999-03-12', 'Varese', 'cliente');

-- ==============================================================================
-- 2. RISTORANTI THE KNIFE
-- ==============================================================================
INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio, delivery, booking_online) VALUES
                                                                                                                                                  (1, 'Osteria Francescana', 'Via Stella 22', 'Modena', 'Italia', 44.6447600, 10.9202500, 320.00, FALSE, TRUE),
                                                                                                                                                  (1, 'Franceschetta58', 'Strada Vignolese 58', 'Modena', 'Italia', 44.6393400, 10.9388300, 75.00, TRUE, TRUE),
                                                                                                                                                  (2, 'Restaurant Gordon Ramsay', '68 Royal Hospital Rd', 'Londra', 'Regno Unito', 51.4854500, -0.1620200, 210.00, FALSE, TRUE),
                                                                                                                                                  (2, 'Street Burger', 'St Paul''s', 'Londra', 'Regno Unito', 51.5140000, -0.0980000, 25.00, TRUE, FALSE);

-- ==============================================================================
-- 3. DOMINIO CUCINE (Popolamento Dizionario)
-- ==============================================================================
INSERT INTO Tipologia_Cucina (nome) VALUES
                                        ('Contemporanea'),
                                        ('Emiliana'),
                                        ('Francese'),
                                        ('Hamburger'),
                                        ('Creativa'),
                                        ('Fast Food');

-- ==============================================================================
-- 4. RELAZIONI RISTORANTI - CUCINE (N:M)
-- ==============================================================================
INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) VALUES
                                                                   (1, 1), -- Osteria Francescana -> Contemporanea
                                                                   (1, 5), -- Osteria Francescana -> Creativa (Esempio di cucina multipla)
                                                                   (2, 2), -- Franceschetta58 -> Emiliana
                                                                   (3, 1), -- Gordon Ramsay -> Contemporanea
                                                                   (3, 3), -- Gordon Ramsay -> Francese
                                                                   (4, 4), -- Street Burger -> Hamburger
                                                                   (4, 6); -- Street Burger -> Fast Food

-- ==============================================================================
-- 5. RECENSIONI
-- ==============================================================================
INSERT INTO Recensioni (id_utente, id_ristorante, valutazione, testo) VALUES
                                                                          (3, 1, 5, 'Un percorso degustazione incredibile, l''anguilla risalendo il fiume Po è geniale.'),
                                                                          (3, 4, 3, 'Hamburger buono ma servizio troppo frettoloso, c''era molta confusione.'),
                                                                          (4, 2, 4, 'Ottima reinterpretazione della tradizione emiliana. Tortellini perfetti.');

-- ==============================================================================
-- 6. RISPOSTE DEI GESTORI
-- ==============================================================================
INSERT INTO Risposte_Recensioni (id_recensione, id_gestore, testo) VALUES
                                                                       (1, 1, 'Grazie infinite per le tue parole, ti aspettiamo presto in Via Stella.'),
                                                                       (2, 2, 'Siamo spiacenti per l''attesa, stiamo potenziando lo staff per gestire meglio i picchi orari.');

-- ==============================================================================
-- 7. PREFERITI
-- ==============================================================================
INSERT INTO Preferiti (id_utente, id_ristorante) VALUES
                                                     (3, 1),
                                                     (3, 2),
                                                     (4, 1);