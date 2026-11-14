/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.uninsubria.theknife.client.util.CuisineTags;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.client.util.RecensioneSelezionata;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.client.util.stars.BaseRatingView;
import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.AzioneDuplicataException;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.RecensioneNonTrovataException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.uninsubria.theknife.client.util.ServerConnection.getServer;

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
    private FlowPane cuisineTags;



    // =========================================================
    // RECENSIONI
    // =========================================================

    @FXML
    private VBox reviewContainer;

    @FXML
    private Label resultsCountLabel;

    @FXML
    private Label reviewFilterLabel;

    @FXML
    private Button clearReviewFilterButton;

    private RistoranteDTO ristorante;
    private List<RecensioneDTO> recensioniCaricate = List.of();
    private Double valutazioneSelezionata;
    private boolean gestoreProprietario;


    @FXML
    private Label averageRatingLabel;

    @FXML
    private HBox averageRatingContainer;

    private BaseRatingView averageRating;

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
    private HBox ratingDistributionContainer;

    // =========================================================
    // INIZIALIZZAZIONE
    // =========================================================

    @FXML
    public void initialize() {

        System.out.println("AreaRistoranteController inizializzato");
        averageRating = new BaseRatingView(5, 0);
        averageRating.setSpacing(2);
        averageRating.setScaleX(0.75);
        averageRating.setScaleY(0.75);
        averageRatingContainer.getChildren().add(averageRating);
        averageRatingLabel.setAlignment(javafx.geometry.Pos.CENTER);
        averageRatingLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        HBox.setHgrow(ratingDistributionContainer, Priority.ALWAYS);
        ratingDistributionContainer.setMinWidth(0);
        ratingDistributionContainer.setMaxWidth(Double.MAX_VALUE);
        aggiornaDistribuzioneRecensioni(List.of());

        // Recupera il ristorante selezionato nella pagina precedente
        ristorante = RistoranteSelezionato.getRistorante();

        if (ristorante != null) {
            System.out.println("Ristorante selezionato: " + ristorante.nome());
            mostraDatiRistorante();
            aggiornaStatoPreferito();
            RemoteRefreshManager.subscribe(
                    this, SubscriptionTopic.RESTAURANT, ristorante.idRistorante(), event -> {
                        if (event.type() == DataChangedEvent.ChangeType.FAVORITES_CHANGED) {
                            return;
                        }
                        if (event.type() == DataChangedEvent.ChangeType.RESTAURANT_CHANGED) {
                            aggiornaDettaglioRistorante();
                        }
                        aggiornaAzioniRecensione(SessioneUtente.isCliente());
                        caricaRecensioni();
                    }, () -> {
                        aggiornaDettaglioRistorante();
                        aggiornaAzioniRecensione(SessioneUtente.isCliente());
                        caricaRecensioni();
                    });
            UtenteDTO utente = SessioneUtente.getUtenteCorrente();
            if (utente != null) {
                RemoteRefreshManager.subscribe(
                        this, SubscriptionTopic.USER, utente.idUtente(), event -> {
                            if (event.type() == DataChangedEvent.ChangeType.FAVORITES_CHANGED) {
                                aggiornaStatoPreferito();
                            }
                        }, this::aggiornaStatoPreferito);
            }
        } else {
            System.out.println("Nessun ristorante selezionato");
        }
        boolean clienteLoggato = SessioneUtente.isCliente();
        aggiornaAzioniRecensione(clienteLoggato);
        caricaRecensioni();
    }

    private void aggiornaDettaglioRistorante() {
        try {
            ristorante = ServerConnection.getServer().getDettaglioRistorante(ristorante.idRistorante());
            mostraDatiRistorante();
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException
                 | org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di aggiornamento",
                    "Impossibile aggiornare i dati del ristorante.");
        }
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
                        ? (ristorante.prezzoStimato() ? "≈ " : "")
                                + String.format("%.2f €", ristorante.prezzoMedio())
                        : "N/D"
        );

        lblDelivery.setText(
                Boolean.TRUE.equals(ristorante.delivery()) ? "Sì" : "No"
        );

        lblPrenotazione.setText(
                Boolean.TRUE.equals(ristorante.bookingOnline()) ? "Sì" : "No"
        );

        CuisineTags.populate(cuisineTags, ristorante.tipologieCucina());
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
            gestoreProprietario = false;
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
            recensioniCaricate = recensioni;
            mostraRecensioniFiltrate(gestoreProprietario);

        } catch (RemoteException | SistemaIndisponibileException | NotBoundException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile caricare le recensioni o verificare la proprietà del ristorante.");
        }
    }

    private void mostraRecensioniFiltrate(boolean gestoreProprietario) {
        if (reviewContainer == null) {
            System.out.println("ERRORE: reviewContainer è NULL");
            return;
        }

        List<RecensioneDTO> recensioniVisibili = recensioniCaricate.stream()
                .filter(recensione -> valutazioneSelezionata == null
                        || recensione.valutazione() != null
                        && Math.abs(recensione.valutazione() - valutazioneSelezionata) < 0.000001)
                .toList();

        reviewContainer.getChildren().clear();
        resultsCountLabel.setText(recensioniVisibili.size() + " recensioni trovate");
        boolean filtroAttivo = valutazioneSelezionata != null;
        reviewFilterLabel.setText(filtroAttivo
                ? "Filtro valutazione: " + formattaValutazione(valutazioneSelezionata)
                : "");
        reviewFilterLabel.setVisible(filtroAttivo);
        reviewFilterLabel.setManaged(filtroAttivo);
        clearReviewFilterButton.setVisible(filtroAttivo);
        clearReviewFilterButton.setManaged(filtroAttivo);

        for (RecensioneDTO recensione : recensioniVisibili) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/uninsubria/theknife/client/views/RecensioneItem.fxml")
                );
                Node item = loader.load();
                RecensioneItemController controller = loader.getController();
                controller.setRecensione(recensione);
                controller.setRispostaAbilitata(gestoreProprietario);
                reviewContainer.getChildren().add(item);
            } catch (Exception e) {
                System.out.println("ERRORE caricando la recensione:");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void azzeraFiltriRecensioni() {
        valutazioneSelezionata = null;
        mostraRecensioniFiltrate(gestoreProprietario);
    }


    private void aggiornaMediaRecensioni(List<RecensioneDTO> recensioni) {

        if (recensioni == null || recensioni.isEmpty()) {
            averageRatingLabel.setText("0.0 / 10");
            averageRating.setRating(0);
            return;
        }

        double somma = 0;

        for (RecensioneDTO recensione : recensioni) {
            somma += recensione.valutazione();
        }

        double media = somma / recensioni.size();

        averageRatingLabel.setText(
                String.format(Locale.ROOT, "%.1f / 10", media * 2)
        );

        averageRating.setRating(media);
    }

    private void aggiornaDistribuzioneRecensioni(List<RecensioneDTO> recensioni) {

        int[] conteggi = new int[11];
        int totale = 0;
        if (recensioni != null) {
            for (RecensioneDTO recensione : recensioni) {
                if (recensione == null || recensione.valutazione() == null) {
                    continue;
                }
                double voto = recensione.valutazione();
                int indice = (int) Math.round(voto * 2);
                if (voto >= 1 && voto <= 5 && Math.abs(voto * 2 - indice) < 0.000001) {
                    conteggi[indice]++;
                    totale++;
                }
            }
        }

        CategoryAxis asseValutazioni = new CategoryAxis();
        asseValutazioni.setLabel("Valutazione");

        NumberAxis assePercentuali = new NumberAxis();
        assePercentuali.setAutoRanging(false);
        assePercentuali.setLowerBound(0);
        assePercentuali.setUpperBound(100);
        assePercentuali.setTickUnit(25);
        assePercentuali.setMinorTickVisible(false);
        assePercentuali.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number valore) {
                return String.format(Locale.ROOT, "%.0f%%", valore.doubleValue());
            }

            @Override
            public Number fromString(String testo) {
                return Double.parseDouble(testo.replace("%", ""));
            }
        });

        BarChart<String, Number> istogramma = new BarChart<>(asseValutazioni, assePercentuali);
        istogramma.getStyleClass().add("rating-histogram");
        istogramma.setLegendVisible(false);
        istogramma.setAnimated(false);
        istogramma.setCategoryGap(8);
        istogramma.setBarGap(0);
        istogramma.setMinHeight(190);
        istogramma.setPrefHeight(210);
        istogramma.setMaxWidth(Double.MAX_VALUE);

        BarChart.Series<String, Number> serie = new BarChart.Series<>();
        for (int indice = 0; indice < conteggi.length; indice++) {
            double valutazione = indice / 2.0;
            String voto = formattaValutazione(valutazione);
            double percentuale = totale == 0 ? 0 : conteggi[indice] * 100.0 / totale;
            BarChart.Data<String, Number> barraData = new BarChart.Data<>(voto, percentuale);
            StackPane barra = new StackPane();
            barra.getStyleClass().add("rating-histogram-node");
            Region riempimento = new Region();
            riempimento.getStyleClass().add("rating-histogram-bar");
            Label percentualeLabel = new Label(String.format(Locale.ROOT, "%.1f%%", percentuale));
            percentualeLabel.getStyleClass().add("rating-histogram-percentage");
            percentualeLabel.setVisible(false);
            percentualeLabel.setMouseTransparent(true);
            barra.getChildren().setAll(riempimento, percentualeLabel);
            barra.setOnMouseEntered(event -> percentualeLabel.setVisible(true));
            barra.setOnMouseExited(event -> percentualeLabel.setVisible(false));
            barra.setOnMouseClicked(event -> {
                valutazioneSelezionata = valutazione;
                mostraRecensioniFiltrate(gestoreProprietario);
                event.consume();
            });
            barraData.setNode(barra);
            serie.getData().add(barraData);
        }
        istogramma.getData().add(serie);
        ratingDistributionContainer.getChildren().setAll(istogramma);
    }

    private String formattaValutazione(double valutazione) {
        return String.format(Locale.ROOT, "%.1f", valutazione).replace(".0", "");
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
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/RicercaAvanzataView.fxml");
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
        } catch (org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException e) {
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
        SceneManager.initOwner(alert);
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
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/ScriviRecensione.fxml");
    }

    @FXML
    private void onModificaRecensioneClick(ActionEvent actionEvent) {
        if (recensioneUtente == null) {
            return;
        }
        RecensioneSelezionata.imposta(recensioneUtente);
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/ScriviRecensione.fxml");
    }

    @FXML
    private void onRimuoviRecensioneClick() {
        if (recensioneUtente == null) {
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        SceneManager.initOwner(conferma);
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
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

     */

    // =========================================================
    // LOGO
    // =========================================================

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }
}