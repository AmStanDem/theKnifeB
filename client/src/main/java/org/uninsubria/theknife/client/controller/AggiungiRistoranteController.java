/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.CheckComboBox;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.LuogoInput;

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
    private CheckComboBox<String> comboCucine;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkBookingOnline;

    @FXML
    private Button btnSalva;

    @FXML
    private Button btnAnnulla;

    private boolean tipologieCaricate;

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

    private void caricaTipologieCucina() {
        Thread.startVirtualThread(() -> {
            try {
                List<String> tipologie = ServerConnection.getServer().ottieniTutteTipologieCucina();
                Platform.runLater(() -> {
                    comboCucine.getItems().setAll(FXCollections.observableArrayList(tipologie));
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
                || txtCitta.getText().trim().isEmpty()
                || txtNazione.getText().trim().isEmpty()
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


        try {

            // 6. Recupero degli altri dati.
            String nome = txtNome.getText().trim();
            String indirizzo = txtIndirizzo.getText().trim();
            String citta = txtCitta.getText().trim();
            String nazione = txtNazione.getText().trim();

            boolean delivery = chkDelivery.isSelected();
            boolean bookingOnline = chkBookingOnline.isSelected();

            // 3. Conversione dei campi numerici.
            double prezzoMedio = Double.parseDouble(
                    txtPrezzoMedio.getText().trim().replace(",", ".")
            );

            GeocodingService.Luogo luogoSelezionato =
                    txtIndirizzo.getLuogoSelezionato();
            if (luogoSelezionato == null
                    || !luogoSelezionato.etichetta().equals(indirizzo)
                    || !luogoSelezionato.localita().equalsIgnoreCase(citta)
                    || !luogoSelezionato.nazione().equalsIgnoreCase(nazione)) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Indirizzo da verificare",
                        "Seleziona un luogo valido dai suggerimenti. Città e nazione devono corrispondere al risultato scelto."
                );
                txtIndirizzo.requestFocus();
                return;
            }
            double latitudine = luogoSelezionato.latitudine();
            double longitudine = luogoSelezionato.longitudine();
            indirizzo = luogoSelezionato.indirizzo();

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
            List<String> cucineSelezionate = List.copyOf(
                    comboCucine.getCheckModel().getCheckedItems()
            );

            if (cucineSelezionate.isEmpty()) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Selezione mancante",
                        "Seleziona almeno una tipologia di cucina."
                );
                return;
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
                    null                // Media stelle iniziale
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

            tornaAllaHome(event);

        } catch (NumberFormatException e) {
            mostraMessaggio(
                    Alert.AlertType.ERROR,
                    "Errore di formato",
                    "Prezzo, latitudine e longitudine devono essere valori numerici validi."
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

    @FXML
    public void onAnnullaClick(ActionEvent event) {
        tornaAllaHome(event);
    }

    private void tornaAllaHome(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/MainLayout.fxml"
        );
    }

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
}
