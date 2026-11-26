# Confronto tra `nomenclatura` e `clientAccorpato`

Analisi statica dei due branch, con riferimento ai controller del client, alle viste FXML, alla navigazione e ai fogli CSS. I percorsi sotto `client\src\main\...` sono quelli dell'applicazione; sono inclusi anche i CSS Javadoc presenti solo nel branch accorpato. La verifica non ha caricato le viste in un runtime JavaFX.

## 1. Inventario dei file

Gli elenchi dei controller comprendono tutte le classi Java nella cartella `client\src\main\java\org\uninsubria\theknife\client\controller`.

### Branch `nomenclatura`

**Controller Java (20)**

- `client\src\main\java\org\uninsubria\theknife\client\controller\AggiungiRistoranteController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\AreaPersonaleController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\AreaRistoranteController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\AreaRistoratoreController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\BaseLayoutController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ListaRistorantiController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ListaRistorantiGestoreController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\LoginController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\MainLayoutController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\MieRecensioniController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ModificaAccountController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ModificaRistoranteController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\PreferitiController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\PreferitoItemController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\RecensioneItemController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\RestaurantItemController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\RicercaAvanzataController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ScriviRecensioneController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ScriviRispostaController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\SignUpController.java`

**Viste FXML (20)**

- `client\src\main\resources\org\uninsubria\theknife\client\views\AggiungiRistorante.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\AreaPersonale.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\AreaRistoratore.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\BaseLayout.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\ListaRistorantiGestore.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\LoginView.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\MainLayout.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\MieRecensioni.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\ModificaAccount.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\ModificaRistorante.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\Preferiti.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\PreferitoItem.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RecensioneItem.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RestaurantItem.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RicercaAvanzataView.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RispondiRecensione.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\Ristorante.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\ScriviRecensione.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\SignUp.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\listaRistoranti.fxml`

**CSS applicativi (16)**

- `client\src\main\resources\org\uninsubria\theknife\client\styles\Ristorante-info.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\areaUtente.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\lista-ristoranti.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\login-style.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\main-layout.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\modifica-account.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\modificaRistorante.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\preferiti.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\preferito-item.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\recensione-item.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\registraRistorante.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\ricerca-avanzata.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\ristorante-item.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\scriviRecensione.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\signUp-style.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\style.css`

### Branch `clientAccorpato`

**Controller Java (14)**

- `client\src\main\java\org\uninsubria\theknife\client\controller\AreaRistoranteController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\AuthController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\BaseLayoutController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\DashboardGestoreController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\FormRistoranteController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ListaRistorantiController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\MainLayoutController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\MieRecensioniController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\PreferitiController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ProfiloController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\RecensioneItemController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\RicercaAvanzataController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\RistoranteItemController.java`
- `client\src\main\java\org\uninsubria\theknife\client\controller\ScriviRecensioneController.java`

**Viste FXML (14)**

- `client\src\main\resources\org\uninsubria\theknife\client\views\AuthView.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\BaseLayout.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\DashboardGestore.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\FormRistorante.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\MainLayout.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\MieRecensioni.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\Preferiti.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\ProfiloView.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RecensioneItem.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RicercaAvanzataView.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\Ristorante.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\RistoranteItem.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\ScriviRecensione.fxml`
- `client\src\main\resources\org\uninsubria\theknife\client\views\listaRistoranti.fxml`

**CSS applicativi (16; stessi percorsi del branch originale)**

- `client\src\main\resources\org\uninsubria\theknife\client\styles\Ristorante-info.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\areaUtente.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\lista-ristoranti.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\login-style.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\main-layout.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\modifica-account.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\modificaRistorante.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\preferiti.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\preferito-item.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\recensione-item.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\registraRistorante.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\ricerca-avanzata.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\ristorante-item.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\scriviRecensione.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\signUp-style.css`
- `client\src\main\resources\org\uninsubria\theknife\client\styles\style.css`
- `doc\javadoc\resource-files\jquery-ui.min.css`
- `doc\javadoc\resource-files\stylesheet.css`
- `doc\javadoc\script-dir\jquery-ui.min.css`
- `doc\javadoc\stylesheet.css`

I quattro CSS sotto `doc\javadoc` sono risorse generate per la documentazione, non stylesheet delle viste JavaFX.

## 2. Mappa di corrispondenza dettagliata

