# TheKnife

## Manuale Utente

**Autori:** [DA COMPILARE: Riotto Thomas 760981 VA; Tullo Alessandro 760760 VA; Albé Giovanni 760117 VA; Pesavento Antonio 759933 VA]

**Data:** [DA COMPILARE: GG/MM/AAAA]

**Versione documento:** [DA COMPILARE: es. 1.0]

---

# Indice

1. [Installazione](#1-installazione)
   1. [Requisiti di sistema](#11-requisiti-di-sistema)
   2. [Setup dell'ambiente](#12-setup-dellambiente)
   3. [Installazione del programma](#13-installazione-del-programma)
2. [Esecuzione ed uso](#2-esecuzione-ed-uso)
   1. [Setup e lancio del programma](#21-setup-e-lancio-del-programma)
   2. [Uso delle funzionalità](#22-uso-delle-funzionalità)
   3. [Data set di test](#23-data-set-di-test)
3. [Limiti della soluzione sviluppata](#3-limiti-della-soluzione-sviluppata)
4. [Sitografia / Bibliografia](#4-sitografia--bibliografia)

---

# 1. Installazione

> Questa sezione deve essere comprensibile a chi non conosce Java, database o sviluppo software.

## 1.1 Requisiti di sistema

[DA COMPILARE: indicare sistemi operativi, memoria, spazio su disco e componenti necessarie.]

- Sistema operativo supportato: Windows, Linux o macOS
- Java 21 o successivo
- Docker Desktop o Docker Engine
- PostgreSQL 16 con estensione PostGIS
- Memoria RAM minima consigliata: [DA COMPILARE]
- Spazio libero su disco: [DA COMPILARE]
- Connessione di rete: [DA COMPILARE]

## 1.2 Setup dell'ambiente

[DA COMPILARE: descrivere in modo semplice come preparare il computer.]

### 1.2.1 Installazione di Java

1. Scaricare una versione di Java 21 o successiva.
2. Avviare il programma di installazione.
3. Accettare le impostazioni predefinite.
4. Riavviare il computer se richiesto.

![DA INSERIRE: screenshot del controllo della versione Java]

### 1.2.2 Preparazione del database

1. Copiare `.env.example` come `.env`.
2. Verificare che Docker sia avviato.
3. Aprire un terminale nella cartella principale del progetto.
4. Avviare il database:

```text
docker compose up -d db
```

![DA INSERIRE: screenshot di Docker con il database avviato]

## 1.3 Installazione del programma

[DA COMPILARE: indicare come ottenere e salvare i file dell'applicazione.]

1. Scaricare il file relativo al proprio sistema operativo:
   - Windows: `theknife-client-windows.jar`
   - Linux: `theknife-client-linux.jar`
   - macOS: `theknife-client-macos.jar`
2. Scaricare separatamente `theknife-server.jar`.
3. Posizionare i file in una cartella facilmente raggiungibile.
4. Non rinominare i file necessari al funzionamento.

![DA INSERIRE: screenshot della cartella contenente i file scaricati]

# 2. Esecuzione ed uso

## 2.1 Setup e lancio del programma

### 2.1.1 Avvio del database

1. Aprire un terminale nella cartella principale.
2. Avviare il database:

```text
docker compose up -d db
```

3. Attendere la conferma che il database sia in esecuzione.

### 2.1.2 Avvio del server

1. Aprire un secondo terminale.
2. Avviare il server:

```text
java -jar bin/theknife-server.jar
```

3. Attendere il completamento dell'avvio.
4. Lasciare aperta la finestra del server durante l'utilizzo.

### 2.1.3 Avvio dell'applicazione

1. Aprire il JAR client corrispondente al proprio sistema operativo.
2. In alternativa, usare il terminale:

```text
java -jar bin/theknife-client-<sistema>.jar
```

3. Attendere la comparsa della schermata iniziale.

![DA INSERIRE: screenshot della schermata iniziale]

### 2.1.4 Controllo iniziale

Verificare che:

- il database sia avviato;
- il server sia in ascolto;
- la schermata principale si apra;
- sia possibile cercare un ristorante;
- non vengano visualizzati errori di connessione.

## 2.2 Uso delle funzionalità

### 2.2.1 Modalità ospite

La modalità ospite consente di consultare i ristoranti senza creare un account.

**Procedura**

1. Aprire il programma.
2. Selezionare l'accesso come ospite.
3. Visualizzare i ristoranti disponibili.
4. Usare i filtri per restringere i risultati.

![DA INSERIRE: screenshot della modalità ospite]

### 2.2.2 Ricerca dei ristoranti

**Procedura**

1. Selezionare **Ricerca**.
2. Inserire o scegliere il luogo.
3. Impostare i filtri desiderati: cucina, fascia di prezzo, prenotazione o asporto.
4. Confermare la ricerca.
5. Selezionare un ristorante dalla lista.

![DA INSERIRE: screenshot del modulo di ricerca]

![DA INSERIRE: screenshot dei risultati]

### 2.2.3 Registrazione

**Procedura**

1. Selezionare **Registrati**.
2. Inserire email, password e dati richiesti.
3. Confermare la registrazione.
4. Accedere con le nuove credenziali.

![DA INSERIRE: screenshot del modulo di registrazione]

### 2.2.4 Accesso e area personale

**Procedura**

1. Selezionare **Accedi**.
2. Inserire email e password.
3. Confermare l'accesso.
4. Aprire l'area personale.

![DA INSERIRE: screenshot della schermata di login]

![DA INSERIRE: screenshot dell'area personale]

### 2.2.5 Modifica dell'account

1. Accedere al proprio account.
2. Aprire **Modifica account**.
3. Aggiornare i dati desiderati.
4. Salvare le modifiche.

![DA INSERIRE: screenshot del modulo di modifica account]

### 2.2.6 Lista dei preferiti

**Procedura**

1. Accedere con un account cliente.
2. Aprire la scheda di un ristorante.
3. Selezionare **Aggiungi ai preferiti**.
4. Aprire la sezione dei preferiti.
5. Rimuovere un ristorante dalla lista se necessario.

![DA INSERIRE: screenshot del pulsante preferiti]

![DA INSERIRE: screenshot della lista dei preferiti]

### 2.2.7 Scrittura di una recensione

**Procedura**

1. Accedere con un account cliente.
2. Aprire la scheda di un ristorante.
3. Selezionare la sezione recensioni.
4. Assegnare un voto da una a cinque stelle.
5. Inserire il commento.
6. Confermare l'invio.

![DA INSERIRE: screenshot del modulo recensione]

### 2.2.8 Consultazione delle recensioni

1. Aprire la scheda del ristorante.
2. Controllare il voto medio.
3. Leggere i commenti disponibili.

[DA COMPILARE: indicare se modifica e cancellazione delle recensioni sono disponibili nella versione consegnata.]

![DA INSERIRE: screenshot della sezione recensioni]

### 2.2.9 Funzionalità del ristoratore

**Procedura**

1. Accedere con un account ristoratore.
2. Aprire l'area personale o la sezione ristorante.
3. Visualizzare i ristoranti gestiti.
4. Consultare recensioni e voti medi.
5. Selezionare una recensione e inserire una risposta.

![DA INSERIRE: screenshot dell'area ristoratore]

![DA INSERIRE: screenshot del modulo di risposta]

### 2.2.10 Gestione del ristorante

[DA COMPILARE: descrivere tutte le operazioni disponibili al ristoratore.]

1. Accedere come ristoratore.
2. Aprire la sezione di gestione.
3. Inserire o modificare le informazioni richieste.
4. Salvare le modifiche.
5. Verificare che i dati aggiornati siano visibili.

![DA INSERIRE: screenshot della gestione del ristorante]

## 2.3 Data set di test

[DA COMPILARE: descrivere i dati forniti con il progetto.]

### 2.3.1 Account dimostrativi

| Tipo | Email | Password |
|---|---|---|
| Cliente | `cliente.demo1@theknife.it` | `KnifeDemo2026!` |
| Cliente | `cliente.demo2@theknife.it` | `KnifeDemo2026!` |
| Ristoratore | `ristoratore.demo1@theknife.it` | `KnifeDemo2026!` |
| Ristoratore | `ristoratore.demo2@theknife.it` | `KnifeDemo2026!` |

[DA COMPILARE: aggiungere gli altri account presenti nel database.]

### 2.3.2 Casi di prova suggeriti

| Funzionalità | Dati utilizzati | Risultato atteso |
|---|---|---|
| Registrazione | Nuovo account cliente | Accesso riuscito |
| Ricerca | Luogo e cucina selezionati | Elenco pertinente |
| Preferiti | Ristorante selezionato | Ristorante presente nei preferiti |
| Recensione | Account cliente e voto | Recensione salvata |
| Risposta | Account ristoratore | Risposta visibile alla recensione |

# 3. Limiti della soluzione sviluppata

[DA COMPILARE: elencare i limiti effettivi verificati durante l'utilizzo.]

Possibili limiti:

- disponibilità limitata ai sistemi operativi supportati;
- necessità di avviare il database e il server;
- dipendenza dalla connessione di rete;
- numero limitato di ristoranti nel data set dimostrativo;
- prezzi stimati e contrassegnati con `≈`;
- dati del dataset non necessariamente aggiornati;
- modifica o cancellazione delle recensioni non disponibili;
- prenotazioni o pagamenti non implementati;
- geolocalizzazione non disponibile in tutte le condizioni;
- disponibilità delle funzionalità remote subordinata al server.

# 4. Sitografia / Bibliografia

## 4.1 Risorse web

**Formato suggerito:**

- Autore o ente. *Titolo della pagina*. URL: `https://...`. Ultimo accesso: GG/MM/AAAA.
- Oracle. *Java Documentation*. URL: `https://docs.oracle.com/en/java/`. Ultimo accesso: `__`.
- OpenJFX. *JavaFX Documentation*. URL: `https://openjfx.io/`. Ultimo accesso: `__`.
- PostgreSQL. *PostgreSQL Documentation*. URL: `https://www.postgresql.org/docs/16/`. Ultimo accesso: `__`.
- PostGIS. *PostGIS Documentation*. URL: `https://postgis.net/documentation/`. Ultimo accesso: `__`.

[DA COMPILARE: inserire tutte le risorse effettivamente consultate.]

## 4.2 Pubblicazioni

**Formato suggerito:**

- Cognome, Nome. *Titolo dell'opera*. Edizione, editore, anno, ISBN.
- Autore. *Titolo dell'articolo*. Nome della rivista, volume, numero, pagine, anno, DOI.
- COGNOME Nome. *Titolo della relazione accademica*. Università, corso di laurea, anno accademico.
