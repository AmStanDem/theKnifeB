# Revisioni — confronto tra specifiche e progetto The Knife

## Scopo e legenda

Questo documento mette in relazione i requisiti indicati nelle specifiche del professore con l'implementazione attuale del progetto. I nomi tra backtick sono i nomi richiesti oppure quelli effettivamente individuati nel codice.

- **Presente**: la funzione risulta implementata nel flusso applicativo.
- **Parziale**: la funzione esiste, ma presenta una differenza o una copertura incompleta rispetto alla specifica.
- **Da completare/verificare**: non è stata individuata un'implementazione sufficiente a dimostrare l'intero requisito.

I prompt sono pensati per essere copiati singolarmente in un assistente di programmazione con accesso al repository. Chiedono di controllare il codice prima di intervenire, così da non duplicare funzioni già esistenti.

---

## 1. Ricerca e consultazione — client e guest

### `cercaRistorante()`

**Stato: presente, con una differenza tra flusso UI e contratto dati.**

La ricerca paginata è costruita da `ListaRistorantiController.caricaPagina`, `eseguiRicerca` e `creaRichiesta`; il controller invia i filtri al server. `RicercaAvanzataController` gestisce la raccolta dei filtri e la località. Sul server, `RistoranteDAO.cercaPagina` applica filtri di cucina, prezzo, stelle, delivery, prenotazione e distanza geografica.

La località è obbligatoria nell'interfaccia di ricerca analizzata, che usa coordinate e raggio. Il DTO `RicercaRistorantiPaginaDTO`, da solo, non garantisce tale obbligatorietà. È opportuno verificare che la validazione sia anche nel livello server e che copra ogni percorso di ricerca, non soltanto la schermata principale.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/ListaRistorantiController.java`; `client/src/main/java/org/uninsubria/theknife/client/controller/RicercaAvanzataController.java`; `common/src/main/java/org/uninsubria/theknife/common/dto/RicercaRistorantiPaginaDTO.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RistoranteDAO.java`.

**Prompt per l'IA**

> Nel repository The Knife verifica la funzione richiesta `cercaRistorante()`, oggi realizzata principalmente da `ListaRistorantiController` e `RistoranteDAO.cercaPagina`. Controlla tutti i flussi di ricerca e i DTO: la località geografica deve essere obbligatoria e i filtri per cucina, fascia di prezzo, delivery, prenotazione online e media/minimo stelle devono essere coerenti dal client fino alla query JDBC. Implementa solo le correzioni necessarie, rispettando architettura e convenzioni esistenti; aggiungi o aggiorna test per validazione e combinazioni di filtri. Non duplicare filtri già correttamente implementati e non modificare requisiti non coinvolti.

### `visualizzaRistorante()`

**Stato: presente.**

`AreaRistoranteController.aggiornaDettaglioRistorante` e `mostraDatiRistorante` recuperano e mostrano i dettagli tramite il servizio `RistoranteService.ottieniDettaglio` e il DAO `RistoranteDAO.trovaPerId`. Il dettaglio include le caratteristiche del ristorante e i dati di contatto disponibili.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/AreaRistoranteController.java`; `server/src/main/java/org/uninsubria/theknife/server/service/RistoranteService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RistoranteDAO.java`.

**Prompt per l'IA**

> Verifica che la funzione richiesta `visualizzaRistorante()` sia completa in `AreaRistoranteController` e nel recupero dati server. Confronta i campi mostrati con quelli previsti dalle specifiche e con il modello/schema effettivi. Se manca un dato obbligatorio, implementa il recupero e la visualizzazione attraverso i livelli già esistenti, aggiornando i test pertinenti. Non cambiare layout o comportamento che già soddisfano la consegna.

### `visualizzaRecensioni()` — lato consultazione

**Stato: parziale rispetto al conteggio complessivo.**

`RecensioneDAO.trovaPerRistorante` recupera le recensioni di un ristorante. `AreaRistoranteController` le mostra e calcola media e distribuzione delle stelle. La media visualizzata è convertita in scala `/10`, mentre il voto delle recensioni è su 5. Il contatore mostrato nella lista filtrata può contare solo le recensioni visibili dopo il filtro, e non equivale necessariamente al totale richiesto.

**Riferimenti:** `server/src/main/java/org/uninsubria/theknife/server/dao/RecensioneDAO.java`; `client/src/main/java/org/uninsubria/theknife/client/controller/AreaRistoranteController.java`.

**Prompt per l'IA**

> Controlla la funzione `visualizzaRecensioni()` in `AreaRistoranteController` e il recupero in `RecensioneDAO.trovaPerRistorante`. Allinea la schermata alla specifica mostrando chiaramente recensioni, stelle, media e numero totale di recensioni; il totale deve restare distinto dal conteggio dei risultati filtrati. Mantieni esplicita e coerente la scala dei voti (1–5, oppure la conversione già prevista dalla UI). Aggiungi test per lista vuota, filtro attivo e totale diverso dal numero filtrato.

---

## 2. Registrazione e autenticazione

### `registrazione()`

**Stato: presente, con controlli server da allineare.**

`SignUpController.handleRegistrati` e `registraUtente` raccolgono nome, cognome, email/username, password, data di nascita, domicilio e ruolo cliente/ristoratore. `UtenteService.registra` esegue le verifiche server e applica BCrypt; `UtenteDAO.inserisciUtente` persiste l'hash. BCrypt è hashing non reversibile, non cifratura reversibile, ed è l'approccio appropriato per memorizzare password.

La UI controlla la complessità della password, ma il servizio server richiede soltanto che non sia vuota. Poiché il ruolo gestore può essere accettato dal percorso chiamato `registraCliente`, va controllato che la politica di registrazione dei gestori coincida con la consegna.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/SignUpController.java`; `server/src/main/java/org/uninsubria/theknife/server/service/UtenteService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/UtenteDAO.java`.

