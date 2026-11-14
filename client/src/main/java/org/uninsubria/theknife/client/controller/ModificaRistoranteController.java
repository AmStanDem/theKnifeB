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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class ModificaRistoranteController {

    @FXML private TextField txtNome;
    @FXML private ComboBox<RistoranteDTO> comboRistoranti;
    @FXML private LuogoInput txtIndirizzo;
    @FXML private TextField txtCitta;
    @FXML private TextField txtNazione;
    @FXML private TextField txtPrezzoMedio;
    @FXML private CheckComboBox<String> comboCucine;
    @FXML private CheckBox chkDelivery;
    @FXML private CheckBox chkBookingOnline;
    @FXML private Button btnSalva;

    private RistoranteDTO ristorante;
    private boolean tipologieCaricate;
    private boolean ristorantiCaricati;

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

    private void selezionaRistorante(RistoranteDTO selezionato) {
        ristorante = selezionato;
        if (selezionato == null) {
            return;
        }
        popolaCampi();
        aggiornaTipologieSelezionate();
        aggiornaStatoSalvataggio();
    }

    private void popolaCampi() {
        txtNome.setText(ristorante.nome());
        txtCitta.setText(ristorante.citta());
        txtNazione.setText(ristorante.nazione());
        txtPrezzoMedio.setText(String.valueOf(ristorante.prezzoMedio()));
        chkDelivery.setSelected(Boolean.TRUE.equals(ristorante.delivery()));
        chkBookingOnline.setSelected(Boolean.TRUE.equals(ristorante.bookingOnline()));

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

    private void caricaTipologieCucina() {
        Thread.startVirtualThread(() -> {
            try {
                List<String> tipologie = ServerConnection.getServer().ottieniTutteTipologieCucina();
                Platform.runLater(() -> {
                    comboCucine.getItems().setAll(FXCollections.observableArrayList(tipologie));
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

    private void aggiornaTipologieSelezionate() {
        comboCucine.getCheckModel().clearChecks();
        if (ristorante != null && ristorante.tipologieCucina() != null) {
            for (String tipologia : ristorante.tipologieCucina()) {
                comboCucine.getCheckModel().check(tipologia);
            }
        }
    }

    private void aggiornaStatoSalvataggio() {
        btnSalva.setDisable(!tipologieCaricate || !ristorantiCaricati || ristorante == null);
    }

    @FXML
    public void onSalvaClick(ActionEvent event) {
        if (!tipologieCaricate) {
            mostraMessaggio(Alert.AlertType.WARNING, "Catalogo non disponibile",
                    "Attendi il caricamento delle tipologie di cucina prima di salvare.");
            return;
        }
        if (txtNome.getText().trim().isEmpty()
                || txtIndirizzo.getText().trim().isEmpty()
                || txtCitta.getText().trim().isEmpty()
                || txtNazione.getText().trim().isEmpty()
                || txtPrezzoMedio.getText().trim().isEmpty()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Campi mancanti",
                    "Compila tutti i campi obbligatori (*).");
            return;
        }

        GeocodingService.Luogo luogo = txtIndirizzo.getLuogoSelezionato();
        if (luogo == null
                || !luogo.etichetta().equals(txtIndirizzo.getText().trim())
                || !luogo.localita().equalsIgnoreCase(txtCitta.getText().trim())
                || !luogo.nazione().equalsIgnoreCase(txtNazione.getText().trim())) {
            mostraMessaggio(Alert.AlertType.WARNING, "Indirizzo da verificare",
                    "Seleziona un indirizzo valido dai suggerimenti o dalla mappa.");
            txtIndirizzo.requestFocus();
            return;
        }

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

        List<String> cucine = List.copyOf(comboCucine.getCheckModel().getCheckedItems());
        if (cucine.isEmpty()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Selezione mancante",
                    "Seleziona almeno una tipologia di cucina.");
            return;
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
                ristorante.mediaStelle()
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

    @FXML
    public void onAnnullaClick(ActionEvent event) {
        tornaAiMieiRistoranti(event);
    }

    private void tornaAiMieiRistoranti(ActionEvent event) {
        SceneManager.switchScene(event,
                "/org/uninsubria/theknife/client/views/ListaRistorantiGestore.fxml");
    }

    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String contenuto) {
        Alert alert = new Alert(tipo);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}
