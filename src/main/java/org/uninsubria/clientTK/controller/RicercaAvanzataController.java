package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller JavaFX dell'interfaccia dedicata alla ricerca avanzata
 * dei ristoranti.
 * <p>
 * Gestisce le interazioni dell'utente con la schermata di ricerca
 * avanzata e la navigazione verso la lista dei ristoranti.
 * </p>
 */
public class RicercaAvanzataController {

    @FXML
    /**
     * Gestisce la ricerca di un ristorante.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code listaRistoranti.fxml},
     * nella quale vengono visualizzati i risultati della ricerca.
     * </p>
     *
     * @param event evento generato dal click dell'utente
     */
    private void cercaRistorante(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/listaRistoranti.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void reimpostaFiltri() {
    }



}