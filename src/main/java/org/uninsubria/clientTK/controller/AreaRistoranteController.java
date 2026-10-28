package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.RistoranteSelezionato;
import org.uninsubria.clientTK.util.RecensioneSelezionata;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.exceptions.AzioneDuplicataException;
import org.uninsubria.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.common.exceptions.RecensioneNonTrovataException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

import static org.uninsubria.clientTK.util.ServerConnection.getServer;

public class AreaRistoranteController {


    @FXML
    private Label lblNomeRistorante;

    @FXML
    private Label lblNazione;

    @FXML
    private Label lblCitta;

    @FXML
    private Label lblIndirizzo;


    @FXML
    private Label lblFasciaPrezzo;

    @FXML
    private Label lblDelivery;

    @FXML
    private Label lblPrenotazione;

    @FXML
    private Label lblTipoCucina;



    // =========================================================
    // RECENSIONI
    // =========================================================

    @FXML
    private VBox reviewContainer;

    @FXML
    private Label resultsCountLabel;

    private RistoranteDTO ristorante;


    @FXML
    private Label averageRatingLabel;

    @FXML
    private Rating averageRating;

    @FXML
    private Button recensisci;

    @FXML
    private Button modificaRecensione;

    @FXML
    private Button rimuoviRecensione;

    @FXML
    private Button aggiungiPreferito;

    private boolean ristorantePreferito;
    private RecensioneDTO recensioneUtente;


    @FXML
    private ProgressBar bar5;

    @FXML
    private ProgressBar bar4;

    @FXML
    private ProgressBar bar3;

    @FXML
    private ProgressBar bar2;

    @FXML
    private ProgressBar bar1;

    // =========================================================
    // INIZIALIZZAZIONE
    // =========================================================

    @FXML
    public void initialize() {

        System.out.println("AreaRistoranteController inizializzato");

        // Recupera il ristorante selezionato nella pagina precedente
        ristorante = RistoranteSelezionato.getRistorante();

        if (ristorante != null) {
            System.out.println("Ristorante selezionato: " + ristorante.nome());
            mostraDatiRistorante();
            aggiornaStatoPreferito();
        } else {
            System.out.println("Nessun ristorante selezionato");
        }
        boolean clienteLoggato = SessioneUtente.isCliente();
        aggiornaAzioniRecensione(clienteLoggato);
        caricaRecensioni();
    }

    private void aggiornaAzioniRecensione(boolean clienteLoggato) {
        recensisci.setVisible(clienteLoggato && recensioneUtente == null);
        recensisci.setManaged(clienteLoggato && recensioneUtente == null);
        modificaRecensione.setVisible(clienteLoggato && recensioneUtente != null);
        modificaRecensione.setManaged(clienteLoggato && recensioneUtente != null);
        rimuoviRecensione.setVisible(clienteLoggato && recensioneUtente != null);
        rimuoviRecensione.setManaged(clienteLoggato && recensioneUtente != null);

        if (!clienteLoggato || ristorante == null || ristorante.idRistorante() == null) {
            recensioneUtente = null;
            recensisci.setVisible(false);
            recensisci.setManaged(false);
            modificaRecensione.setVisible(false);
            modificaRecensione.setManaged(false);
            rimuoviRecensione.setVisible(false);
            rimuoviRecensione.setManaged(false);
            return;
        }

        try {
            recensioneUtente = ServerConnection.getServer()
                    .getRecensioniUtente(SessioneUtente.getUtenteCorrente().idUtente())
                    .stream()
                    .filter(recensione -> Objects.equals(
                            recensione.idRistorante(), ristorante.idRistorante()))
                    .findFirst()
                    .orElse(null);
            recensisci.setVisible(recensioneUtente == null);
            recensisci.setManaged(recensioneUtente == null);
            modificaRecensione.setVisible(recensioneUtente != null);
            modificaRecensione.setManaged(recensioneUtente != null);
            rimuoviRecensione.setVisible(recensioneUtente != null);
            rimuoviRecensione.setManaged(recensioneUtente != null);
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile verificare se hai già recensito questo ristorante.");
        }
    }

