package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.CheckComboBox;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.dto.UtenteDTO;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.uninsubria.clientTK.util.GeocodingService;

public class AggiungiRistoranteController {

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtIndirizzo;

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

    @FXML
    public void initialize() {
        ObservableList<String> tipologieCucina =
                FXCollections.observableArrayList(
                        "Italiana",
                        "Pizzeria",
                        "Giapponese",
                        "Cinese",
                        "Messicana",
                        "Indiana",
                        "Vegetariana",
                        "Vegan"
                );

        comboCucine.getItems().setAll(tipologieCucina);
    }

    @FXML
    public void onSalvaClick(ActionEvent event) {

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

            // Recupera automaticamente le coordinate dall'indirizzo.
            GeocodingService.Coordinate coordinate;

            try {
                coordinate = GeocodingService.ottieniCoordinate(
                        indirizzo,
                        citta,
                        nazione
                );

            } catch (IOException | InterruptedException e) {

                Thread.currentThread().interrupt();

                mostraMessaggio(
                        Alert.AlertType.ERROR,
                        "Errore di geolocalizzazione",
                        "Non è stato possibile recuperare le coordinate. "
                                + "Controlla la connessione e riprova."
                );

                return;
            }

            if (coordinate == null) {
                mostraMessaggio(
                        Alert.AlertType.WARNING,
                        "Indirizzo non trovato",
                        "Non è stato possibile trovare l'indirizzo inserito. "
                                + "Verifica indirizzo, città e nazione."
                );

                return;
            }

            double latitudine = coordinate.latitudine();
            double longitudine = coordinate.longitudine();

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
                "/org/uninsubria/clientTK/views/MainLayout.fxml"
        );
    }

    private void mostraMessaggio(
            Alert.AlertType alertType,
            String titolo,
            String contenuto
    ) {
        Alert alert = new Alert(alertType);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}
