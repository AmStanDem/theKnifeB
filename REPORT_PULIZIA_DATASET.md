# Report di pulizia del dataset Michelin

## 1. Scopo

Il file `server/src/main/resources/dataset/michelin_my_maps_clean.csv`, importato a runtime da
`CsvImporter`, conteneva diversi problemi di qualità del dato: numeri di telefono in notazione
scientifica (con perdita di cifre), URL malformati, campi invertiti, etichette di paese incoerenti
e città-stato prive del paese. Questo documento descrive le anomalie individuate, le correzioni
applicate e come riprodurle.

- **File sorgente (intatto):** `server/src/main/resources/dataset/michelin_my_maps.csv`
- **File corretto (importato dall'app):** `server/src/main/resources/dataset/michelin_my_maps_clean.csv`
- **Script di rigenerazione:** `tools/clean_michelin_dataset.py`
- **Codice reso difensivo:** `server/src/main/java/org/uninsubria/theknife/server/utils/CsvImporter.java`

Dataset: **17.737 record**, 14 colonne (`Name, Address, Location, Price, Cuisine, Longitude,
Latitude, PhoneNumber, Url, WebsiteUrl, Award, GreenStar, FacilitiesAndServices, Description`).

## 2. Metodologia e provenienza dei dati

Il dataset deriva dal progetto open source [`ngshiheng/michelin-my-maps`](https://github.com/ngshiheng/michelin-my-maps)
(licenza MIT), a sua volta generato dallo scraping di `guide.michelin.com`.

Confrontando il file locale con lo storico del repository è stato individuato lo snapshot
**identico** al dataset di partenza:

| Proprietà | Valore |
|---|---|
| Commit | `d521a7d8e1ff4b48125c5333f75e8b77d83e70cc` |
| Data | 2025-02-01 |
| Record | 17.737 |
| URL di guida corrispondenti | 100% (17.737/17.737) |

Questa corrispondenza permette di recuperare i valori **originali e completi** dei campi corrotti
agganciando i record per `Url`. In particolare, per i numeri di telefono non troncati si è
verificato che **tutti i 12.145 telefoni non in notazione scientifica coincidono esattamente** con
lo snapshot (a meno del prefisso `+`): questo prova che lo snapshot è la fonte corretta.

## 3. Anomalie individuate

### 3.1 Telefoni in notazione scientifica — 5.229 record (gravità alta)

Il file è passato attraverso un foglio di calcolo (Excel), che ha interpretato i numeri di telefono
come valori numerici. Ciò ha prodotto due effetti:

1. rimozione del prefisso internazionale `+`;
2. riscrittura dei numeri lunghi in **notazione scientifica con 6 cifre significative**, perdendo
   da 6 a 10 cifre finali.

Esempi (valore nel file → valore reale recuperato):

| Ristorante | Valore corrotto | Valore corretto |
|---|---|---|
| Amador | `4.36609E+11` | `436609070500` |
| O Frade | `3.51939E+11` | `351939482939` |
| Tokumaru | `8.18092E+11` | `818091690331` |
| Taian Table | `8.6173E+12` | `8617301605350` |

La semplice espansione della notazione scientifica (`4.36609E+11` → `436609000000`) **non è
sufficiente**: le cifre finali sono definitivamente perse nel file locale. Per questo i valori sono
stati recuperati dallo snapshot sorgente tramite l'`Url` di guida.

### 3.2 URL del sito web malformati — 13 record nel file originale

Il file originale conteneva 13 URL con lo schema errato `httpss`. La "pulizia" precedente ne aveva
corretti 12 con una sostituzione `httpss://` → `https://`, ma **un record è stato peggiorato**:
per `O Frade` lo schema `httpss//` (senza `:`) non è stato riconosciuto e al valore è stato anteposto
`http://`, producendo `http://httpss//fraderestaurante.com`.

| Ristorante | Valore precedente | Valore corretto |
|---|---|---|
| O Frade | `http://httpss//fraderestaurante.com` | `https://fraderestaurante.com` |

### 3.3 Campo invertito — 1 record

Per `Tokumaru` la colonna `Cuisine` conteneva una **copia dell'indirizzo** e il prezzo era assente
(errore di allineamento delle colonne nella sorgente):

| Colonna | Valore precedente | Valore corretto |
|---|---|---|
| `Cuisine` | `406 Kameyacho, Nakagyo-ku, Kyoto, 604-0941, Japan` | `Japanese` |
| `Price` | *(vuoto)* | `¥¥` |

I valori corretti provengono dalla pagina ufficiale della Guida Michelin, verificata durante
l'analisi.

### 3.4 Etichette di paese incoerenti — 5 record

La stessa città (`Prague`) compariva con due etichette di paese diverse:

| Etichetta | Record |
|---|---|
| `Czechia` | 20 |
| `Czech Republic` | 5 |

Tutte le occorrenze di `Czech Republic` sono state uniformate a `Czechia`.

### 3.5 Paese mancante per le città-stato — 610 record

La colonna `Location` conteneva un solo componente (la città) per alcune città-stato, senza il
paese. Questo ha un impatto concreto: `CsvImporter` assegnava loro il paese `Non specificata`,
alterando anche la stima del prezzo (`PrezzoDatasetEstimator` usa il paese come coefficiente
economico).

| `Location` originale | Record | `Location` corretta |
|---|---|---|
| `Singapore` | 288 | `Singapore, Singapore` |
| `Hong Kong` | 102 | `Hong Kong, Hong Kong SAR China` |
| `Dubai` | 102 | `Dubai, United Arab Emirates` |
| `Macau` | 50 | `Macau, Macau SAR China` |
| `Abu Dhabi` | 48 | `Abu Dhabi, United Arab Emirates` |
| `Luxembourg` | 20 | `Luxembourg, Luxembourg` |

### 3.6 Controlli eseguiti senza anomalie

Per completezza, sono state verificate e **scartate** le seguenti ipotesi di errore:

- scambio `Longitude`/`Latitude` (nessun caso, nemmeno con analisi per paese);
- righe duplicate (per chiave `Name+Address+Location` o righe intere);
- numero di campi per riga (tutte le 17.737 righe hanno esattamente 14 campi);
- campi spostati/invertiti residui oltre a `Tokumaru` (nessuna cella duplicata nella stessa riga);
- valuta incoerente col paese (i simboli `$`, `€`, `£`, `¥`, … seguono la convenzione della Guida);
- caratteri di controllo, BOM, CRLF o tabulazioni (il file usa solo LF, UTF-8 senza BOM);
- righe con coordinate nulle o fuori range.

## 4. Correzioni applicate

Lo script `tools/clean_michelin_dataset.py` rigenera il file pulito dal file originale applicando,
in ordine:

1. **Telefoni** — recupero delle cifre complete dallo snapshot (aggancio per `Url`); in assenza di
   snapshot, espansione *best effort* della notazione scientifica.
2. **URL** — normalizzazione degli schemi malformati (`httpss://`, `httpss//`, `http://httpss//`,
   `https//`, `http//`) e aggiunta di `https://` quando lo schema è assente, preservando gli
   `http://` validi già presenti.
3. **Location** — normalizzazione delle etichette di paese e aggiunta del paese alle città-stato.
4. **Campi invertiti** — correzione puntuale di `Tokumaru`.

### Riepilogo delle modifiche

| Categoria | Record corretti |
|---|---|
| Telefoni in notazione scientifica | 5.229 |
| Paese città-stato (`Location`) | 610 |
| Etichetta di paese `Czech Republic` → `Czechia` | 5 |
| URL malformato (`O Frade`) | 1 |
| Campo invertito/assente (`Tokumaru`) | 1 |

Nessun'altra colonna è stata modificata: i campi `FacilitiesAndServices`, `GreenStar`, `Description`,
`Award`, `Price`, `Cuisine` ecc. restano invariati (oltre a `Tokumaru`).

### Impatti

- I numeri di telefono sono ora cifre complete e importabili come testo (nessuna notazione
  scientifica, nessun `+` per coerenza con il formato preesistente delle righe già valide).
- Le 610 città-stato hanno ora un paese valido: la stima del prezzo usa il coefficiente corretto
  (es. `Luxembourg` entra nella fascia "very high cost") e la generazione degli account gestore per
  città non ricade più in `Non specificata`.

## 5. Modifiche al codice (`CsvImporter`)

Anche il codice di importazione è stato reso robusto, così da non dipendere più dalla perfezione del
file CSV. In `CsvImporter.java`:

- `analizzaLocalita(String)` — scompone `Location` in città/paese, assegna il paese alle città-stato
  e normalizza gli alias di paese; usato da `caricaStaging` e `aggiornaContattiDataset`.
- `normalizzaTelefono(String)` — espande la notazione scientifica con `BigDecimal` e riduce il
  valore alle sole cifre; applicato a `telefono` sia in inserimento sia in aggiornamento.
- `normalizzaUrl(String)` — corregge gli schemi malformati e aggiunge `https://` se assente.

In questo modo, anche fornendo un CSV non ripulito, il database viene popolato con valori corretti.

## 6. Riproducibilità

```bash
# Rigenera il file clean (scarica lo snapshot di riferimento la prima volta)
python3 tools/clean_michelin_dataset.py

# Senza rete: espande solo la notazione scientifica (con perdita di precisione)
python3 tools/clean_michelin_dataset.py --no-download
```

Lo snapshot di riferimento viene salvato in `$TMPDIR/michelin_reference.csv` ed è ancorato al commit
`d521a7d8e1ff4b48125c5333f75e8b77d83e70cc`.

## 7. Verifiche finali

Sul file rigenerato:

- 0 numeri di telefono in notazione scientifica;
- 0 URL con schema malformato;
- 0 `Location` prive di paese;
- 0 occorrenze di `Czech Republic`;
- telefoni corrispondenti allo snapshot su tutte le 17.737 righe;
- 17.737 record, 14 colonne per riga, invariati gli altri campi.
