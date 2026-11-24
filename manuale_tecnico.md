# TheKnife

## Manuale Tecnico

**Autori:** [DA COMPILARE: Riotto Thomas 760981 VA; Tullo Alessandro 760760 VA; Albé Giovanni 760117 VA; Pesavento Antonio 759933 VA]

**Data:** [DA COMPILARE: GG/MM/AAAA]

**Versione documento:** [DA COMPILARE: es. 1.0]

---

# Indice

1. [Architettura e Scelte Progettuali](#1-architettura-e-scelte-progettuali)
   1. [Scelte architetturali](#11-scelte-architetturali)
   2. [Strutture dati utilizzate](#12-strutture-dati-utilizzate)
   3. [Scelte algoritmiche](#13-scelte-algoritmiche)
   4. [Design pattern adottati](#14-design-pattern-adottati)
2. [Progettazione del Sistema - Diagrammi UML](#2-progettazione-del-sistema---diagrammi-uml)
   1. [Struttura statica ad alto livello](#21-struttura-statica-ad-alto-livello)
   2. [Struttura dinamica e interazioni](#22-struttura-dinamica-e-interazioni)
3. [Progettazione del Database](#3-progettazione-del-database)
   1. [Documento di analisi dei requisiti ristrutturato](#31-documento-di-analisi-dei-requisiti-ristrutturato)
   2. [Schema ER non ristrutturato](#32-schema-er-non-ristrutturato)
   3. [Schema ER ristrutturato](#33-schema-er-ristrutturato)
   4. [Documentazione dello schema](#34-documentazione-dello-schema)
   5. [Vincoli in linguaggio naturale](#35-vincoli-in-linguaggio-naturale)
4. [Documentazione del Codice - JavaDoc](#4-documentazione-del-codice---javadoc)
5. [Setup Tecnico, Compilazione ed Esecuzione](#5-setup-tecnico-compilazione-ed-esecuzione)
6. [Limiti Tecnici della Soluzione Sviluppata](#6-limiti-tecnici-della-soluzione-sviluppata)
7. [Sitografia / Bibliografia](#7-sitografia--bibliografia)

---

# 1. Architettura e Scelte Progettuali

## 1.1 Scelte architetturali

### 1.1.1 Architettura generale

Il sistema adotta un'architettura distribuita a più componenti:

- **Client:** applicazione desktop JavaFX;
- **Server applicativo:** servizi Java esposti tramite Java RMI;
- **Database:** PostgreSQL con estensione PostGIS;
- **Repository:** build Maven multi-modulo.

```text
Client JavaFX
      |
      | Java RMI
      v
Server applicativo
      |
      | JDBC / HikariCP
      v
PostgreSQL + PostGIS
```

[DA COMPILARE: motivare la scelta architetturale rispetto a alternative come REST o architettura monolitica.]

### 1.1.2 Moduli Maven

| Modulo | Responsabilità |
|---|---|
| `common` | DTO, eccezioni e contratto RMI condivisi |
| `server` | servizi, DAO, database, RMI e migrazioni |
| `client` | JavaFX, controller FXML, risorse e interazione utente |

Il client e il server dipendono da `common` e non dipendono direttamente l'uno dall'altro.

### 1.1.3 Comunicazione client-server

Componenti principali:

- `ITheKnifeServer`;
- `TheKnifeServerImpl`;
- `RemoteObserver`;
- `RemoteSubscriptionManager`;
- `SubscriptionTopic`;
- `DataChangedEvent`;
- `ServerConnection`.

[DA COMPILARE: descrivere registry RMI, porte, chiamate sincrone, sottoscrizioni e propagazione degli aggiornamenti.]

### 1.1.4 Architettura dei dati

[DA COMPILARE: descrivere il flusso dei dati fra vista, DTO, servizio, DAO e database.]

```text
Vista JavaFX -> Controller -> ServerConnection -> RMI
-> TheKnifeServerImpl -> Service -> DAO -> PostgreSQL
```

## 1.2 Strutture dati utilizzate

[DA COMPILARE: elencare e motivare tutte le strutture dati rilevanti.]

Possibili strutture:

- `List<T>` per i risultati;
- `Map<K, V>` per indici, cache o aggregazioni;
- DTO per il trasferimento di dati;
- dati osservabili JavaFX;
- pagine di ristoranti;
- strutture per risultati paginati;
- liste di preferiti e recensioni.

| Struttura | Responsabilità | Motivazione |
|---|---|---|
| `RistoranteDTO` | Trasferimento dati ristorante | Separazione dal modello SQL |
| `UtenteDTO` | Trasferimento dati utente | Incapsulamento dei dati remoti |
| `RecensioneDTO` | Recensione e risposta | Supporto alle operazioni RMI |
| `PaginaRistorantiDTO` | Risultati paginati | Riduzione dei dati trasferiti |
| `FiltriRicercaDTO` | Criteri di ricerca | Gestione unificata dei filtri |

## 1.3 Scelte algoritmiche

[DA COMPILARE: descrivere gli algoritmi rilevanti, complessità e casi limite.]

### 1.3.1 Ricerca dei ristoranti

Documentare:

- costruzione dei filtri;
- query dinamiche;
- gestione dei filtri vuoti;
- paginazione;
- ordinamento;
- indici utilizzati;
- complessità attesa.

### 1.3.2 Ricerca geografica

Documentare:

- geocodifica;
- coordinate geografiche;
- distanze;
- PostGIS;
- gestione dei luoghi non riconosciuti.

Componenti rilevanti:

- `AutocompleteLuoghi`;
- `GeocodingService`;
- `LuogoInput`;
- `SelettoreLuogoDialog`.

### 1.3.3 Stima dei prezzi

Il dataset Michelin usa una colonna `Price` composta da simboli valuta.

La stima considera:

- numero di simboli valuta;
- fascia di prezzo;
- coefficiente del costo della vita;
- variazione deterministica;
- trattamento dei valori non validi.

Componenti rilevanti:

- `PrezzoDatasetEstimator`;
- `CsvImporter`.

### 1.3.4 Concorrenza

Documentare:

- prevenzione delle azioni duplicate;
- locking ottimistico delle recensioni;
- gestione dei conflitti;
- eccezioni applicative;
- propagazione degli aggiornamenti.

## 1.4 Design pattern adottati

[DA COMPILARE: indicare i pattern effettivamente osservati nel progetto.]

### 1.4.1 MVC o separazione presentazione-logica

- Vista: file FXML;
- Controller: package `org.uninsubria.theknife.client.controller`;
- Modello/DTO: package `org.uninsubria.theknife.common.dto`;
- vantaggio: separazione fra interfaccia e logica applicativa.

### 1.4.2 DAO

| DAO | Responsabilità |
|---|---|
| `UtenteDAO` | Operazioni sugli utenti |
| `RistoranteDAO` | Operazioni sui ristoranti |
| `RecensioneDAO` | Operazioni sulle recensioni |
| `PreferitiDAO` | Operazioni sui preferiti |

### 1.4.3 Service Layer

I servizi racchiudono la logica applicativa:

- `UtenteService`;
- `RistoranteService`;
- `RecensioneService`;
- `PreferitiService`.

### 1.4.4 Observer

Il client riceve notifiche remote quando i dati cambiano.

I topic controllano quali viste devono aggiornarsi dopo modifiche a:

- ristoranti;
- recensioni;
- risposte;
- preferiti;
- account.

### 1.4.5 Singleton o componenti condivisi

[DA COMPILARE: documentare `SessioneUtente`, `SceneManager`, `DatabaseConfig` o altri componenti condivisi.]

### 1.4.6 Codice significativo

```java
// [DA INSERIRE: estratto significativo.]
// Indicare:
// - classe e metodo;
// - pattern applicato;
// - motivazione;
// - flusso prima e dopo l'esecuzione.
```

# 2. Progettazione del Sistema - Diagrammi UML

## 2.1 Struttura statica ad alto livello

### 2.1.1 Class Diagram

[DA INSERIRE: diagramma delle classi ad alto livello.]

Argomenti da rappresentare:

- package `client`, `common` e `server`;
- relazioni fra controller, viste e DTO;
- relazioni fra servizi, DAO e database;
- contratto RMI e relativa implementazione;
- gerarchie delle eccezioni.

## 2.2 Struttura dinamica e interazioni

### 2.2.1 Sequence Diagram - Login

```text
Utente -> JavaFX -> LoginController -> ServerConnection
       -> ITheKnifeServer -> TheKnifeServerImpl
       -> UtenteService -> UtenteDAO -> PostgreSQL
```

[DA INSERIRE: diagramma UML completo con messaggi e ritorni.]

### 2.2.2 Sequence Diagram - Ricerca

```text
Utente -> RicercaAvanzataController -> ServerConnection
       -> ITheKnifeServer -> RistoranteService
       -> RistoranteDAO -> PostgreSQL
```

[DA INSERIRE: diagramma UML completo.]

### 2.2.3 Sequence Diagram - Recensione

Descrivere:

- autenticazione dell'utente;
- controllo dei dati;
- prevenzione dei duplicati;
- controllo della versione;
- aggiornamento del database;
- invio della notifica remota.

[DA INSERIRE: diagramma UML completo.]

### 2.2.4 Interaction Diagram

[DA INSERIRE: diagramma di collaborazione alternativo al sequence diagram.]

### 2.2.5 State Diagram

[DA INSERIRE: diagramma degli stati rappresentativo.]

Stati possibili:

- ospite;
- utente autenticato;
- recensione modificabile;
- recensione obsoleta;
- sottoscrizione attiva;
- sottoscrizione rimossa.

# 3. Progettazione del Database

## 3.1 Documento di analisi dei requisiti ristrutturato

### 3.1.1 Requisiti funzionali

- consultazione dei ristoranti;
- ricerca con filtri;
- registrazione e accesso;
- gestione dei preferiti;
- inserimento delle recensioni;
- gestione dei ristoranti da parte dei gestori;
- risposta alle recensioni;
- notifica dei cambiamenti alle viste sottoscritte.

### 3.1.2 Requisiti informativi

[DA COMPILARE: descrivere le informazioni effettivamente memorizzate.]

- utente;
- cliente o ristoratore;
- ristorante;
- recensione;
- preferito;
- risposta;
- luogo;
- tipologie di cucina.

### 3.1.3 Requisiti non funzionali

[DA COMPILARE: prestazioni, integrità, sicurezza, disponibilità e portabilità.]

## 3.2 Schema ER non ristrutturato

![DA INSERIRE: schema ER non ristrutturato]

Documentare:

- entità iniziali;
- attributi;
- relazioni;
- cardinalità;
- ridondanze;
- elementi da normalizzare.

## 3.3 Schema ER ristrutturato

![DA INSERIRE: schema ER ristrutturato]

Documentare:

- chiavi primarie;
- chiavi esterne;
- normalizzazione;
- vincoli;
- indici;
- relazioni molte-a-molti.

## 3.4 Documentazione dello schema

| Entità | Attributi principali | Chiave primaria | Relazioni |
|---|---|---|---|
| Utente | ID, email, password, nome, tipo | ID utente | Preferiti, recensioni, ristoranti |
| Ristorante | ID, nome, indirizzo, coordinate, prezzo | ID ristorante | Recensioni, preferiti |
| Recensione | ID, voto, testo, versione | ID recensione | Utente, ristorante |
| Preferito | utente, ristorante | Coppia utente-ristorante | Utente, ristorante |

[DA COMPILARE: completare con tutte le tabelle presenti nelle migrazioni Flyway.]

### 3.4.1 Migrazioni Flyway

Descrivere i file in `server/src/main/resources/db/migration/`:

- creazione dello schema;
- dati iniziali;
- supporto ai mezzi punti;
- indici di ricerca;
- prezzi sconosciuti;
- locking delle recensioni;
- collegamenti esterni;
- account dimostrativi;
- ottimizzazione delle ricerche.

## 3.5 Vincoli in linguaggio naturale

- l'email dell'utente è univoca;
- la password è memorizzata protetta;
- un utente può inserire un ristorante nei preferiti una sola volta;
- un cliente può lasciare una sola recensione per ristorante;
- il voto deve appartenere all'intervallo consentito;
- una recensione appartiene a un utente e a un ristorante;
- un ristoratore risponde alle recensioni dei propri ristoranti;
- una risposta appartiene a una sola recensione;
- un aggiornamento obsoleto deve essere rifiutato;
- cancellazioni e aggiornamenti devono rispettare le chiavi esterne;
- le coordinate geografiche devono essere valide;
- i prezzi stimati devono essere distinti dai prezzi gestiti.

# 4. Documentazione del Codice - JavaDoc

## 4.1 Generazione

Comando Maven indicativo:

```text
mvn javadoc:javadoc
```

[DA COMPILARE: indicare il comando effettivamente utilizzato e il percorso HTML generato.]

Comando diretto d'esempio:

```text
javadoc -d doc/javadoc -sourcepath common/src/main/java;server/src/main/java;client/src/main/java -subpackages org.uninsubria.theknife
```

Su Unix usare `:` invece di `;` per separare i percorsi.

## 4.2 Package

I package devono contenere una descrizione della responsabilità e, quando utile:

```java
/**
 * [Descrizione del package.]
 *
 * @author [Nome autore]
 * @see [Package o classe correlata]
 */
```

## 4.3 Classi e interfacce

```java
/**
 * [Descrizione della classe o interfaccia.]
 *
 * @author [Nome autore]
 * @see [Classe, interfaccia o package correlato]
 */
public class ClasseEsempio {
}
```

## 4.4 Attributi

```java
/**
 * <p>Indica [descrizione dell'attributo].</p>
 * <p>Valore rappresentato come <code>Tipo</code>.</p>
 */
private Tipo attributo;
```

## 4.5 Metodi

```java
/**
 * [Descrizione del metodo.]
 *
 * @param parametro descrizione del parametro
 * @return descrizione del valore restituito
 * @throws EccezioneDescrittiva se la precondizione non è soddisfatta
 */
public Tipo metodo(Tipo parametro) {
    // [Implementazione]
}
```

## 4.6 Linee guida

- documentare responsabilità e comportamento;
- usare `@author` nelle classi e interfacce;
- usare `@see` nei package e nelle API correlate;
- usare `<p>` per separare i paragrafi;
- usare `<code>` per tipi e identificatori;
- usare `@param` per ogni parametro;
- usare `@return` quando viene restituito un valore;
- usare `@throws` per ogni eccezione rilevante;
- usare `@deprecated` per API obsolete;
- evitare descrizioni che ripetano solo il nome del metodo.

# 5. Setup Tecnico, Compilazione ed Esecuzione

## 5.1 Requisiti tecnici

- JDK 21 o successivo;
- Maven 3.9 o successivo;
- Docker Engine o Docker Desktop;
- PostgreSQL 16;
- estensione PostGIS;
- JavaFX 21.0.2;
- PostgreSQL JDBC Driver;
- HikariCP;
- Flyway;
- jBCrypt;
- ControlsFX;
- AtlantaFX;
- Apache Commons CSV.

## 5.2 Configurazione ambiente

Copiare `.env.example` in `.env`:

```text
cp .env.example .env
```

Su Windows PowerShell:

```text
Copy-Item .env.example .env
```

Configurazione d'esempio:

```text
DB_HOST_PORT=5432
THEKNIFE_RMI_HOST=localhost
THEKNIFE_RMI_PORT=1099
```

[DA COMPILARE: documentare proprietà tecniche non sensibili presenti nei file di configurazione.]

## 5.3 Compilazione

Dalla directory radice:

```text
mvn clean package
```

Risultati attesi:

- `bin/theknife-server.jar`;
- `bin/theknife-client-linux.jar`;
- `bin/theknife-client-windows.jar`;
- `bin/theknife-client-macos.jar`.

## 5.4 Avvio del database

```text
docker compose up -d db
```

Per usare una porta host diversa:

```text
DB_HOST_PORT=5433 docker compose up -d db
```

## 5.5 Avvio del server

Profilo Maven:

```text
mvn -pl server -am -Prun-server integration-test
```

Avvio diretto:

```text
java -jar bin/theknife-server.jar
```

Il server:

- applica le migrazioni Flyway;
- inizializza HikariCP;
- avvia il registry RMI;
- espone `ITheKnifeServer`;
- resta attivo fino alla terminazione del processo.

## 5.6 Avvio del client

Dai sorgenti:

```text
mvn -pl client -am -Prun-client process-classes
```

Avvio diretto:

```text
java -jar bin/theknife-client-windows.jar
```

Sostituire il JAR in base al sistema operativo.

## 5.7 JavaDoc

[DA COMPILARE: inserire il comando utilizzato dal gruppo.]

```text
mvn javadoc:javadoc
javadoc -d doc/javadoc [DA COMPLETARE]
```

## 5.8 Test di concorrenza RMI

Configurare un database dedicato:

```text
THEKNIFE_TEST_JDBC_URL=jdbc:postgresql://localhost:5432/theknife_test
THEKNIFE_TEST_DB_USER=admin
THEKNIFE_TEST_DB_PASSWORD=[DA COMPILARE]
mvn -pl server -am -Pconcurrency-tests integration-test
```

Casi da documentare:

- preferito duplicato;
- recensione duplicata;
- conflitto tra aggiornamenti della stessa recensione;
- comportamento di locking ottimistico;
- eccezioni applicative.

# 6. Limiti Tecnici della Soluzione Sviluppata

[DA COMPILARE: elencare i limiti verificati attraverso test e revisione del codice.]

Possibili limiti:

- dipendenza da Docker e PostgreSQL;
- copertura limitata dell'harness di concorrenza;
- assenza di test end-to-end completi;
- geocodifica dipendente da servizi esterni;
- prestazioni su dataset di grandi dimensioni;
- prezzi stimati e non verificati;
- schema soggetto a ulteriori normalizzazioni;
- gestione limitata delle funzionalità remote;
- validazione incompleta di dati provenienti dal dataset;
- necessità di distribuire JAR separati per ciascun sistema operativo.

# 7. Sitografia / Bibliografia

## 7.1 Risorse web

**Formato suggerito:**

- Oracle. *Java 21 Documentation*. URL: `https://docs.oracle.com/en/java/javase/21/`. Ultimo accesso: GG/MM/AAAA.
- OpenJFX. *JavaFX 21 Documentation*. URL: `https://openjfx.io/`. Ultimo accesso: GG/MM/AAAA.
- Apache Maven. *Maven Documentation*. URL: `https://maven.apache.org/guides/`. Ultimo accesso: GG/MM/AAAA.
- PostgreSQL. *PostgreSQL 16 Documentation*. URL: `https://www.postgresql.org/docs/16/`. Ultimo accesso: GG/MM/AAAA.
- Flyway. *Flyway Documentation*. URL: `https://documentation.red-gate.com/fd`. Ultimo accesso: GG/MM/AAAA.
- HikariCP. *HikariCP Documentation*. URL: `https://github.com/brettwooldridge/HikariCP`. Ultimo accesso: GG/MM/AAAA.
- PostGIS. *PostGIS Documentation*. URL: `https://postgis.net/documentation/`. Ultimo accesso: GG/MM/AAAA.
- Oracle. *Java RMI Documentation*. URL: `https://docs.oracle.com/javase/tutorial/rmi/`. Ultimo accesso: GG/MM/AAAA.

[DA COMPILARE: inserire tutte le fonti effettivamente consultate.]

## 7.2 Pubblicazioni tecniche

**Formato suggerito:**

- Cognome, Nome. *Titolo del libro o articolo*. Editore o rivista, anno, DOI o ISBN.
- Autore. *Titolo del manuale tecnico*. Organizzazione, versione, anno.
