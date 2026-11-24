/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.components.CuisineSelector;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import org.uninsubria.theknife.client.util.LuogoInput;

/**
 * Controller JavaFX della schermata di inserimento di un nuovo ristorante.
 * <p>
 * Gestisce il caricamento delle tipologie di cucina dal server, la
 * compilazione e la validazione dei dati del modulo, il geocoding
 * dell'indirizzo e la registrazione del ristorante tramite RMI.
 * </p>
 * <p>
 * La registrazione è consentita esclusivamente a un utente autenticato
 * con ruolo gestore.
 * </p>
 */
public class AggiungiRistoranteController {

    @FXML
    private TextField txtNome;

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

    private boolean tipologieCaricate;

    /**
     * Inizializza i componenti della vista.
     * <p>
     * Disabilita il selettore delle cucine e il pulsante di salvataggio,
     * configura i campi derivati dal luogo selezionato e avvia il
     * caricamento delle tipologie di cucina disponibili.
     * </p>
     */
    @FXML
    public void initialize() {
        comboCucine.setDisable(true);
        btnSalva.setDisable(true);
        txtIndirizzo.addTextFieldStyleClass("custom-text-field");
        txtCitta.setEditable(false);
        txtNazione.setEditable(false);
        txtIndirizzo.setOnLuogoSelezionato(luogo -> {
            txtCitta.setText(luogo.localita());
            txtNazione.setText(luogo.nazione());
        });
        caricaTipologieCucina();
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
                    btnSalva.setDisable(false);
                    tipologieCaricate = true;
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
     * Valida i dati del modulo e registra il nuovo ristorante tramite RMI.
     * <p>
     * Converte e controlla il prezzo medio e le coordinate geografiche,
     * verifica la selezione delle cucine e la validità del numero di
     * telefono, quindi crea un {@link RistoranteDTO} e lo invia al server.
     * Al termine mostra l'esito dell'operazione e torna alla lista dei
     * ristoranti del gestore.
     * </p>
     *
     * @param event evento JavaFX usato per cambiare scena
     * @param utente utente gestore autenticato che effettua la registrazione
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



            // 7. Creazione del DTO da inviare al server.
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

            // 8. Inserimento effettivo tramite RMI.
            ServerConnection.getServer().registraNuovoRistorante(
                    ristorante,
                    utente.idUtente()
            );

            // 9. Conferma solo dopo il ritorno della chiamata al server.
            mostraMessaggio(
                    Alert.AlertType.INFORMATION,
                    "Successo",
                    "Il ristorante \"" + nome
                            + "\" è stato registrato con successo."
            );

            tornaAiMieiRistoranti(event);

        } catch (NumberFormatException e) {
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Errore di formato",
                    "Il prezzo medio deve essere un valore numerico valido."
            );

        } catch (RemoteException e) {
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
                    "Non è stato possibile registrare il ristorante. "
                            + "Controlla i dati e riprova."
            );

            e.printStackTrace();
        }
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
                "/org/uninsubria/theknife/client/views/ListaRistorantiGestore.fxml"
        );
    }

    /**
     * Visualizza una finestra di dialogo con il messaggio indicato.
     *
     * @param alertType tipo di avviso da mostrare
     * @param titolo titolo della finestra di dialogo
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
     *
     * Il campo può essere vuoto. Se valorizzato, deve contenere
     * un dominio valido, eventualmente preceduto da http:// o https://.
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
