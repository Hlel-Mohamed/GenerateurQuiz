package net.universitecentrale.generateurquiz.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import net.universitecentrale.generateurquiz.GenerateurQuizApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.Question;
import net.universitecentrale.generateurquiz.entity.QuestionMCQ;
import net.universitecentrale.generateurquiz.entity.QuestionRemplirBlanc;
import net.universitecentrale.generateurquiz.entity.QuestionVraisFaux;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur pour la gestion des questions.
 */
public class QuestionsController {

    @FXML
    private Button menuButton; // Bouton pour retourner au menu
    @FXML
    private Button modifierButton; // Bouton pour modifier une question
    @FXML
    private Button ajouterButton;  // Bouton pour ajouter une question
    @FXML
    private TableView<Question> questionTableView; // Tableau pour afficher les questions
    @FXML
    private TableColumn<Question, Long> idColumn; // Colonne pour l'ID de la question dans le tableau
    @FXML
    private TableColumn<Question, String> questionColumn;  // Colonne pour le texte de la question dans le tableau
    @FXML
    private TableColumn<Question, String> sujetColumn; // Colonne pour le sujet de la question dans le tableau
    @FXML
    private TableColumn<Question, String> typeColumn; // Colonne pour le type de la question dans le tableau
    private static final Logger LOGGER = Logger.getLogger(QuestionsController.class.getName()); // Logger pour enregistrer les erreurs et les informations


