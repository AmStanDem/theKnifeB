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
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;


import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

/**
 * Controller JavaFX della schermata di modifica di un ristorante.
 * <p>
 * Consente a un gestore autenticato di selezionare uno dei propri ristoranti,
 * modificarne i dati e inviare l'aggiornamento al server tramite RMI.
 * Gestisce il caricamento delle tipologie di cucina, il geocoding dell'indirizzo
 * e la validazione dei dati prima del salvataggio.
 * </p>
 */
public class ModificaRistoranteController {

    @FXML private TextField txtNome;
    @FXML private ComboBox<RistoranteDTO> comboRistoranti;
    @FXML private LuogoInput txtIndirizzo;
    @FXML private TextField txtCitta;
    @FXML private TextField txtNazione;
    @FXML private TextField txtPrezzoMedio;
    @FXML private CuisineSelector comboCucine;
    @FXML private CheckBox chkDelivery;
    @FXML private CheckBox chkBookingOnline;
    @FXML private Button btnSalva;

    @FXML private Button btnElimina;

    @FXML private TextField txtTelefono;
    @FXML private TextField txtSitoWeb;
    @FXML private TextArea txtDescrizione;

    private RistoranteDTO ristorante;
    private boolean tipologieCaricate;
    private boolean ristorantiCaricati;

    /**
     * Inizializza il modulo di modifica.
     * <p>
     * Verifica il ruolo del gestore, configura il selettore dei ristoranti,
     * i campi derivati dal luogo e avvia il caricamento dei ristoranti e delle
     * tipologie di cucina disponibili.
     * </p>
     */
    @FXML
    public void initialize() {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isGestore()) {
            btnSalva.setDisable(true);
            mostraMessaggio(Alert.AlertType.ERROR, "Accesso non consentito",
                    "Devi accedere con un account ristoratore per modificare un ristorante.");
            return;
        }

