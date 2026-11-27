/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.uninsubria.theknife.client.util.BackHandler;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.LuogoInput;
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
import org.uninsubria.theknife.common.util.FasciaPrezzo;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

/**
 * Controller JavaFX del modulo usato dai gestori per creare, modificare ed eliminare ristoranti.
 * <p>
 * La modalità viene scelta in modo esplicito dal chiamante: {@link #apriCreazione()}
 * prepara un nuovo inserimento, mentre {@link #apriModifica(RistoranteDTO)} carica
 * i dati di un ristorante esistente e abilita anche l'eliminazione. Verifica i dati
 * inseriti e usa i servizi remoti per salvare le modifiche.
 * </p>
 */
public class FormRistoranteController implements BackHandler {

    @FXML
    private TextField txtNome;

    @FXML
    private VBox containerRistoranti;

    @FXML
    private ComboBox<RistoranteDTO> comboRistoranti;

    @FXML
    private LuogoInput txtIndirizzo;

    @FXML
    private TextField txtCitta;

    @FXML
    private TextField txtNazione;

    @FXML
    private ComboBox<String> comboFasciaPrezzo;

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
    private StatoForm statoIniziale;

    /**
     * Fotografia dei valori del modulo, usata per capire se ci sono modifiche
     * non ancora salvate prima di abbandonare la pagina.
     */
    private record StatoForm(
            String nome,
            String indirizzo,
            String citta,
            String nazione,
            String fasciaPrezzo,
            String telefono,
            String sitoWeb,
            String descrizione,
            boolean consegna,
            boolean prenotazioneOnline,
            List<String> cucine
    ) {
    }