    /**
     * Méthode d'initialisation du contrôleur. Elle est appelée après que tous les champs FXML ont été chargés.
     * Elle initialise les colonnes du tableau et charge les questions de la base de données.
     */
    @FXML
    public void initialize() {
        // Définition des factories pour les cellules des colonnes. Ces factories déterminent comment les valeurs sont extraites pour chaque cellule de la colonne.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id")); // Utilise la valeur de la propriété "id" de l'objet Question comme valeur de la cellule.
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("texte")); // Utilise la valeur de la propriété "texte" de l'objet Question comme valeur de la cellule.
        sujetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSujet().getTexte())); // Utilise la valeur de la propriété "texte" de l'objet Sujet associé à la Question comme valeur de la cellule.
        typeColumn.setCellValueFactory(cellData -> { // Détermine le type de la question et utilise une chaîne correspondante comme valeur de la cellule.
            if (cellData.getValue() instanceof QuestionMCQ) {
                return new SimpleStringProperty("MCQ");
            } else if (cellData.getValue() instanceof QuestionVraisFaux) {
                return new SimpleStringProperty("Vrai/Faux");
            } else if (cellData.getValue() instanceof QuestionRemplirBlanc) {
                return new SimpleStringProperty("Remplissez les blancs");
            } else {
                return new SimpleStringProperty("Erreur");
            }
        });

        // Création de la liste des questions. Cette liste est observable, ce qui signifie qu'elle peut notifier les vues de tout changement de données.
        ObservableList<Question> questions = FXCollections.observableArrayList();

        // Récupération des questions de la base de données.
        try (Connection connection = DatabaseConnection.getConnection()) { // Établit une connexion à la base de données.
            Statement statement = connection.createStatement(); // Crée un objet Statement pour envoyer des requêtes SQL à la base de données.

            // Exécute une requête SQL pour récupérer toutes les questions, leurs sujets et leurs types.
            ResultSet resultSet = statement.executeQuery("SELECT question.*, sujet.texte as sujet_texte, questionmcq.id as mcq_id, questionvraisfaux.id as vf_id, questionremplirblanc.id as rb_id FROM question INNER JOIN sujet ON question.idSujet = sujet.id LEFT JOIN questionmcq ON question.id = questionmcq.id LEFT JOIN questionvraisfaux ON question.id = questionvraisfaux.id LEFT JOIN questionremplirblanc ON question.id = questionremplirblanc.id");

            // Utilise la méthode getQuestionType du contrôleur GenererController pour déterminer le type de chaque question et ajouter la question à la liste.
            GenererController.getQuestionType(questions, resultSet);
        } catch (SQLException e) { // Attrape et gère les exceptions SQL.
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des questions", e); // Enregistre l'erreur dans le journal.
        }

        // Ajout des questions au tableau. Le tableau est mis à jour pour refléter les données de la liste de questions.
        questionTableView.setItems(questions);
    }

    /**
     * Cette méthode est appelée lorsque l'utilisateur clique sur le bouton "Ajouter une question".
     * Elle charge la vue d'édition à partir du fichier FXML et l'affiche dans une nouvelle fenêtre.
     * <p>
     * Voici les étapes détaillées de ce que fait cette méthode :
     * 1. Crée un FXMLLoader et charge le fichier FXML de la vue d'édition ("view/edit-view.fxml").
     * 2. Crée une nouvelle scène avec la vue chargée et une taille spécifique (640x480).
     * 3. Obtient la fenêtre actuelle à partir du bouton "Ajouter une question".
     * 4. Définit le titre de la fenêtre comme "Ajouter une question".
     * 5. Définit la scène de la fenêtre comme la scène créée précédemment.
     * 6. Affiche la fenêtre.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    public void ajouterQuestion() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/edit-view.fxml")); // Crée un FXMLLoader et charge le fichier FXML de la vue d'édition
        Scene scene = new Scene(fxmlLoader.load(), 640, 480); // Crée une nouvelle scène avec la vue chargée et une taille spécifique
        Stage stage = (Stage) ajouterButton.getScene().getWindow(); // Obtient la fenêtre actuelle à partir du bouton "Ajouter une question"
        stage.setTitle("Ajouter une question"); // Définit le titre de la fenêtre
        stage.setScene(scene); // Définit la scène de la fenêtre
        stage.show(); // Affiche la fenêtre
    }

    /**
     * Cette méthode est appelée lorsque l'utilisateur clique sur le bouton "Supprimer une question".
     * Elle supprime la question sélectionnée de la base de données et du tableau.
     * <p>
     * Voici les étapes détaillées de ce que fait cette méthode :
     * 1. Obtient la question sélectionnée dans le tableau.
     * 2. Si une question est sélectionnée :
     * a. Établit une connexion à la base de données.
     * b. En fonction du type de la question sélectionnée, prépare et exécute une requête SQL pour supprimer la question de la table correspondante.
     * c. Prépare et exécute une requête SQL pour supprimer la question de la table "question".
     * d. Si une erreur SQL se produit lors de l'exécution des requêtes, enregistre l'erreur avec le logger.
     * e. Supprime la question du tableau.
     */
    @FXML
    public void supprimerQuestion() {
        Question selectedQuestion = questionTableView.getSelectionModel().getSelectedItem();

        if (selectedQuestion != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {

                if (selectedQuestion instanceof QuestionMCQ) {
                    PreparedStatement preparedStatementMCQ = connection.prepareStatement("DELETE FROM questionmcq WHERE id = " + selectedQuestion.getId());
                    preparedStatementMCQ.execute();
                } else if (selectedQuestion instanceof QuestionVraisFaux) {
                    PreparedStatement preparedStatementVF = connection.prepareStatement("DELETE FROM questionvraisfaux WHERE id = " + selectedQuestion.getId());
                    preparedStatementVF.execute();
                } else if (selectedQuestion instanceof QuestionRemplirBlanc) {
                    PreparedStatement preparedStatementRB = connection.prepareStatement("DELETE FROM questionremplirblanc WHERE id = " + selectedQuestion.getId());
                    preparedStatementRB.execute();
                }

                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM question WHERE id = " + selectedQuestion.getId());
                preparedStatement.execute();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la question", e);
            }
            questionTableView.getItems().remove(selectedQuestion);
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur clique sur le bouton "Modifier une question".
     * Elle charge la vue d'édition avec la question sélectionnée et l'affiche.
     */
    @FXML
    public void modifierQuestion() {
        // Obtient la question sélectionnée dans le tableau
        Question selectedQuestion = questionTableView.getSelectionModel().getSelectedItem();

        // Si une question est sélectionnée
        if (selectedQuestion != null) {
            try {
                // Crée un FXMLLoader et charge le fichier FXML de la vue d'édition
                FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/edit-view.fxml"));
                // Crée une nouvelle scène avec la vue chargée
                Scene scene = new Scene(fxmlLoader.load());

                // Obtient le contrôleur de la vue d'édition à partir du FXMLLoader
                EditQuestionController editQuestionController = fxmlLoader.getController();
                // Définit la question sélectionnée dans le contrôleur de la vue d'édition
                editQuestionController.setQuestion(selectedQuestion);
                // Définit le texte du label titre dans le contrôleur de la vue d'édition comme "Modifier une question"
                editQuestionController.titreLabel.setText("Modifier une question");
                // Obtient la fenêtre actuelle à partir du bouton "Modifier une question"
                Stage stage = (Stage) modifierButton.getScene().getWindow();
                // Définit le titre de la fenêtre comme "Modifier une question"
                stage.setTitle("Modifier une question");
                // Définit la scène de la fenêtre comme la scène créée précédemment
                stage.setScene(scene);
            } catch (IOException e) {
                // Enregistre l'erreur dans le journal si une erreur d'entrée/sortie se produit lors du chargement de la vue à partir du fichier FXML
                LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue de modification", e);
            }
        }
    }

    /**
     * Cette méthode est appelée lorsque l'utilisateur clique sur le bouton "Retour au menu".
     * Elle charge la vue principale et l'affiche dans une nouvelle fenêtre.
     * <p>
     * Voici les étapes détaillées de ce que fait cette méthode :
     * 1. Crée un FXMLLoader et charge le fichier FXML de la vue principale ("view/main-view.fxml").
     * 2. Crée une nouvelle scène avec la vue chargée.
     * 3. Obtient la fenêtre actuelle à partir du bouton "Retour au menu".
     * 4. Définit le titre de la fenêtre comme "Générateur de quiz".
     * 5. Définit la scène de la fenêtre comme la scène créée précédemment.
     * 6. Affiche la fenêtre.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    public void retourMenu() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/main-view.fxml")); // Crée un FXMLLoader et charge le fichier FXML de la vue principale
        Scene scene = new Scene(fxmlLoader.load()); // Crée une nouvelle scène avec la vue chargée
        Stage stage = (Stage) menuButton.getScene().getWindow(); // Obtient la fenêtre actuelle à partir du bouton "Retour au menu"
        stage.setTitle("Générateur de quiz"); // Définit le titre de la fenêtre
        stage.setScene(scene); // Définit la scène de la fenêtre
        stage.show(); // Affiche la fenêtre
    }
}