**Prompt per l'IA**

> Esamina la registrazione in `SignUpController`, `UtenteService` e `UtenteDAO`. Allinea i campi richiesti (nome, cognome, email/username, password, data di nascita, domicilio e ruolo) e soprattutto applica lato server gli stessi requisiti minimi di password già previsti dalla UI, senza salvare mai la password in chiaro. Verifica inoltre la regola di assegnazione del ruolo cliente/gestore rispetto alle specifiche. Aggiorna test di validazione e persistenza, mantenendo BCrypt e senza introdurre cifratura reversibile.

### Login / autenticazione

**Stato: presente.**

`LoginController.onAccedi` invoca l'autenticazione server; `UtenteService.autentica` verifica la password BCrypt e restituisce i dati utente senza esporre l'hash. Cliente e gestore usano il medesimo flusso e vengono distinti tramite il ruolo. Il pulsante della vista di login è collegato a `onAccedi`.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/LoginController.java`; `client/src/main/resources/org/uninsubria/theknife/client/views/LoginView.fxml`; `server/src/main/java/org/uninsubria/theknife/server/service/UtenteService.java`.

**Prompt per l'IA**

> Verifica il login comune a clienti e gestori implementato da `LoginController.onAccedi` e `UtenteService.autentica`. Assicurati che l'autenticazione verifichi BCrypt, non restituisca l'hash della password e che il ruolo determini correttamente le operazioni e le schermate accessibili. Correggi solo eventuali discrepanze con le specifiche e aggiungi test per login valido, password errata e ruoli diversi.

---

## 3. Interazione dei clienti registrati

### `aggiungiPreferito()`, `rimuoviPreferito()` e `visualizzaPreferiti()`

**Stato: presenti nel flusso attivo.**

Le operazioni server sono gestite da `PreferitiService` e `PreferitiDAO`. `PreferitiController.caricaPreferiti` mostra la lista e `rimuoviPreferito` gestisce la rimozione; la vista associata è `Preferiti.fxml`. Il dettaglio ristorante consente di aggiungere/togliere il preferito e aggiornare lo stato.

La vecchia vista `listaPreferiti.fxml` e il controller `ListaPreferitiController`, che usavano dati di test, sono stati rimossi. La lista effettiva dei preferiti è gestita da `Preferiti.fxml` e `PreferitiController`.

**Riferimenti:** `server/src/main/java/org/uninsubria/theknife/server/service/PreferitiService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/PreferitiDAO.java`; `client/src/main/java/org/uninsubria/theknife/client/controller/PreferitiController.java`; `client/src/main/resources/org/uninsubria/theknife/client/views/Preferiti.fxml`.

**Prompt per l'IA**

> Controlla il flusso attivo dei preferiti in `PreferitiService`, `PreferitiDAO`, `PreferitiController` e `Preferiti.fxml`. Verifica che un cliente autenticato possa aggiungere un ristorante, rimuoverlo e visualizzare la lista persistita, e che lo stato del pulsante nel dettaglio sia aggiornato correttamente. Aggiungi test per aggiunta, rimozione, duplicati e lista vuota.

### `aggiungiRecensione()`

**Stato: presente; possibile differenza sul voto frazionario.**

`ScriviRecensioneController.onInviaRecensioneClick` invia testo e voto a `RecensioneService` e `RecensioneDAO`. Il servizio valida voti compresi tra 1 e 5, ammettendo anche mezze stelle.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/ScriviRecensioneController.java`; `server/src/main/java/org/uninsubria/theknife/server/service/RecensioneService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RecensioneDAO.java`; migrazione `V3__support_half_star_reviews.sql`.

**Prompt per l'IA**