| Area/funzionalità | `nomenclatura` — controller, FXML e CSS | `clientAccorpato` — corrispondenza e contenuti confluiti |
|---|---|---|
| Accesso e registrazione | `LoginController` + `LoginView.fxml` + `login-style.css`; `SignUpController` + `SignUp.fxml` + `signUp-style.css` | `AuthController` + `AuthView.fxml` + `login-style.css`. Login e registrazione sono stati unificati in una vista/controller con schermate interne e funzioni di accesso/registrazione. `SignUpController` è rinominato e ampliato a `AuthController`; `LoginController` è rimosso. `signUp-style.css` rimane nel repository ma non è più referenziato. |
| Shell, home e instradamento | `BaseLayoutController` + `BaseLayout.fxml` + `main-layout.css`; `MainLayoutController` + `MainLayout.fxml` + `main-layout.css` | File e controller restano con gli stessi nomi. La shell aggiorna la navigazione a `AuthView.fxml` e `ProfiloView.fxml`; `SceneManager` usa `BaseLayout.fxml`, `AuthView.fxml`, `MainLayout.fxml` e lo stylesheet globale `style.css`. I riferimenti di destinazione individuati corrispondono a file presenti. |
| Ricerca semplice e lista risultati | `MainLayoutController` + `MainLayout.fxml`; `ListaRistorantiController` + `listaRistoranti.fxml`; CSS `main-layout.css` e `lista-ristoranti.css` | Controller e viste mantengono i nomi. Cambiano i nomi di alcuni handler (`onSearch`/`onFilterChanged`/`onResetFilters`/`onLoadMore` diventano `cerca`/`filtriCambiati`/`reimpostaFiltri`/`caricaAltri`), mantenendo ricerca, filtri, paginazione e apertura dettagli. `lista-ristoranti.css` aggiunge regole informative per la dashboard del gestore. |
| Ricerca avanzata | `RicercaAvanzataController` + `RicercaAvanzataView.fxml` + `ricerca-avanzata.css` | Invariati nei nomi. La vista continua a collegare handler per prezzo, ricerca e reset; i riferimenti FXML individuati hanno metodi corrispondenti nel controller. |
| Dettaglio ristorante e preferiti | `AreaRistoranteController` + `Ristorante.fxml` + `Ristorante-info.css` | Invariati nei nomi. La vista mantiene dati, recensioni, filtro e azioni sul ristorante; il metodo `onPreferitiClick` è pubblico e quindi accessibile dal binding FXML anche senza annotazione `@FXML`. Non emerge un percorso FXML rotto. |
| Area personale, profilo e modifica account | `AreaPersonaleController` + `AreaPersonale.fxml` + `areaUtente.css`; `ModificaAccountController` + `ModificaAccount.fxml` + `registraRistorante.css` e `modifica-account.css` | Accorpati in `ProfiloController` + `ProfiloView.fxml`, che contiene i pannelli profilo e modifica credenziali. La vista usa `areaUtente.css`, `modifica-account.css` e `modificaRistorante.css`. L'uscita e le azioni verso preferiti, recensioni e ristoranti sono integrate nel profilo. |
| Gestione ristoranti del gestore | `AreaRistoratoreController` + `AreaRistoratore.fxml` + `areaUtente.css`; `ListaRistorantiGestoreController` + `ListaRistorantiGestore.fxml` + `lista-ristoranti.css` | Le due aree sono accorpate in `DashboardGestoreController` + `DashboardGestore.fxml` + `lista-ristoranti.css`. La dashboard mostra i dati del gestore e la lista, con accesso a profilo, aggiunta/modifica e uscita. `ListaRistorantiGestore.fxml` risulta rinominato in `DashboardGestore.fxml`; il controller è sostituito. |
| Inserimento e modifica ristorante | `AggiungiRistoranteController` + `AggiungiRistorante.fxml` + `registraRistorante.css`; `ModificaRistoranteController` + `ModificaRistorante.fxml` + `modificaRistorante.css` | Riuniti in `FormRistoranteController` + `FormRistorante.fxml` + `registraRistorante.css`. Il nuovo controller contiene modalità di inserimento/modifica, caricamento e selezione ristorante, salvataggio, eliminazione, annullamento e validazioni. La vista `AggiungiRistorante.fxml` è rinominata e ampliata; `ModificaRistorante.fxml` viene rimosso. La regola CSS dedicata `.btn-delete` esiste in `modificaRistorante.css`, ma il nuovo FXML non carica quel foglio: vedi §4. |
| Preferiti e scheda ristorante riutilizzabile | `PreferitiController` + `Preferiti.fxml` + `preferiti.css`; `PreferitoItemController` + `PreferitoItem.fxml` + `preferito-item.css`; `RestaurantItemController` + `RestaurantItem.fxml` + `ristorante-item.css` | `PreferitiController` e `Preferiti.fxml` restano. La scheda dedicata ai preferiti è stata sostituita dal componente comune `RistoranteItemController` + `RistoranteItem.fxml`; `RestaurantItem` è rinominato in `RistoranteItem`. `ristorante-item.css` aggiunge `.remove-button` per l'azione di rimozione. `preferito-item.css` resta senza riferimenti. |
| Recensioni dell'utente | `MieRecensioniController` + `MieRecensioni.fxml` + `preferiti.css` | Controller e vista restano; sono mantenute caricamento, modifica ed eliminazione delle recensioni. Il CSS associato resta lo stesso. |
| Singola recensione e risposta del gestore | `RecensioneItemController` + `RecensioneItem.fxml` + `recensione-item.css`; `ScriviRecensioneController` + `ScriviRecensione.fxml` + `scriviRecensione.css`; `ScriviRispostaController` + `RispondiRecensione.fxml` + `scriviRecensione.css` | Il controller della singola recensione resta e viene aggiornato. Inserimento/modifica recensione e risposta sono consolidati in `ScriviRecensioneController` e `ScriviRecensione.fxml`: il controller rileva la modalità risposta, configura la stessa UI e instrada l'invio alla logica di risposta. `RispondiRecensione.fxml` e `ScriviRispostaController` sono rimossi. `RecensioneItemController.rispondiCliccato` salva la recensione selezionata e naviga alla vista consolidata. |
| Oggetto ristorante e gestione recensioni | `RecensioneItemController` + `RecensioneItem.fxml`; `RestaurantItemController` + `RestaurantItem.fxml` | `RecensioneItem` resta associato alla stessa vista; cambiano i nomi di handler ma i metodi collegati sono presenti. Il controller ristorante diventa `RistoranteItemController` e viene riutilizzato per dettagli, modifica e rimozione. |

