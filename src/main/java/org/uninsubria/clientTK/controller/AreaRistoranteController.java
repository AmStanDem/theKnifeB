package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.clientTK.util.RistoranteSelezionato;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

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
        } else {
            System.out.println("Nessun ristorante selezionato");
        }

        caricaRecensioni();
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
            List<RecensioneDTO> recensioni = ServerConnection.getServer().getRecensioniRistoranteAnonimo(ristorante.idRistorante());

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

                    controller.setDati(
                            recensione.nomeAutore(),
                            false,
                            recensione.dataCreazione().format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")
                            ),
                            recensione.valutazione(),
                            recensione.testo(),
                            recensione.rispostaGestore()
                    );

                    reviewContainer.getChildren().add(item);
                    System.out.println("Recensione aggiunta!");

                } catch (Exception e) {
                    System.out.println("ERRORE caricando la recensione:");
                    e.printStackTrace();
                }
            }


        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (SistemaIndisponibileException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
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
    }

    // =========================================================
    // SCRIVI RECENSIONE
    // =========================================================

    @FXML
    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/ScriviRecensione.fxml");
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    public void onLogOutClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    // =========================================================
    // LOGO
    // =========================================================

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }
}