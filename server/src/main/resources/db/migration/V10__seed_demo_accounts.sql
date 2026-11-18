-- Account dimostrativi aggiuntivi.
-- Password per tutti gli account: KnifeDemo2026!
-- Il valore salvato e' un hash BCrypt (work factor 12).
INSERT INTO Utenti (email, password, nome, cognome, data_nascita, domicilio, ruolo) VALUES
    ('cliente.demo1@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo1', '1995-04-12', 'Milano', 'cliente'),
    ('cliente.demo2@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo2', '1992-08-23', 'Varese', 'cliente'),
    ('cliente.demo3@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo3', '1998-01-05', 'Como', 'cliente'),
    ('cliente.demo4@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Cliente', 'Demo4', '1990-11-17', 'Busto Arsizio', 'cliente'),
    ('ristoratore.demo1@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo1', '1985-03-19', 'Milano', 'gestore'),
    ('ristoratore.demo2@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo2', '1980-07-08', 'Varese', 'gestore'),
    ('ristoratore.demo3@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo3', '1988-12-02', 'Como', 'gestore'),
    ('ristoratore.demo4@theknife.it', '$2a$12$P6HgPeYeu6PTByZZ3VNZ0Oetrc.0GTnMbv6NtwVBxnnIQknUKMk5m', 'Gestore', 'Demo4', '1983-05-26', 'Busto Arsizio', 'gestore')
ON CONFLICT (email) DO NOTHING;