### Riepilogo rinomine, accorpamenti e rimozioni

- **Accorpati:** login + registrazione; area personale + modifica account; pannello ristoratore + lista gestore; inserimento + modifica ristorante; composizione/invio risposta + form recensione; componente preferiti + scheda ristorante generica.
- **Rinominati:** `SignUpController` → `AuthController`; `AggiungiRistorante.fxml` → `FormRistorante.fxml`; `ListaRistorantiGestore.fxml` → `DashboardGestore.fxml`; `RestaurantItemController`/`RestaurantItem.fxml` → `RistoranteItemController`/`RistoranteItem.fxml`.
- **Lasciati invariati nel nome:** shell, home, ricerca semplice/avanzata, dettaglio ristorante, preferiti, recensioni utente e item recensione.
- **Rimossi come file autonomi:** i controller e le viste sostituiti dai nuovi accorpamenti; non risultano controller o viste residui non agganciati alle viste attuali.

## 3. Coerenza viste-controller in `clientAccorpato`

### `fx:controller`, campi e handler

- Tutte le 14 viste FXML dichiarano un `fx:controller` esistente nel package `org.uninsubria.theknife.client.controller`; non sono emersi controller FXML mancanti.
- Tutti gli `onAction="#metodo"` individuati hanno un metodo con quel nome nel controller associato. Gli handler privati sono annotati `@FXML`; `AreaRistoranteController.onPreferitiClick` è pubblico, quindi non richiede l'annotazione per essere invocato dal loader.
- Non tutti gli `fx:id` hanno un corrispondente campo controller. Gli ID senza campo annotato `@FXML` sono:

| FXML | `fx:id` senza campo corrispondente |
|---|---|
| `FormRistorante.fxml` | `containerRistoranti`, `btnAnnulla` |
| `MainLayout.fxml` | `btnCerca`, `btnRicercaAvanzata` |
| `RicercaAvanzataView.fxml` | `btnCerca`, `btnReimposta` |
| `Ristorante.fxml` | `reviewScrollPane` |
| `ScriviRecensione.fxml` | `btnAnnulla` |
| `listaRistoranti.fxml` | `searchButton`, `resetFiltersButton` |

Un ID FXML senza campo non viene iniettato nel controller. Per i pulsanti con `onAction` questo non impedisce il binding degli handler; per gli ID che il controller dovesse leggere direttamente, invece, l'iniezione non avverrebbe. L'analisi statica non ha rilevato un errore di destinazione degli handler, ma l'eventuale inutilizzo intenzionale dei singoli nodi va confermato caricando le viste.

### Navigazione e file orfani

- Le stringhe di percorso `.fxml` individuate nei controller e in `SceneManager` risolvono a viste presenti nel branch.
- `SceneManager` indica come viste principali `BaseLayout.fxml`, `AuthView.fxml` e `MainLayout.fxml`; il CSS globale `style.css` esiste. Non risultano riferimenti alla vecchia `LoginView.fxml`, alle altre viste eliminate o a destinazioni di navigazione inesistenti.
- Ogni controller della cartella controller è associato a una vista tramite `fx:controller`; non risultano controller nuovi orfani. Le viste attuali sono raggiunte dalla shell o dai controller/item che le usano.
- Le funzioni riconoscibili dai metodi sono state ricondotte ai nuovi controller: risposta al gestore, gestione CRUD dei ristoranti, profilo/credenziali, preferiti e recensioni. Non emerge una perdita certa dai riferimenti statici; la parità del comportamento non è certificabile senza esecuzione.