        comboCucine.setDisable(true);
        btnSalva.setDisable(true);
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
        txtIndirizzo.addTextFieldStyleClass("custom-text-field");
        txtCitta.setEditable(false);
        txtNazione.setEditable(false);
        txtIndirizzo.setOnLuogoSelezionato(luogo -> {
            txtCitta.setText(luogo.localita());
            txtNazione.setText(luogo.nazione());
        });
        caricaRistoranti(utente);
        caricaTipologieCucina();
    }

    /**
     * Carica dal server i ristoranti associati al gestore autenticato.
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
     * Seleziona un ristorante e popola i campi del modulo.
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
     * Copia i dati del ristorante selezionato nei componenti della vista.
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
     * Recupera dal server tutte le tipologie di cucina disponibili.
     * <p>
     * Una volta completato il caricamento, aggiorna il selettore sul thread
     * JavaFX e ripristina le cucine associate al ristorante selezionato.
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
                    aggiornaTipologieSelezionate();
                    aggiornaStatoSalvataggio();
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
     * Imposta nel selettore delle cucine le tipologie del ristorante selezionato.
     */
    private void aggiornaTipologieSelezionate() {
        comboCucine.setSelectedCuisines(
                ristorante == null ? List.of() : ristorante.tipologieCucina());
    }

    /**
     * Aggiorna lo stato del pulsante di salvataggio.
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
        btnElimina.setDisable(disabilitato);}

    /**
     * Gestisce il click sul pulsante di salvataggio.
     * <p>
     * Verifica il catalogo delle cucine e i campi obbligatori, quindi risolve
     * il luogo inserito prima di avviare la validazione finale e la modifica.
     * </p>
     *
     * @param event evento JavaFX usato per cambiare scena dopo il salvataggio
     */
    @FXML
    public void onSalvaClick(ActionEvent event) {
        if (!tipologieCaricate) {
            mostraMessaggio(Alert.AlertType.WARNING, "Catalogo non disponibile",
                    "Attendi il caricamento delle tipologie di cucina prima di salvare.");
            return;
        }
        if (txtNome.getText().trim().isEmpty()
                || txtIndirizzo.getText().trim().isEmpty()
                || txtPrezzoMedio.getText().trim().isEmpty()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Campi mancanti",
                    "Compila tutti i campi obbligatori (*).");
            return;
        }

        txtIndirizzo.risolviLuogo(
                luogo -> salvaRistorante(event, luogo),
                messaggio -> mostraMessaggio(Alert.AlertType.WARNING, "Località non valida", messaggio)
        );
    }

    /**
     * Valida i dati finali e invia al server il ristorante aggiornato.
     * <p>
     * Controlla prezzo medio, selezione delle cucine e ruolo dell'utente,
     * quindi crea un {@link RistoranteDTO} mantenendo l'identificatore e la
     * valutazione esistenti del ristorante.
     * </p>
     *
     * @param event evento JavaFX usato per tornare alla lista
     * @param luogo luogo risolto dal servizio di geocoding
     */
    private void salvaRistorante(ActionEvent event, GeocodingService.Luogo luogo) {
        double prezzoMedio;
        try {
            prezzoMedio = Double.parseDouble(txtPrezzoMedio.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Prezzo non valido",
                    "Inserisci un prezzo medio numerico.");
            return;
        }
        if (!Double.isFinite(prezzoMedio) || prezzoMedio < 0) {
            mostraMessaggio(Alert.AlertType.WARNING, "Prezzo non valido",
                    "Il prezzo medio deve essere un numero finito e non negativo.");
            return;
        }

        List<String> cucine = comboCucine.getSelectedCuisines();
        if (cucine.isEmpty()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Selezione mancante",
                    "Seleziona almeno una tipologia di cucina.");
            return;
        }

        String telefono = txtTelefono.getText().trim();
        String sitoWeb = txtSitoWeb.getText().trim();

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


        if (!sitoWeb.isEmpty()
                && !sitoWeb.matches("(?i)^https?://.*")) {
            sitoWeb = "https://" + sitoWeb;
        }

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isGestore()) {
            mostraMessaggio(Alert.AlertType.ERROR, "Accesso non consentito",
                    "Devi accedere con un account ristoratore per modificare un ristorante.");
            return;
        }

        RistoranteDTO aggiornato = new RistoranteDTO(
                ristorante.idRistorante(),
                txtNome.getText().trim(),
                luogo.indirizzo(),
                luogo.localita(),
                luogo.nazione(),
                luogo.latitudine(),
                luogo.longitudine(),
                cucine,
                prezzoMedio,
                chkDelivery.isSelected(),
                chkBookingOnline.isSelected(),
                ristorante.mediaStelle(),
                ristorante.prezzoStimato(),
                telefono,
                sitoWeb,
                txtDescrizione.getText().trim()
        );

        try {
            ServerConnection.getServer().modificaRistorante(aggiornato, utente.idUtente());
            mostraMessaggio(Alert.AlertType.INFORMATION, "Modifica completata",
                    "Il ristorante \"" + aggiornato.nome() + "\" è stato aggiornato.");
            tornaAiMieiRistoranti(event);
        } catch (RemoteException | NotBoundException | OperazioneNonConsentitaException
                 | DatiMancantiException | SistemaIndisponibileException e) {
            System.err.println("[RESTAURANT_FORM_ERROR] Impossibile modificare il ristorante: "
                    + e.getMessage());
            mostraMessaggio(Alert.AlertType.ERROR, "Errore durante la modifica",
                    "Il ristorante non è stato aggiornato. Verifica la connessione e i dati, poi riprova.");
        }
    }

    /**
     * Annulla la modifica e torna alla lista dei ristoranti del gestore.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onAnnullaClick(ActionEvent event) {
        tornaAiMieiRistoranti(event);
    }

    /**
     * Mostra la lista dei ristoranti gestiti dall'utente.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    private void tornaAiMieiRistoranti(ActionEvent event) {
        SceneManager.switchScene(event,
                "/org/uninsubria/theknife/client/views/ListaRistorantiGestore.fxml");
    }

    /**
     * Visualizza una finestra di dialogo con il messaggio indicato.
     *
     * @param tipo tipo dell'avviso
     * @param titolo titolo della finestra
     * @param contenuto testo da mostrare all'utente
     */
    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String contenuto) {
        Alert alert = new Alert(tipo);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    private boolean sitoWebValido(String sitoWeb) {
        if (sitoWeb == null || sitoWeb.isBlank()) {
            return true;
        }

        String indirizzo = sitoWeb.trim();

        if (indirizzo.contains(" ")) {
            return false;
        }

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

            String[] parti = host.split("\\.");

            if (parti.length < 2) {
                return false;
            }

            String estensione = parti[parti.length - 1];

            return estensione.matches("[a-zA-Z]{2,}");

        } catch (URISyntaxException e) {
            return false;
        }
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



}
