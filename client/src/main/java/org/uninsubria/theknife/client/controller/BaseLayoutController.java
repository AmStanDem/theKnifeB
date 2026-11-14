package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;

import java.util.Objects;

public class BaseLayoutController {

    @FXML
    private StackPane contentContainer;

    @FXML
    private Button btnAccediRegistrati;

    @FXML
    private Button btnBack;

    @FXML
    private ImageView imgUtente;

    private boolean paginaLogin;
    private boolean loggato;

    @FXML
    private void initialize() {
        loggato = SessioneUtente.getUtenteCorrente() != null;
        imgUtente.setVisible(loggato);
        imgUtente.setManaged(loggato);
        aggiornaPulsanteAccesso();
    }

    @FXML
    private void goBack(ActionEvent event) {
        SceneManager.goBack(event);
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/LoginView.fxml"
        );
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/MainLayout.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleLoggatoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/AreaPersonale.fxml"
        );
    }

    public void setContent(Node content) {
        contentContainer.getChildren().setAll(Objects.requireNonNull(content));
    }

    public void setCanGoBack(boolean canGoBack) {
        btnBack.setDisable(!canGoBack);
    }

    public void setLoginPage(boolean loginPage) {
        paginaLogin = loginPage;
        aggiornaPulsanteAccesso();
    }

    private void aggiornaPulsanteAccesso() {
        boolean mostraPulsante = !loggato && !paginaLogin;
        btnAccediRegistrati.setVisible(mostraPulsante);
        btnAccediRegistrati.setManaged(mostraPulsante);
    }
}
