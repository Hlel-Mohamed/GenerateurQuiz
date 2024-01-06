package net.universitecentrale.generateurquiz.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import net.universitecentrale.generateurquiz.HelloApplication;

import java.io.IOException;

public class MainController {

    @FXML
    private Button listeQuestionsButton;
    @FXML
    private Button genererButton;

    @FXML
    protected void redirectView(Button button, String viewName, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/" + viewName));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage =(Stage) button.getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void redirectListeQuestionsView() throws IOException {
        redirectView(listeQuestionsButton, "questions-view.fxml", "Liste des questions");
    }

    @FXML
    protected void redirectGenererView() throws IOException {
        redirectView(genererButton, "generer-view.fxml", "Générer un quiz");
    }
}