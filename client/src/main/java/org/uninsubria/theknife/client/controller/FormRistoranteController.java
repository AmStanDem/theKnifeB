/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.components.CuisineSelector;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

/**
 * Controller JavaFX del form unificato per l'inserimento e la modifica di un ristorante.
 * <p>
 * In modalità creazione si comporta come {@code AggiungiRistoranteController},
 * in modalità modifica come {@code ModificaRistoranteController}. La modalità
 * viene determinata automaticamente in base alla presenza di un ristorante
 * selezionato in {@link RistoranteSelezionato}.
 * </p>
 */
public class FormRistoranteController {

    @FXML
    private TextField txtNome;

    @FXML
    private ComboBox<RistoranteDTO> comboRistoranti;

    @FXML
    private LuogoInput txtIndirizzo;

    @FXML
    private TextField txtCitta;

    @FXML
    private TextField txtNazione;

    @FXML
    private TextField txtPrezzoMedio;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtSitoWeb;

    @FXML
    private TextArea txtDescrizione;

    @FXML
    private CuisineSelector comboCucine;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkBookingOnline;

    @FXML
    private Button btnSalva;

    @FXML
    private Button btnElimina;

    private boolean modalitaModifica = false;
    private boolean tipologieCaricate;
    private boolean ristorantiCaricati;
    private RistoranteDTO ristorante;