> Verifica la creazione delle recensioni in `ScriviRecensioneController`, `RecensioneService` e `RecensioneDAO`. Assicurati che testo e voto siano persistiti, che l'autore sia l'utente autenticato e che il voto rispetti esattamente il formato previsto dalla consegna. Attualmente il progetto consente mezze stelle: mantieni tale comportamento se è ammesso dalle specifiche, altrimenti rendi coerenti UI, validazione server, schema e test per accettare solo interi da 1 a 5.

### `modificaRecensione()` e `eliminaRecensione()`

**Stato: presenti.**

Le operazioni sono implementate in `RecensioneService` e `RecensioneDAO`; modifica ed eliminazione sono vincolate all'autore della recensione. L'interfaccia di scrittura/modifica è gestita dal flusso di `ScriviRecensioneController`.

**Riferimenti:** `server/src/main/java/org/uninsubria/theknife/server/service/RecensioneService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RecensioneDAO.java`; `client/src/main/java/org/uninsubria/theknife/client/controller/ScriviRecensioneController.java`.

**Prompt per l'IA**

> Esamina le operazioni di modifica ed eliminazione delle recensioni in `RecensioneService`, `RecensioneDAO` e nel flusso client. Verifica che soltanto l'autore possa modificare o cancellare la propria recensione, che gli aggiornamenti siano persistenti e che la UI mostri l'esito corretto. Correggi eventuali discrepanze senza rimuovere i controlli di autorizzazione e aggiungi test per autore legittimo e utente diverso.

---

## 4. Interazione dei gestori di ristoranti

### `aggiungiRistorante()`

**Stato: presente.**

`AggiungiRistoranteController.onSalvaClick` e `salvaRistorante` raccolgono e validano i dati; il flusso chiama `RistoranteService.aggiungiRistorante` e `RistoranteDAO.inserisciRistorante`. L'inserimento include le caratteristiche e le associazioni alle cucine e verifica il ruolo del gestore.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/AggiungiRistoranteController.java`; `server/src/main/java/org/uninsubria/theknife/server/service/RistoranteService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RistoranteDAO.java`.

**Prompt per l'IA**

> Verifica l'aggiunta del ristorante attraverso `AggiungiRistoranteController`, `RistoranteService` e `RistoranteDAO`. Confronta i campi del form e i controlli con tutte le caratteristiche richieste dalle specifiche, garantendo che il ristorante sia associato al gestore autenticato e che l'inserimento di ristorante e cucine sia coerente in caso di errore. Implementa solo campi o validazioni mancanti e aggiorna i test pertinenti.

### `visualizzaRiepilogo()`

**Stato: da completare rispetto al riepilogo richiesto.**

