-- Password comune agli utenti di prova: password123
UPDATE Utenti
SET password = '$2a$12$mJR.c1CEAmmovy.fdnaO4OkPAm6AS3Q7.Tz1HlFO7/6HOf2Wj5Vwm'
WHERE email IN (
    'massimo.bottura@theknife.it',
    'gordon.ramsay@theknife.it',
    'mario.rossi@theknife.it',
    'alberto.cliente@theknife.it'
);
