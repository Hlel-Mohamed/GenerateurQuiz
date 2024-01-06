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
import net.universitecentrale.generateurquiz.HelloApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.*;

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




    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("texte"));
        sujetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSujet().getTexte()));
        typeColumn.setCellValueFactory(cellData -> {
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

        ObservableList<Question> questions = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT question.*, sujet.texte as sujet_texte, questionmcq.id as mcq_id, questionvraisfaux.id as vf_id, questionremplirblanc.id as rb_id FROM question INNER JOIN sujet ON question.idSujet = sujet.id LEFT JOIN questionmcq ON question.id = questionmcq.id LEFT JOIN questionvraisfaux ON question.id = questionvraisfaux.id LEFT JOIN questionremplirblanc ON question.id = questionremplirblanc.id");

            GenererController.getQuestionType(questions, resultSet);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des questions", e);
        }

        questionTableView.setItems(questions);
    }

    @FXML
    public void ajouterQuestion() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/edit-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        Stage stage = (Stage) ajouterButton.getScene().getWindow();
        stage.setTitle("Ajouter une question");
        stage.setScene(scene);
        stage.show();

    }

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

    @FXML
    public void modifierQuestion() {
        Question selectedQuestion = questionTableView.getSelectionModel().getSelectedItem();

        if (selectedQuestion != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/edit-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());

                EditQuestionController editQuestionController = fxmlLoader.getController();
                editQuestionController.setQuestion(selectedQuestion);
                editQuestionController.titreLabel.setText("Modifier une question");
                Stage stage = (Stage) modifierButton.getScene().getWindow();
                stage.setTitle("Modifier une question");
                stage.setScene(scene);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue de modification", e);
            }
        }
    }

    @FXML
    public void retourMenu() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) menuButton.getScene().getWindow();
        stage.setTitle("Générateur de quiz");
        stage.setScene(scene);
        stage.show();
    }
}
