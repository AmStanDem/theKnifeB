-- ==============================================================================
-- THE KNIFE - SEED DATI MICHELIN & TEST (V2)
-- ==============================================================================

-- ==============================================================================
-- 1. UTENTI (Gestori reali e Clienti fittizi)
-- Password per tutti: password123
-- ==============================================================================
INSERT INTO Utenti (email, password, nome, cognome, data_nascita, domicilio, ruolo) VALUES
                                                                                        ('massimo.bottura@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Massimo', 'Bottura', '1962-09-30', 'Modena', 'gestore'),
                                                                                        ('gordon.ramsay@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Gordon', 'Ramsay', '1966-11-08', 'Londra', 'gestore'),
                                                                                        ('alessandro.cliente@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Alessandro', 'Rossi', '2001-05-20', 'Luino', 'cliente'),
                                                                                        ('giovanni.cliente@theknife.it', '$2a$12$D2M/O3T3sW9XQG9lH9wR6e.t7Wc3J/U1lP8tN0V3yJ2bV0Z5c/Cqe', 'Giovanni', 'Bianchi', '2000-08-14', 'Varese', 'cliente');

-- ==============================================================================
-- 2. RISTORANTI THE KNIFE (Estratti dal dominio Michelin)
-- Coordinate geografiche reali per testare algoritmi di calcolo delle distanze
-- ==============================================================================
INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, tipo_cucina, prezzo_medio, delivery, booking_online) VALUES
-- Ristoranti di Massimo Bottura (id_gestore = 1)
(1, 'Osteria Francescana', 'Via Stella 22', 'Modena', 'Italia', 44.6447600, 10.9202500, 'Contemporanea', 320.00, FALSE, TRUE),
(1, 'Franceschetta58', 'Strada Vignolese 58', 'Modena', 'Italia', 44.6393400, 10.9388300, 'Emiliana', 75.00, TRUE, TRUE),

-- Ristoranti di Gordon Ramsay (id_gestore = 2)
(2, 'Restaurant Gordon Ramsay', '68 Royal Hospital Rd', 'Londra', 'Regno Unito', 51.4854500, -0.1620200, 'Francese', 210.00, FALSE, TRUE),
(2, 'Pétrus by Gordon Ramsay', '1 Kinnerton St', 'Londra', 'Regno Unito', 51.4996800, -0.1554500, 'Europea', 150.00, FALSE, TRUE);

-- ==============================================================================
-- 3. RECENSIONI
-- ==============================================================================
INSERT INTO Recensioni (id_utente, id_ristorante, valutazione, testo) VALUES
                                                                          (3, 1, 5, 'Un''esperienza sensoriale che trascende la semplice ristorazione. Il "Camouflage" è un capolavoro assoluto.'),
                                                                          (3, 4, 4, 'Servizio impeccabile e cantina dei vini straordinaria, anche se l''acustica della sala potrebbe essere migliorata.'),
                                                                          (4, 2, 3, 'Ottimi i tortellini, ma le porzioni del menù degustazione sono decisamente troppo modeste rispetto al prezzo.');

-- ==============================================================================
-- 4. RISPOSTE DEI GESTORI
-- ==============================================================================
INSERT INTO Risposte_Recensioni (id_recensione, id_gestore, testo) VALUES
                                                                       (1, 1, 'Grazie Thomas per aver colto l''essenza della nostra narrazione culinaria. Ti aspettiamo nuovamente in Via Stella.'),
                                                                       (3, 1, 'Ci spiace che le quantità non abbiano soddisfatto le tue aspettative. La nostra filosofia privilegia la concentrazione dei sapori.');

-- ==============================================================================
-- 5. PREFERITI
-- ==============================================================================
INSERT INTO Preferiti (id_utente, id_ristorante) VALUES
                                                     (3, 1),
                                                     (3, 2),
                                                     (4, 3);