`ListaRistorantiGestoreController` mostra la lista dei ristoranti del gestore tramite `getRistorantiGestore`/`trovaPerGestore` e il numero di ristoranti. Non risulta un riepilogo aggregato del gestore con media delle stelle e numero totale di recensioni ricevute.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/ListaRistorantiGestoreController.java`; `common/src/main/java/org/uninsubria/theknife/common/rmi/ITheKnifeServer.java`; servizio e DAO dei ristoranti.

**Prompt per l'IA**

> Implementa la funzione richiesta `visualizzaRiepilogo()` per il gestore: deve mostrare i ristoranti di sua proprietà e, per ciascuno (o secondo la forma richiesta dalla consegna), la media delle stelle e il numero totale di recensioni. Ispeziona prima `ITheKnifeServer`, `TheKnifeServerImpl`, servizi, DAO e schermate esistenti; riusa i dati già disponibili e aggiungi un DTO/metodo soltanto se necessario. I dati devono essere calcolati sul proprietario autenticato e non devono includere ristoranti altrui. Aggiorna UI e test per nessuna recensione, più ristoranti e medie diverse.

### `visualizzaRecensioni()` — lato gestore

**Stato: parziale.**

Il gestore può consultare le recensioni entrando nel dettaglio dei singoli ristoranti. Non è stato individuato un endpoint o una vista dedicata che elenchi insieme le recensioni dei propri ristoranti.

**Riferimenti:** `client/src/main/java/org/uninsubria/theknife/client/controller/AreaRistoranteController.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RecensioneDAO.java`; `ListaRistorantiGestoreController.java`.

**Prompt per l'IA**

> Verifica la consultazione delle recensioni da parte del gestore. Se la specifica richiede una vista riepilogativa, implementa una schermata o un flusso che elenchi recensioni e stelle di tutti e soli i ristoranti posseduti dal gestore autenticato, riusando i componenti esistenti. Aggiungi i controlli di autorizzazione sul server: non fidarti di un identificativo gestore fornito liberamente dal client. Se il dettaglio per singolo ristorante è già sufficiente secondo la consegna, documenta tale corrispondenza senza introdurre una funzione duplicata.

### `rispostaRecensioni()`

**Stato: presente, incluso il vincolo di una sola risposta.**

`RecensioneService.aggiungiRisposta` delega al DAO una risposta del gestore proprietario. La query verifica la proprietà e impedisce una seconda risposta; inoltre `Risposte_Recensioni.id_recensione` è chiave primaria nel database.

**Riferimenti:** `server/src/main/java/org/uninsubria/theknife/server/service/RecensioneService.java`; `server/src/main/java/org/uninsubria/theknife/server/dao/RecensioneDAO.java`; migrazione `V1__createSchema__schema iniziale.sql`.

**Prompt per l'IA**

> Controlla `rispostaRecensioni()` nel flusso server, in particolare `RecensioneService.aggiungiRisposta`, il DAO e il vincolo DB su `Risposte_Recensioni`. Conferma con test che soltanto il gestore proprietario possa rispondere e che una recensione accetti al massimo una risposta anche in caso di richieste concorrenti. Correggi eventuali lacune mantenendo il vincolo transazionale/database e aggiorna la UI per mostrare chiaramente una risposta già inserita.

---

## 5. Architettura client/server e database

### Server e richieste concorrenti

**Stato: presente con RMI; il nome del modulo differisce.**

Il client comunica con il contratto RMI `ITheKnifeServer` nel modulo `common`; `TheKnifeServerImpl` implementa il contratto e `MainServer` avvia il server. `RmiConcurrencyHarness` verifica operazioni simultanee da due client RMI. `DatabaseConfig` configura Hikari con un pool thread-safe.

Il modulo nel repository si chiama `server`, non `serverTK`. Non risulta configurato un pool di thread RMI personalizzato: la concorrenza è affidata a RMI e il pool JDBC gestisce le connessioni al database.

**Riferimenti:** `common/src/main/java/org/uninsubria/theknife/common/rmi/ITheKnifeServer.java`; `server/src/main/java/org/uninsubria/theknife/server/rmi/TheKnifeServerImpl.java`; `server/src/main/java/org/uninsubria/theknife/server/MainServer.java`; `server/src/test/java/org/uninsubria/theknife/server/RmiConcurrencyHarness.java`; `server/src/main/java/org/uninsubria/theknife/server/config/DatabaseConfig.java`.

**Prompt per l'IA**

> Confronta l'architettura client/server attuale (JavaFX, contratto RMI `ITheKnifeServer`, `TheKnifeServerImpl`, `MainServer` e pool Hikari) con il requisito di gestire più client concorrenti. Verifica e, se possibile, aggiungi test di integrazione che eseguano richieste simultanee e controllino la consistenza dei dati. Non introdurre un pool RMI personalizzato né rinominare moduli senza una necessità dimostrata dalle specifiche. Segnala esplicitamente se `serverTK` è un nome obbligatorio o soltanto una denominazione usata dal professore.

### Persistenza JDBC / PostgreSQL

**Stato: presente.**

`DatabaseConfig` configura connessione JDBC, Hikari e Flyway; `compose.yaml` avvia PostgreSQL/PostGIS. La migrazione iniziale crea `Utenti`, `RistorantiTheKnife`, `Recensioni`, `Risposte_Recensioni`, `Preferiti` e le tabelle di supporto alle tipologie di cucina. Migrazioni successive aggiungono campi e vincoli: per verificare lo schema corrente occorre considerare l'intera sequenza, non soltanto V1.

**Riferimenti:** `server/src/main/java/org/uninsubria/theknife/server/config/DatabaseConfig.java`; `server/src/main/resources/db/migration/`; `compose.yaml`.

**Prompt per l'IA**

> Analizza tutte le migrazioni Flyway, la configurazione JDBC e i DAO, quindi confronta lo schema effettivo PostgreSQL con le tabelle richieste (`RistorantiTheKnife`, `Utenti` e tabelle recensioni). Verifica nomi, campi, chiavi, relazioni e vincoli usando l'intera sequenza delle migrazioni. Correggi solo le discrepanze necessarie con nuove migrazioni additive e testabili; non modificare o cancellare migrazioni già applicate e non alterare dati esistenti senza una strategia sicura.

---

## Riepilogo delle priorità

1. **Completare `visualizzaRiepilogo()`** con media e numero totale di recensioni per il gestore.
2. **Chiarire il conteggio in `visualizzaRecensioni()`** e distinguere totale complessivo da risultati filtrati.
3. **Rendere coerenti i controlli server di `registrazione()`** con le regole password della UI e la politica di assegnazione del ruolo.
4. **Verificare il vincolo geografico in tutti i percorsi di `cercaRistorante()`**, non solo nel controller.
5. **Confermare con il professore** se il voto recensione può includere mezze stelle e se `serverTK` è un nome letterale obbligatorio.
