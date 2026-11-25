-- ==============================================================================
-- THE KNIFE - DATI DI PROVA (V2 consolidato)
-- ==============================================================================
-- Include gli inserimenti dati da V2, V9 (fix password), V10 (account demo).
-- ==============================================================================

-- ==============================================================================
-- 1. UTENTI
-- ==============================================================================
-- Password comune per gli utenti di prova: KnifeDemo2026!
INSERT INTO Utenti (email, password, nome, cognome, data_nascita, domicilio, ruolo) VALUES
                                                                                        ('massimo.bottura@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Massimo', 'Bottura', '1962-09-30', 'Modena', 'gestore'),
                                                                                        ('gordon.ramsay@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gordon', 'Ramsay', '1966-11-08', 'Londra', 'gestore'),
                                                                                        ('mario.rossi@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Mario', 'Rossi', '2001-05-20', 'Milano', 'cliente'),
                                                                                        ('alberto.cliente@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Alberto', 'Neri', '1999-03-12', 'Varese', 'cliente');

-- Account dimostrativi aggiuntivi. Password: KnifeDemo2026!
INSERT INTO Utenti (email, password, nome, cognome, data_nascita, domicilio, ruolo) VALUES
    ('cliente.demo1@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo1', '1995-04-12', 'Milano', 'cliente'),
    ('cliente.demo2@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo2', '1992-08-23', 'Varese', 'cliente'),
    ('cliente.demo3@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo3', '1998-01-05', 'Como', 'cliente'),
    ('cliente.demo4@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo4', '1990-11-17', 'Busto Arsizio', 'cliente'),
    ('ristoratore.demo1@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo1', '1985-03-19', 'Milano', 'gestore'),
    ('ristoratore.demo2@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo2', '1980-07-08', 'Varese', 'gestore'),
    ('ristoratore.demo3@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo3', '1988-12-02', 'Como', 'gestore'),
    ('ristoratore.demo4@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo4', '1983-05-26', 'Busto Arsizio', 'gestore');

-- ==============================================================================
-- 2. RISTORANTI THE KNIFE
-- ==============================================================================
INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio, delivery, booking_online) VALUES
                                                                                                                                                   (1, 'La Corte del Gusto', 'Via dei Tigli 14', 'Modena', 'Italia', 44.6451200, 10.9213400, 320.00, FALSE, TRUE),
                                                                                                                                                   (1, 'Trattoria Il Girasole', 'Via delle Magnolie 27', 'Modena', 'Italia', 44.6389200,  10.9391200, 75.00, TRUE, TRUE),
                                                                                                                                                   (2, 'Restaurant Gordon Ramsay', '68 Royal Hospital Rd', 'Londra', 'Regno Unito', 51.4854500, -0.1620200, 210.00, FALSE, TRUE),
                                                                                                                                                   (2, 'Street Burger', 'St Paul''s', 'Londra', 'Regno Unito', 51.5140000, -0.0980000, 25.00, TRUE, FALSE);

-- ==============================================================================
-- 3. DOMINIO CUCINE
-- ==============================================================================
INSERT INTO Tipologia_Cucina (nome) VALUES
                                        ('Contemporanea'),
                                        ('Emiliana'),
                                        ('Francese'),
                                        ('Hamburger'),
                                        ('Creativa'),
                                        ('Fast Food'),
                                        ('Japanese');

-- ==============================================================================
-- 4. RELAZIONI RISTORANTI - CUCINE (N:M)
-- ==============================================================================
INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) VALUES
                                                                    (1, 1),
                                                                    (1, 5),
                                                                    (2, 2);

-- ==============================================================================
-- 5. RECENSIONI
-- ==============================================================================
INSERT INTO Recensioni (id_utente, id_ristorante, valutazione, testo) VALUES
                                                                           (3, 1, 5, 'Un percorso degustazione incredibile, l''anguilla risalendo il fiume Po è geniale.'),
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