    /**
     * Inizializza i componenti della vista.
     * <p>
     * Determina automaticamente la modalità: se
     * {@link RistoranteSelezionato#getRistorante()} restituisce un ristorante
     * con identificatore, si entra in modalità modifica; altrimenti in modalità
     * creazione. Configura i campi, i listener e avvia i caricamenti asincroni.
     * </p>
     */
    @FXML
    public void initialize() {
        RistoranteDTO selezionato = RistoranteSelezionato.getRistorante();

        if (selezionato != null && selezionato.idRistorante() != null) {
            modalitaModifica = true;
        }

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isGestore()) {
            btnSalva.setDisable(true);
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Accesso non consentito",
                    "Devi accedere con un account ristoratore per gestire un ristorante."
            );
            return;
        }

        comboCucine.setDisable(true);
        btnSalva.setDisable(true);

        if (modalitaModifica) {
            comboRistoranti.setVisible(true);
            comboRistoranti.setManaged(true);
            btnElimina.setVisible(true);
            btnElimina.setManaged(true);

            comboRistoranti.setConverter(new StringConverter<>() {
                @Override
                public String toString(RistoranteDTO value) {
                    return value == null ? "" : value.nome() + " — " + value.citta();
                }

                @Override
                public RistoranteDTO fromString(String value) {
                    return null;
                }
            });
            comboRistoranti.setOnAction(event -> selezionaRistorante(comboRistoranti.getValue()));
        } else {
            comboRistoranti.setVisible(false);
            comboRistoranti.setManaged(false);
            btnElimina.setVisible(false);
            btnElimina.setManaged(false);
        }

        txtIndirizzo.addTextFieldStyleClass("custom-text-field");
        txtCitta.setEditable(false);
        txtNazione.setEditable(false);
        txtIndirizzo.setOnLuogoSelezionato(luogo -> {
            txtCitta.setText(luogo.localita());
            txtNazione.setText(luogo.nazione());
        });

        caricaTipologieCucina();

        if (modalitaModifica) {
            caricaRistoranti(utente);
        }
    }

    /**
     * Recupera dal server tutte le tipologie di cucina disponibili.
     * <p>
     * L'operazione viene eseguita in un thread virtuale; una volta ottenuto
     * il risultato, i componenti JavaFX vengono aggiornati sul thread
     * dell'applicazione. In caso di errore viene visualizzato un messaggio
     * all'utente.
     * </p>
     */
    private void caricaTipologieCucina() {
        Thread.startVirtualThread(() -> {
            try {
                List<String> tipologie = ServerConnection.getServer().ottieniTutteTipologieCucina();
                Platform.runLater(() -> {
                    comboCucine.setCuisines(tipologie);
                    comboCucine.setDisable(false);
                    tipologieCaricate = true;
                    if (modalitaModifica) {
                        aggiornaTipologieSelezionate();
                        aggiornaStatoSalvataggio();
                    } else {
                        btnSalva.setDisable(false);
                    }
                });
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                System.err.println("[RESTAURANT_FORM_ERROR] Impossibile caricare le tipologie cucina: "
                        + e.getMessage());
                Platform.runLater(() -> mostraMessaggio(
                        Alert.AlertType.ERROR,
                        "Catalogo cucine non disponibile",
                        "Non è stato possibile caricare le tipologie di cucina dal server. "
                                + "Riapri il modulo quando il server è disponibile."
                ));
            }
        });
    }

    /**
     * Carica dal server i ristoranti associati al gestore autenticato (solo modifica).
     * <p>
     * Popola il selettore sul thread JavaFX e seleziona il ristorante già
     * memorizzato dalla navigazione precedente, se presente tra i risultati.
     * </p>
     *
     * @param utente gestore proprietario dei ristoranti
     */
    private void caricaRistoranti(UtenteDTO utente) {
        Thread.startVirtualThread(() -> {
            try {
                List<RistoranteDTO> ristoranti =
                        ServerConnection.getServer().getRistorantiGestore(utente.idUtente());
                Platform.runLater(() -> {
                    comboRistoranti.getItems().setAll(ristoranti);
                    ristorantiCaricati = true;
                    if (ristoranti.isEmpty()) {
                        mostraMessaggio(Alert.AlertType.INFORMATION, "Nessun ristorante",
                                "Non hai ancora ristoranti da modificare.");
                    } else {
                        RistoranteDTO selezionato = RistoranteSelezionato.getRistorante();
                        RistoranteDTO ristoranteInLista = selezionato == null ? null
                                : ristoranti.stream()
                                .filter(item -> item.idRistorante().equals(selezionato.idRistorante()))
                                .findFirst()
                                .orElse(null);
                        RistoranteDTO scelta = ristoranteInLista == null
                                ? ristoranti.getFirst() : ristoranteInLista;
                        comboRistoranti.setValue(scelta);
                        selezionaRistorante(scelta);
                    }
                    aggiornaStatoSalvataggio();
                });
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                System.err.println("[RESTAURANT_FORM_ERROR] Impossibile caricare i ristoranti: "
                        + e.getMessage());
                Platform.runLater(() -> mostraMessaggio(
                        Alert.AlertType.ERROR,
                        "Ristoranti non disponibili",
                        "Non è stato possibile recuperare i ristoranti dal server."
                ));
            }
        });
    }

    /**
     * Seleziona un ristorante e popola i campi del modulo (solo modifica).
     *
     * @param selezionato ristorante scelto dall'elenco
     */
    private void selezionaRistorante(RistoranteDTO selezionato) {
        ristorante = selezionato;
        if (selezionato == null) {
            return;
        }
        popolaCampi();
        aggiornaTipologieSelezionate();
        aggiornaStatoSalvataggio();
    }

    /**
     * Copia i dati del ristorante selezionato nei componenti della vista (solo modifica).
     */
    private void popolaCampi() {
        txtNome.setText(ristorante.nome());
        txtCitta.setText(ristorante.citta());
        txtNazione.setText(ristorante.nazione());
        txtPrezzoMedio.setText(String.valueOf(ristorante.prezzoMedio()));
        chkDelivery.setSelected(Boolean.TRUE.equals(ristorante.delivery()));
        chkBookingOnline.setSelected(Boolean.TRUE.equals(ristorante.bookingOnline()));

        txtTelefono.setText(ristorante.telefono() != null
                ? ristorante.telefono() : "");

        txtSitoWeb.setText(ristorante.sitoWeb() != null
                ? ristorante.sitoWeb() : "");

        txtDescrizione.setText(ristorante.descrizione() != null
                ? ristorante.descrizione() : "");

        if (ristorante.latitudine() != null && ristorante.longitudine() != null) {
            txtIndirizzo.setLuogoSelezionato(new GeocodingService.Luogo(
                    ristorante.indirizzo(),
                    ristorante.indirizzo(),
                    ristorante.citta(),
                    ristorante.nazione(),
                    ristorante.latitudine(),
                    ristorante.longitudine()
            ));
        } else {
            txtIndirizzo.setText(ristorante.indirizzo());
        }
    }

    /**
     * Imposta nel selettore delle cucine le tipologie del ristorante selezionato (solo modifica).
     */
    private void aggiornaTipologieSelezionate() {
        comboCucine.setSelectedCuisines(
                ristorante == null ? List.of() : ristorante.tipologieCucina());
    }

    /**
     * Aggiorna lo stato del pulsante di salvataggio (solo modifica).
     * <p>
     * Il pulsante è disponibile solo dopo il caricamento dei ristoranti e delle
     * tipologie e quando è stato selezionato un ristorante valido.
     * </p>
     */
    private void aggiornaStatoSalvataggio() {
        boolean disabilitato = !tipologieCaricate
                || !ristorantiCaricati
                || ristorante == null;

        btnSalva.setDisable(disabilitato);
        btnElimina.setDisable(disabilitato);
    }

    /**
     * Gestisce il click sul pulsante di salvataggio.
     * <p>
     * Verifica che il catalogo delle cucine sia stato caricato, che i campi
     * obbligatori siano compilati e che l'utente corrente abbia il ruolo
     * di gestore. Se i controlli preliminari sono superati, risolve la
     * località inserita prima di procedere con il salvataggio.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    public void onSalvaClick(ActionEvent event) {
        if (!tipologieCaricate) {
            mostraMessaggio(
                    Alert.AlertType.WARNING,
                    "Catalogo cucine non disponibile",
                    "Attendi il caricamento delle tipologie di cucina prima di salvare."
            );
            return;
        }

        // 1. Verifica che tutti i campi obbligatori siano compilati.
        if (txtNome.getText().trim().isEmpty()
                || txtIndirizzo.getText().trim().isEmpty()
                || txtPrezzoMedio.getText().trim().isEmpty()) {

            mostraMessaggio(
                    Alert.AlertType.WARNING,
                    "Campi mancanti",
                    "Compila tutti i campi obbligatori (*)."
            );

            return;
        }

        // 2. Verifica che sia presente un gestore autenticato.
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();

        if (utente == null || !SessioneUtente.isGestore()) {
            mostraMessaggio(
                    Alert.AlertType.WARNING,
                    "Accesso non consentito",
                    "Devi accedere con un account gestore per aggiungere un ristorante."
            );
            return;
        }

        txtIndirizzo.risolviLuogo(
                luogo -> salvaRistorante(event, utente, luogo),
                messaggio -> mostraMessaggio(Alert.AlertType.WARNING, "Località non valida", messaggio)
        );
    }

    /**
     * Valida i dati del modulo e registra o modifica il ristorante tramite RMI.
     * <p>
     * In modalità creazione chiama {@code registraNuovoRistorante()},
     * in modalità modifica chiama {@code modificaRistorante()}.
     * </p>
     *
     * @param event          evento JavaFX usato per cambiare scena
     * @param utente         utente gestore autenticato
     * @param luogoSelezionato luogo risolto dal servizio di geocoding
     */
    private void salvaRistorante(
            ActionEvent event,
            UtenteDTO utente,
            GeocodingService.Luogo luogoSelezionato
    ) {
        try {
            String nome = txtNome.getText().trim();
            String indirizzo = luogoSelezionato.indirizzo();
            String citta = luogoSelezionato.localita();
            String nazione = luogoSelezionato.nazione();
            String telefono = testoFacoltativo(txtTelefono.getText());
            String sitoWeb = testoFacoltativo(txtSitoWeb.getText());
            String descrizione = testoFacoltativo(txtDescrizione.getText());
            boolean delivery = chkDelivery.isSelected();
            boolean bookingOnline = chkBookingOnline.isSelected();
            double prezzoMedio = Double.parseDouble(
                    txtPrezzoMedio.getText().trim().replace(",", ".")
            );
            double latitudine = luogoSelezionato.latitudine();
            double longitudine = luogoSelezionato.longitudine();

            // 4. Controllo dei valori inseriti.
            if (!Double.isFinite(prezzoMedio)
                    || !Double.isFinite(latitudine)
                    || !Double.isFinite(longitudine)) {

                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Dati non validi",
                        "Inserisci valori numerici finiti."
                );
                return;
            }

            if (prezzoMedio < 0) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Dati non validi",
                        "Il prezzo medio non può essere negativo."
                );
                return;
            }

            if (latitudine < -90 || latitudine > 90) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Dati non validi",
                        "La latitudine deve essere compresa tra -90 e 90."
                );
                return;
            }

            if (longitudine < -180 || longitudine > 180) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Dati non validi",
                        "La longitudine deve essere compresa tra -180 e 180."
                );
                return;
            }

            // 5. Recupero delle tipologie di cucina selezionate.
            List<String> cucineSelezionate = comboCucine.getSelectedCuisines();

            if (cucineSelezionate.isEmpty()) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Selezione mancante",
                        "Seleziona almeno una tipologia di cucina."
                );
                return;
            }

            if (!telefonoValido(telefono)) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Numero di telefono non valido",
                        "Inserisci un numero valido con almeno 7 e massimo 15 cifre."
                );
                txtTelefono.requestFocus();
                return;
            }

            if (!sitoWebValido(sitoWeb)) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Sito web non valido",
                        "Inserisci un indirizzo web valido, ad esempio www.ristorante.it."
                );

                txtSitoWeb.requestFocus();
                return;
            }

            // Normalizzazione del sito web.
            if (sitoWeb != null
                    && !sitoWeb.matches("(?i)^https?://.*")) {
                sitoWeb = "https://" + sitoWeb;
            }

            if (modalitaModifica && ristorante != null) {
                // MODIFICA: usa l'id e la media stelle del ristorante originale
                RistoranteDTO aggiornato = new RistoranteDTO(
                        ristorante.idRistorante(),
                        nome,
                        indirizzo,
                        citta,
                        nazione,
                        latitudine,
                        longitudine,
                        cucineSelezionate,
                        prezzoMedio,
                        delivery,
                        bookingOnline,
                        ristorante.mediaStelle(),
                        ristorante.prezzoStimato(),
                        telefono,
                        sitoWeb,
                        descrizione
                );

                ServerConnection.getServer().modificaRistorante(aggiornato, utente.idUtente());

                mostraMessaggio(
                        Alert.AlertType.INFORMATION,
                        "Modifica completata",
                        "Il ristorante \"" + nome + "\" è stato aggiornato."
                );
            } else {
                // CREAZIONE
                RistoranteDTO ristorante = new RistoranteDTO(
                        null,               // ID generato dal database
                        nome,
                        indirizzo,
                        citta,
                        nazione,
                        latitudine,
                        longitudine,
                        cucineSelezionate,
                        prezzoMedio,
                        delivery,
                        bookingOnline,
                        null,               // Media stelle iniziale
                        false,
                        telefono,
                        sitoWeb,
                        descrizione
                );

                ServerConnection.getServer().registraNuovoRistorante(
                        ristorante,
                        utente.idUtente()
                );

                mostraMessaggio(
                        Alert.AlertType.INFORMATION,
                        "Successo",
                        "Il ristorante \"" + nome
                                + "\" è stato registrato con successo."
                );
            }

            tornaAiMieiRistoranti(event);

        } catch (NumberFormatException e) {
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Errore di formato",
                    "Il prezzo medio deve essere un valore numerico valido."
            );

        } catch (RemoteException | NotBoundException | OperazioneNonConsentitaException
                 | DatiMancantiException | SistemaIndisponibileException e) {
            System.err.println("[RESTAURANT_FORM_ERROR] Impossibile salvare il ristorante: "
                    + e.getMessage());
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Errore di connessione",
                    "Impossibile comunicare con il server. "
                            + "Verifica che il server sia avviato e riprova."
            );

        } catch (Exception e) {
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Errore durante il salvataggio",
                    "Non è stato possibile completare l'operazione. "
                            + "Controlla i dati e riprova."
            );

            e.printStackTrace();
        }
    }

    /**
     * Gestisce il click sul pulsante di eliminazione (solo modifica).
     * <p>
     * Mostra una finestra di conferma prima di eliminare definitivamente
     * il ristorante selezionato.
     * </p>
     *
     * @param event evento JavaFX usato per cambiare scena dopo l'eliminazione
     */
    @FXML
    public void onEliminaClick(ActionEvent event) {
        if (ristorante == null) {
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        SceneManager.initOwner(conferma);
        conferma.setTitle("Elimina ristorante");
        conferma.setHeaderText("Eliminare " + ristorante.nome() + "?");
        conferma.setContentText(
                "Questa operazione eliminerà definitivamente il ristorante, "
                        + "le recensioni, le risposte e i preferiti associati.\n\n"
                        + "L'operazione non può essere annullata. Vuoi continuare?"
        );

        ButtonType elimina = new ButtonType(
                "Elimina",
                ButtonBar.ButtonData.OK_DONE
        );

        conferma.getButtonTypes().setAll(
                elimina,
                ButtonType.CANCEL
        );

        Optional<ButtonType> risultato = conferma.showAndWait();

        if (risultato.isPresent() && risultato.get() == elimina) {
            if (eliminaRistorante()) {
                tornaAiMieiRistoranti(event);
            }
        }
    }

    /**
     * Elimina il ristorante selezionato tramite RMI.
     *
     * @return {@code true} se l'eliminazione è riuscita, {@code false} altrimenti
     */
    private boolean eliminaRistorante() {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();

        if (utente == null || !SessioneUtente.isGestore()) {
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Accesso non consentito",
                    "Devi accedere con un account ristoratore per eliminare un ristorante."
            );
            return false;
        }

        try {
            ServerConnection.getServer().eliminaRistorante(
                    ristorante.idRistorante(),
                    utente.idUtente()
            );

            mostraMessaggio(
                    Alert.AlertType.INFORMATION,
                    "Eliminazione completata",
                    "Il ristorante \"" + ristorante.nome() + "\" è stato eliminato."
            );

            return true;

        } catch (RemoteException | NotBoundException
                 | OperazioneNonConsentitaException
                 | RistoranteNonTrovatoException
                 | SistemaIndisponibileException e) {

            System.err.println(
                    "[RESTAURANT_FORM_ERROR] Impossibile eliminare il ristorante: "
                            + e.getMessage()
            );

            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Errore durante l'eliminazione",
                    "Il ristorante non è stato eliminato. Verifica la connessione e riprova."
            );

            return false;
        }
    }

    /**
     * Gestisce il click sul pulsante di annullamento.
     * <p>
     * Chiude il modulo senza salvare e torna alla lista dei ristoranti
     * gestiti dall'utente.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    public void onAnnullaClick(ActionEvent event) {
        tornaAiMieiRistoranti(event);
    }

    /**
     * Mostra la lista dei ristoranti del gestore nella scena corrente.
     *
     * @param event evento JavaFX contenente l'origine del cambio scena
     */
    private void tornaAiMieiRistoranti(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/DashboardGestore.fxml"
        );
    }

    /**
     * Visualizza una finestra di dialogo con il messaggio indicato.
     *
     * @param alertType tipo di avviso da mostrare
     * @param titolo    titolo della finestra di dialogo
     * @param contenuto testo informativo da presentare all'utente
     */
    private void mostraMessaggio(
            Alert.AlertType alertType,
            String titolo,
            String contenuto
    ) {
        Alert alert = new Alert(alertType);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    /**
     * Normalizza il valore di un campo facoltativo.
     *
     * @param valore testo inserito dall'utente
     * @return il testo privo di spazi iniziali e finali, oppure {@code null}
     *         se il valore è vuoto
     */
    private String testoFacoltativo(String valore) {
        return valore == null || valore.isBlank() ? null : valore.trim();
    }

    /**
     * Verifica la validità formale di un numero di telefono.
     * <p>
     * Il campo può essere vuoto. Quando valorizzato, deve contenere da 7
     * a 15 cifre e può includere il prefisso internazionale, spazi,
     * parentesi e trattini.
     * </p>
     *
     * @param telefono numero di telefono da controllare
     * @return {@code true} se il numero è vuoto o valido; {@code false}
     *         altrimenti
     */
    private boolean telefonoValido(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return true;
        }

        String telefonoPulito = telefono.trim();

        // Sono ammessi solo numeri, +, spazi, parentesi e trattini.
        if (!telefonoPulito.matches("^\\+?[0-9\\s()\\-]+$")) {
            return false;
        }

        // Il numero deve contenere da 7 a 15 cifre.
        long numeroCifre = telefonoPulito.chars()
                .filter(Character::isDigit)
                .count();

        if (numeroCifre < 7 || numeroCifre > 15) {
            return false;
        }

        // Controllo del prefisso internazionale, se presente.
        if (telefonoPulito.startsWith("+")) {
            String numeroSenzaSimboli = telefonoPulito.replaceAll("[\\s()\\-]", "");

            // Il prefisso deve contenere da 1 a 3 cifre.
            if (!numeroSenzaSimboli.matches("^\\+[1-9][0-9]{0,2}[0-9]{6,14}$")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifica la validità formale di un indirizzo web.
     * <p>
     * Il campo può essere vuoto. Se valorizzato, deve contenere
     * un dominio valido, eventualmente preceduto da http:// o https://.
     * </p>
     *
     * @param sitoWeb indirizzo web da controllare
     * @return true se vuoto o valido, false altrimenti
     */
    private boolean sitoWebValido(String sitoWeb) {
        if (sitoWeb == null || sitoWeb.isBlank()) {
            return true;
        }

        String indirizzo = sitoWeb.trim();

        // Non sono ammessi spazi.
        if (indirizzo.contains(" ")) {
            return false;
        }

        // Se manca il protocollo, aggiunge https:// per la verifica.
        if (!indirizzo.matches("(?i)^https?://.*")) {
            indirizzo = "https://" + indirizzo;
        }

        try {
            URI uri = new URI(indirizzo);

            String schema = uri.getScheme();
            String host = uri.getHost();

            if (schema == null
                    || (!schema.equalsIgnoreCase("http")
                    && !schema.equalsIgnoreCase("https"))) {
                return false;
            }

            if (host == null || !host.contains(".")) {
                return false;
            }

            // Il dominio deve avere almeno due componenti.
            String[] parti = host.split("\\.");

            if (parti.length < 2) {
                return false;
            }

            // L'ultima componente deve avere almeno 2 caratteri.
            String estensione = parti[parti.length - 1];

            return estensione.matches("[a-zA-Z]{2,}");

        } catch (URISyntaxException e) {
            return false;
        }
    }
}