    /**
     * Inizializza i componenti comuni della vista.
     * <p>
     * Configura i campi, i listener e avvia il caricamento delle tipologie di
     * cucina. La modalità viene decisa in modo esplicito dal chiamante tramite
     * {@link #apriCreazione()} o {@link #apriModifica(RistoranteDTO)}; in
     * assenza di indicazioni il modulo si apre in creazione.
     * </p>
     */
    @FXML
    public void initialize() {
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente == null || !SessioneUtente.eGestore()) {
            btnSalva.setDisable(true);
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Accesso non consentito",
                    "Devi accedere con un account ristoratore per gestire un ristorante."
            );
            return;
        }

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
        comboRistoranti.setOnAction(evento -> selezionaRistorante(comboRistoranti.getValue()));

        txtIndirizzo.aggiungiStileCampoTesto("custom-text-field");
        txtCitta.setEditable(false);
        txtNazione.setEditable(false);
        txtIndirizzo.setOnLuogoSelezionato(luogo -> {
            txtCitta.setText(luogo.localita());
            txtNazione.setText(luogo.nazione());
            impostaFascePrezzo(FasciaPrezzo.simboloValuta(luogo.codiceNazione()));
        });

        comboCucine.setDisable(true);
        btnSalva.setDisable(true);

        // Modalità predefinita: creazione, sovrascrivibile con apriModifica.
        apriCreazione();

        caricaTipologieCucina();
    }

    /**
     * Prepara il modulo per la creazione di un nuovo ristorante.
     * <p>
     * Nasconde la selezione del ristorante e i comandi di eliminazione, così
     * che il salvataggio registri una nuova attività.
     * </p>
     */
    public void apriCreazione() {
        modalitaModifica = false;
        ristorante = null;
        mostraSelettoreRistorante(false);
        aggiornaStatoIniziale();
    }

    /**
     * Prepara il modulo per la modifica di un ristorante esistente.
     * <p>
     * Mostra il selettore dei ristoranti del gestore e i comandi di
     * eliminazione, quindi carica i dati del ristorante indicato. Se il
     * ristorante non è valido o non ha un identificativo, il modulo si apre
     * in creazione.
     * </p>
     *
     * @param ristorante ristorante da modificare
     */
    public void apriModifica(RistoranteDTO ristorante) {
        if (ristorante == null || ristorante.idRistorante() == null) {
            apriCreazione();
            return;
        }

        modalitaModifica = true;
        mostraSelettoreRistorante(true);

        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente == null || !SessioneUtente.eGestore()) {
            return;
        }
        caricaRistoranti(utente, ristorante);
    }

    /**
     * Mostra o nasconde i controlli disponibili solo in modalità modifica.
     *
     * @param visibile {@code true} per mostrare selettore ed eliminazione
     */
    private void mostraSelettoreRistorante(boolean visibile) {
        containerRistoranti.setVisible(visibile);
        containerRistoranti.setManaged(visibile);
        comboRistoranti.setVisible(visibile);
        comboRistoranti.setManaged(visibile);
        btnElimina.setVisible(visibile);
        btnElimina.setManaged(visibile);
    }

    /**
     * Intercetta il pulsante "indietro" del layout comune.
     * <p>
     * Se il modulo contiene modifiche non salvate viene chiesta conferma,
     * evitando di perdere i dati inseriti per un click accidentale.
     * </p>
     *
     * @return {@code true} se l'utente sceglie di restare nel modulo
     */
    @Override
    public boolean richiestaIndietro() {
        if (!moduloModificato()) {
            return false;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Modifiche non salvate");
        conferma.setHeaderText(null);
        conferma.setContentText(
                "Le modifiche non salvate andranno perse. Vuoi uscire comunque?");
        ButtonType esci = new ButtonType("Esci senza salvare", ButtonBar.ButtonData.OK_DONE);
        ButtonType continua = new ButtonType("Continua a modificare", ButtonBar.ButtonData.CANCEL_CLOSE);
        conferma.getButtonTypes().setAll(esci, continua);
        SceneManager.impostaProprietario(conferma);

        return conferma.showAndWait().filter(esci::equals).isEmpty();
    }

    /**
     * Aggiorna la fotografia dei valori correnti del modulo.
     */
    private void aggiornaStatoIniziale() {
        statoIniziale = statoCorrente();
    }

    /**
     * Verifica se i valori attuali del modulo differiscono da quelli iniziali.
     *
     * @return {@code true} se ci sono modifiche non salvate
     */
    private boolean moduloModificato() {
        return statoIniziale != null && !statoIniziale.equals(statoCorrente());
    }

    /**
     * Costruisce la fotografia dei valori attualmente presenti nel modulo.
     *
     * @return stato corrente dei campi
     */
    private StatoForm statoCorrente() {
        List<String> cucine = comboCucine.ottieniCucineSelezionate();
        return new StatoForm(
                txtNome.getText(),
                txtIndirizzo.getText(),
                txtCitta.getText(),
                txtNazione.getText(),
                comboFasciaPrezzo.getValue(),
                txtTelefono.getText(),
                txtSitoWeb.getText(),
                txtDescrizione.getText(),
                chkDelivery.isSelected(),
                chkBookingOnline.isSelected(),
                cucine == null ? List.of() : List.copyOf(cucine)
        );
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
                List<String> tipologie = ServerConnection.ottieniServer().ottieniTutteTipologieCucina();
                Platform.runLater(() -> {
                    comboCucine.impostaCucine(tipologie);
                    comboCucine.setDisable(false);
                    tipologieCaricate = true;
                    if (modalitaModifica) {
                        aggiornaTipologieSelezionate();
                        aggiornaStatoSalvataggio();
                        aggiornaStatoIniziale();
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
     * Popola il selettore sul thread JavaFX e seleziona il ristorante indicato
     * se presente tra i risultati, altrimenti il primo disponibile.
     * </p>
     *
     * @param utente gestore proprietario dei ristoranti
     * @param ristoranteDaSelezionare ristorante da preselezionare, oppure {@code null}
     */
    private void caricaRistoranti(UtenteDTO utente, RistoranteDTO ristoranteDaSelezionare) {
        Thread.startVirtualThread(() -> {
            try {
                List<RistoranteDTO> ristoranti =
                        ServerConnection.ottieniServer().ottieniRistorantiGestore(utente.idUtente());
                Platform.runLater(() -> {
                    comboRistoranti.getItems().setAll(ristoranti);
                    ristorantiCaricati = true;
                    if (ristoranti.isEmpty()) {
                        mostraMessaggio(Alert.AlertType.INFORMATION, "Nessun ristorante",
                                "Non hai ancora ristoranti da modificare.");
                    } else {
                        RistoranteDTO ristoranteInLista = ristoranteDaSelezionare == null ? null
                                : ristoranti.stream()
                                .filter(elemento -> elemento.idRistorante()
                                        .equals(ristoranteDaSelezionare.idRistorante()))
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
        aggiornaStatoIniziale();
    }

    /**
     * Copia i dati del ristorante selezionato nei componenti della vista (solo modifica).
     */
    private void popolaCampi() {
        txtNome.setText(ristorante.nome());
        txtCitta.setText(ristorante.citta());
        txtNazione.setText(ristorante.nazione());
        String simbolo = ristorante.fasciaPrezzo() == null ? "¤"
                : ristorante.fasciaPrezzo().substring(0, 1);
        impostaFascePrezzo(simbolo);
        chkDelivery.setSelected(Boolean.TRUE.equals(ristorante.consegna()));
        chkBookingOnline.setSelected(Boolean.TRUE.equals(ristorante.prenotazioneOnline()));

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
                    null,
                    ristorante.latitudine(),
                    ristorante.longitudine()
            ));
        } else {
            txtIndirizzo.setText(ristorante.indirizzo());
        }
        comboFasciaPrezzo.setValue(ristorante.fasciaPrezzo());
    }

    private void impostaFascePrezzo(String simbolo) {
        comboFasciaPrezzo.getItems().setAll(
                FasciaPrezzo.simboli(simbolo, 1),
                FasciaPrezzo.simboli(simbolo, 2),
                FasciaPrezzo.simboli(simbolo, 3),
                FasciaPrezzo.simboli(simbolo, 4)
        );
        if (comboFasciaPrezzo.getValue() == null) {
            comboFasciaPrezzo.setValue(comboFasciaPrezzo.getItems().getFirst());
        }
    }

    /**
     * Imposta nel selettore delle cucine le tipologie del ristorante selezionato (solo modifica).
     */
    private void aggiornaTipologieSelezionate() {
        comboCucine.impostaCucineSelezionate(
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
     * @param evento evento generato dal click sull'elemento JavaFX
     */
    @FXML
    public void onSalvaClick(ActionEvent evento) {
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
                || comboFasciaPrezzo.getValue() == null) {

            mostraMessaggio(
                    Alert.AlertType.WARNING,
                    "Campi mancanti",
                    "Compila tutti i campi obbligatori (*)."
            );

            return;
        }

        // 2. Verifica che sia presente un gestore autenticato.
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();

        if (utente == null || !SessioneUtente.eGestore()) {
            mostraMessaggio(
                    Alert.AlertType.WARNING,
                    "Accesso non consentito",
                    "Devi accedere con un account gestore per aggiungere un ristorante."
            );
            return;
        }

        txtIndirizzo.risolviLuogo(
                luogo -> salvaRistorante(evento, utente, luogo),
                messaggio -> mostraMessaggio(Alert.AlertType.WARNING, "Località non valida", messaggio)
        );
    }

    /**
     * Valida i dati del modulo e registra o modifica il ristorante tramite RMI.
     * <p>
     * In modalità creazione chiama {@code aggiungiRistorante()},
     * in modalità modifica chiama {@code modificaRistorante()}.
     * </p>
     *
     * @param evento          evento JavaFX usato per cambiare scena
     * @param utente         utente gestore autenticato
     * @param luogoSelezionato luogo risolto dal servizio di geocoding
     */
    private void salvaRistorante(
            ActionEvent evento,
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
            boolean consegna = chkDelivery.isSelected();
            boolean prenotazioneOnline = chkBookingOnline.isSelected();
            String fasciaPrezzo = comboFasciaPrezzo.getValue();
            double latitudine = luogoSelezionato.latitudine();
            double longitudine = luogoSelezionato.longitudine();

            // 4. Controllo dei valori inseriti.
            if (!Double.isFinite(latitudine)
                    || !Double.isFinite(longitudine)) {

                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Dati non validi",
                        "Le coordinate geografiche non sono valide."
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
            List<String> cucineSelezionate = comboCucine.ottieniCucineSelezionate();

            if (cucineSelezionate.isEmpty()) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Selezione mancante",
                        "Seleziona almeno una tipologia di cucina."
                );
                return;
            }

            telefono = telefono.replaceAll("\\s+", "");
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
                        fasciaPrezzo,
                        consegna,
                        prenotazioneOnline,
                        ristorante.mediaStelle(),
                        telefono,
                        sitoWeb,
                        descrizione
                );

                ServerConnection.ottieniServer().modificaRistorante(aggiornato, utente.idUtente());

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
                        fasciaPrezzo,
                        consegna,
                        prenotazioneOnline,
                        null,               // Media stelle iniziale
                        telefono,
                        sitoWeb,
                        descrizione
                );

                ServerConnection.ottieniServer().aggiungiRistorante(
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

            tornaAiMieiRistoranti(evento);

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
     * @param evento evento JavaFX usato per cambiare scena dopo l'eliminazione
     */
    @FXML
    public void onEliminaClick(ActionEvent evento) {
        if (ristorante == null) {
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        SceneManager.impostaProprietario(conferma);
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
                tornaAiMieiRistoranti(evento);
            }
        }
    }

    /**
     * Elimina il ristorante selezionato tramite RMI.
     *
     * @return {@code true} se l'eliminazione è riuscita, {@code false} altrimenti
     */
    private boolean eliminaRistorante() {
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();

        if (utente == null || !SessioneUtente.eGestore()) {
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Accesso non consentito",
                    "Devi accedere con un account ristoratore per eliminare un ristorante."
            );
            return false;
        }

        try {
            ServerConnection.ottieniServer().eliminaRistorante(
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
     * @param evento evento generato dal click sull'elemento JavaFX
     */
    @FXML
    public void onAnnullaClick(ActionEvent evento) {
        tornaAiMieiRistoranti(evento);
    }

    /**
     * Mostra la lista dei ristoranti del gestore nella scena corrente.
     *
     * @param evento evento JavaFX contenente l'origine del cambio scena
     */
    private void tornaAiMieiRistoranti(ActionEvent evento) {
        SceneManager.cambiaScena(
                evento,
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
        Alert avviso = new Alert(alertType);
        SceneManager.impostaProprietario(avviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(contenuto);
        avviso.showAndWait();
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