## 4. Gestione CSS in `clientAccorpato`

### Riferimenti e file non utilizzati

Tutti i riferimenti `.css` nei FXML e in `SceneManager` corrispondono a file presenti. Gli stylesheet applicativi sono:

| CSS | Riferimenti correnti |
|---|---|
| `Ristorante-info.css` | `Ristorante.fxml` |
| `areaUtente.css` | `ProfiloView.fxml` |
| `lista-ristoranti.css` | `DashboardGestore.fxml`, `listaRistoranti.fxml` |
| `login-style.css` | `AuthView.fxml` |
| `main-layout.css` | `BaseLayout.fxml`, `MainLayout.fxml` |
| `modifica-account.css` | `ProfiloView.fxml` |
| `modificaRistorante.css` | `ProfiloView.fxml` |
| `preferiti.css` | `MieRecensioni.fxml`, `Preferiti.fxml` |
| `preferito-item.css` | nessuno |
| `recensione-item.css` | `RecensioneItem.fxml` |
| `registraRistorante.css` | `FormRistorante.fxml` |
| `ricerca-avanzata.css` | `RicercaAvanzataView.fxml` |
| `ristorante-item.css` | `RistoranteItem.fxml` |
| `scriviRecensione.css` | `ScriviRecensione.fxml` |
| `signUp-style.css` | nessuno |
| `style.css` | `SceneManager` (foglio globale) |

`preferito-item.css` e `signUp-style.css` sono fogli residui non più referenziati, coerentemente con la rimozione rispettivamente del componente preferiti autonomo e della vista di registrazione separata. Non risultano percorsi CSS rotti. I CSS in `doc\javadoc` sono risorse documentali, non stylesheet caricati dalle viste JavaFX.

### Disallineamenti tra `styleClass` e selettori CSS

- **Accorpamento autenticazione:** `AuthView.fxml` usa `error-label` e `hint-label`, ma `login-style.css` non definisce tali classi. Le regole corrispondenti sono in `signUp-style.css`, ora orfano e non caricato: messaggi di errore e suggerimenti della registrazione possono quindi perdere lo stile previsto.
- **Accorpamento form ristorante:** `FormRistorante.fxml` applica `btn-delete`, assente da `registraRistorante.css`; la regola si trova in `modificaRistorante.css`, non incluso dal form. Il pulsante di eliminazione della modalità modifica può risultare senza lo stile specifico, nonostante il foglio corretto sia ancora caricato in `ProfiloView.fxml`.
- **Disallineamenti presenti anche prima dell'accorpamento:** `Preferiti.fxml` e `MieRecensioni.fxml` usano `content-box`, `restaurant-container`, `restaurant-scroll-pane` e `results-count`, non definiti in `preferiti.css`; `BaseLayout.fxml` usa `base-layout`, non definita in `main-layout.css`. Altri casi nei fogli non modificati sono `actions-row`/`rating-row` in `RecensioneItem.fxml`, `field-group` in `RicercaAvanzataView.fxml` e `reviews-box` in `Ristorante.fxml`. Sono classi senza regole nei CSS associati e meritano una verifica visiva, ma non risultano introdotte dai cambiamenti CSS di questo accorpamento.
- `ProfiloView.fxml` carica tre stylesheet e, considerando tutti e tre, le sue classi risultano definite.
- Nei CSS applicativi non risultano selettori ID (`#id`); i caratteri `#` presenti nei fogli sono valori colore. Non emerge quindi un disallineamento tra selettori CSS ID e nodi FXML.

### Modifiche ai fogli CSS

- `lista-ristoranti.css` aggiunge selettori per i dati utente della dashboard (`user-info-box`, `user-info-label`, `user-info-value`).
- `ristorante-item.css` aggiunge lo stile del pulsante di rimozione (`remove-button`) per il componente ristorante riutilizzato.
- `modificaRistorante.css` aggiunge il raggio agli angoli di `.root-pane`.
- Gli altri CSS applicativi restano agli stessi percorsi e non risultano rinominati o eliminati. I due file residui non utilizzati sono presenti, non rimossi.

## Esito statico

Il branch accorpato riduce da 20 a 14 controller e da 20 a 14 viste, mantenendo i 16 CSS applicativi agli stessi percorsi. I riferimenti di controller, navigazione e risorse esaminati risolvono; gli handler FXML sono presenti. Restano da verificare a runtime gli `fx:id` senza campo, gli stili mancanti dell'autenticazione e del pulsante di eliminazione, oltre ai disallineamenti preesistenti elencati sopra.