    private void mostraDatiRistorante() {
        if (ristorante == null) {
            return;
        }



        lblNomeRistorante.setText(ristorante.nome());
        lblIndirizzo.setText(ristorante.indirizzo());

        lblCitta.setText(ristorante.citta());
        System.out.println("citta: " + ristorante.citta());
        lblNazione.setText(ristorante.nazione());
        System.out.println("citta: " + ristorante.nazione());
        lblFasciaPrezzo.setText(
                ristorante.prezzoMedio() != null
                        ? String.format("%.2f €", ristorante.prezzoMedio())
                        : "N/D"
        );

        lblDelivery.setText(
                Boolean.TRUE.equals(ristorante.delivery()) ? "Sì" : "No"
        );

        lblPrenotazione.setText(
                Boolean.TRUE.equals(ristorante.bookingOnline()) ? "Sì" : "No"
        );

        lblTipoCucina.setText(
                ristorante.tipologieCucina() != null
                        ? String.valueOf(ristorante.tipologieCucina())
                        : "Non specificata"
        );
    }

    private void caricaRecensioni() {
        System.out.println("caricaRecensioni() chiamato");


        try {
            if (ristorante == null || ristorante.idRistorante() == null) {
                return;
            }
            List<RecensioneDTO> recensioni = SessioneUtente.isLoggato()
                    ? ServerConnection.getServer().getRecensioniRistorante(ristorante.idRistorante())
                    : ServerConnection.getServer().getRecensioniRistoranteAnonimo(ristorante.idRistorante());
            boolean gestoreProprietario = false;
            if (SessioneUtente.isGestore()) {
                UtenteDTO gestore = SessioneUtente.getUtenteCorrente();
                gestoreProprietario = gestore != null
                        && ServerConnection.getServer().getRistorantiGestore(gestore.idUtente())
                        .stream()
                        .anyMatch(proprio -> Objects.equals(
                                proprio.idRistorante(), ristorante.idRistorante()));
            }

            aggiornaMediaRecensioni(recensioni);
            aggiornaDistribuzioneRecensioni(recensioni);

            if (reviewContainer == null) {
                System.out.println("ERRORE: reviewContainer è NULL");
                return;
            }

            System.out.println("reviewContainer trovato!");
            reviewContainer.getChildren().clear();



            System.out.println("Numero recensioni: " + recensioni.size());
            resultsCountLabel.setText(recensioni.size() + " recensioni trovate");

            for (RecensioneDTO recensione : recensioni) {
                try {
                    System.out.println("Carico recensione di: " + recensione.nomeAutore());

                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/org/uninsubria/clientTK/views/RecensioneItem.fxml")
                    );

                    Node item = loader.load();
                    RecensioneItemController controller = loader.getController();

                    controller.setRecensione(recensione);
                    controller.setRispostaAbilitata(gestoreProprietario);

                    reviewContainer.getChildren().add(item);
                    System.out.println("Recensione aggiunta!");

                } catch (Exception e) {
                    System.out.println("ERRORE caricando la recensione:");
                    e.printStackTrace();
                }
            }


        } catch (RemoteException | SistemaIndisponibileException | NotBoundException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile caricare le recensioni o verificare la proprietà del ristorante.");
        }
    }


    private void aggiornaMediaRecensioni(List<RecensioneDTO> recensioni) {

        if (recensioni == null || recensioni.isEmpty()) {
            averageRatingLabel.setText("0,0");
            averageRating.setRating(0);
            return;
        }

        double somma = 0;

        for (RecensioneDTO recensione : recensioni) {
            somma += recensione.valutazione();
        }

        double media = somma / recensioni.size();

        averageRatingLabel.setText(
                String.format(java.util.Locale.ITALY, "%.1f", media)
        );

        averageRating.setRating(media);
    }

    private void aggiornaDistribuzioneRecensioni(List<RecensioneDTO> recensioni) {

        int[] conteggi = new int[6];

        for (RecensioneDTO recensione : recensioni) {
            int valutazione = recensione.valutazione();

            if (valutazione >= 1 && valutazione <= 5) {
                conteggi[valutazione]++;
            }
        }

        int totale = recensioni.size();

        if (totale == 0) {
            bar5.setProgress(0);
            bar4.setProgress(0);
            bar3.setProgress(0);
            bar2.setProgress(0);
            bar1.setProgress(0);
            return;
        }

        bar5.setProgress((double) conteggi[5] / totale);
        bar4.setProgress((double) conteggi[4] / totale);
        bar3.setProgress((double) conteggi[3] / totale);
        bar2.setProgress((double) conteggi[2] / totale);
        bar1.setProgress((double) conteggi[1] / totale);
    }

    // =========================================================
    // NAVIGAZIONE
    // =========================================================

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    public void onPreferitiClick(ActionEvent actionEvent) {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per aggiungere ristoranti ai preferiti.");
            return;
        }
        if (ristorante == null || ristorante.idRistorante() == null) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore",
                    "Impossibile identificare il ristorante selezionato.");
            return;
        }

        try {
            if (ristorantePreferito) {
                ServerConnection.getServer().rimuoviPreferito(utente.idUtente(), ristorante.idRistorante());
                ristorantePreferito = false;
                mostraMessaggio(Alert.AlertType.INFORMATION, "Preferiti",
                        "Ristorante rimosso dai preferiti.");
            } else {
                ServerConnection.getServer().aggiungiPreferito(utente.idUtente(), ristorante.idRistorante());
                ristorantePreferito = true;
                mostraMessaggio(Alert.AlertType.INFORMATION, "Preferiti",
                        "Ristorante aggiunto ai preferiti.");
            }
            aggiornaTestoPulsantePreferito();
        } catch (AzioneDuplicataException e) {
            ristorantePreferito = true;
            aggiornaTestoPulsantePreferito();
            mostraMessaggio(Alert.AlertType.INFORMATION, "Preferiti",
                    "Il ristorante è già presente nei tuoi preferiti.");
        } catch (org.uninsubria.common.exceptions.RistoranteNonTrovatoException e) {
            ristorantePreferito = false;
            aggiornaTestoPulsantePreferito();
            mostraMessaggio(Alert.AlertType.WARNING, "Preferiti",
                    "Il ristorante non risulta più presente nei tuoi preferiti.");
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile aggiornare i preferiti. Riprova più tardi.");
        }
    }

    private void aggiornaStatoPreferito() {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        boolean clienteLoggato = utente != null && SessioneUtente.isCliente();
        aggiungiPreferito.setVisible(clienteLoggato);
        aggiungiPreferito.setManaged(clienteLoggato);
        if (!clienteLoggato) {
            return;
        }

        try {
            ristorantePreferito = ServerConnection.getServer()
                    .getPreferitiUtente(utente.idUtente())
                    .stream()
                    .anyMatch(preferito -> preferito.idRistorante().equals(ristorante.idRistorante()));
            aggiornaTestoPulsantePreferito();
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            aggiungiPreferito.setDisable(true);
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile verificare lo stato dei preferiti.");
        }
    }

    private void aggiornaTestoPulsantePreferito() {
        aggiungiPreferito.setText(ristorantePreferito
                ? "Rimuovi dai preferiti"
                : "Aggiungi ai preferiti");
    }

    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    // =========================================================
    // SCRIVI RECENSIONE
    // =========================================================

    @FXML
    public void onRecensioniClick(ActionEvent actionEvent) {
        RecensioneSelezionata.pulisci();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/ScriviRecensione.fxml");
    }

    @FXML
    private void onModificaRecensioneClick(ActionEvent actionEvent) {
        if (recensioneUtente == null) {
            return;
        }
        RecensioneSelezionata.imposta(recensioneUtente);
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/ScriviRecensione.fxml");
    }

    @FXML
    private void onRimuoviRecensioneClick() {
        if (recensioneUtente == null) {
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Rimuovi recensione");
        conferma.setHeaderText("Vuoi rimuovere la tua recensione?");
        conferma.setContentText("Questa operazione non può essere annullata.");
        if (conferma.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per rimuovere una recensione.");
            return;
        }

        try {
            ServerConnection.getServer().eliminaRecensione(
                    recensioneUtente.idRecensione(), utente.idUtente());
            recensioneUtente = null;
            aggiornaAzioniRecensione(true);
            caricaRecensioni();
            mostraMessaggio(Alert.AlertType.INFORMATION, "Recensione rimossa",
                    "La tua recensione è stata rimossa.");
        } catch (RecensioneNonTrovataException | OperazioneNonConsentitaException e) {
            recensioneUtente = null;
            aggiornaAzioniRecensione(true);
            mostraMessaggio(Alert.AlertType.WARNING, "Recensione non disponibile",
                    "La recensione è già stata rimossa o non è più modificabile.");
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile rimuovere la recensione. Riprova più tardi.");
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    /*
    @FXML
    public void onLogOutClick(ActionEvent actionEvent) {
        SessioneUtente.logout();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

     */

    // =========================================================
    // LOGO
    // =========================================================

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}