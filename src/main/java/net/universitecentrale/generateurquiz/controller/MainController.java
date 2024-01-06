package net.universitecentrale.generateurquiz.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import net.universitecentrale.generateurquiz.GenerateurQuizApplication;

import java.io.IOException;

public class MainController {

    @FXML
    private Button listeQuestionsButton; // Bouton pour accéder à la liste des questions
    @FXML
    private Button genererButton; // Bouton pour générer un quiz

    /**
     * Méthode pour rediriger vers une autre vue.
     * Elle charge la vue spécifiée, crée une nouvelle scène avec cette vue, et définit cette scène comme scène de la fenêtre actuelle.
     * Elle définit également le titre de la fenêtre et affiche la fenêtre.
     *
     * @param button   Le bouton qui a déclenché l'action.
     * @param viewName Le nom du fichier FXML de la vue à charger.
     * @param title    Le titre à définir pour la fenêtre.
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    protected void redirectView(Button button, String viewName, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/" + viewName)); // Crée un FXMLLoader et charge le fichier FXML de la vue
        Scene scene = new Scene(fxmlLoader.load()); // Crée une nouvelle scène avec la vue chargée
        Stage stage = (Stage) button.getScene().getWindow(); // Obtient la fenêtre actuelle
        stage.setTitle(title); // Définit le titre de la fenêtre
        stage.setScene(scene); // Définit la scène de la fenêtre
        stage.show(); // Affiche la fenêtre
    }

    /**
     * Méthode pour rediriger vers la vue de la liste des questions.
     * Elle appelle la méthode redirectView avec le bouton listeQuestionsButton, le nom du fichier FXML "questions-view.fxml", et le titre "Liste des questions".
     *
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    protected void redirectListeQuestionsView() throws IOException {
        redirectView(listeQuestionsButton, "questions-view.fxml", "Liste des questions");
    }

    /**
     * Méthode pour rediriger vers la vue de génération de quiz.
     * Elle appelle la méthode redirectView avec le bouton genererButton, le nom du fichier FXML "generer-view.fxml", et le titre "Générer un quiz".
     *
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    protected void redirectGenererView() throws IOException {
        redirectView(genererButton, "generer-view.fxml", "Générer un quiz");
    }
}