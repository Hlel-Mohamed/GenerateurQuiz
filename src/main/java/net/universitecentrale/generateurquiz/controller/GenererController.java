package net.universitecentrale.generateurquiz.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.universitecentrale.generateurquiz.HelloApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenererController {
    @FXML
    private ComboBox<String> sujetCombo;
    @FXML
    private TextField nombreTextField;
    @FXML
    private Button menuButton;

    @FXML
    public void initialize() {
        remplirSujet(sujetCombo);

        nombreTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nombreTextField.setText(oldValue);
            }
        });
    }

    @FXML
    public void genererQuiz() {
        String sujetTextValue = sujetCombo.getValue();
        int nombreQuestions = Integer.parseInt(nombreTextField.getText());

        List<Question> questions = getQuestionsBySujet(sujetTextValue, nombreQuestions);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/quiz-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            QuizController quizController = fxmlLoader.getController();
            quizController.setQuestions(questions);

            Stage stage = (Stage) menuButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de la vue du quiz");
            e.printStackTrace();
        }
    }

    private List<Question> getQuestionsBySujet(String sujetTextValue, int nombreQuestions) {
        List<Question> questions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT question.*, sujet.texte as sujet_texte, questionmcq.id as mcq_id, questionvraisfaux.id as vf_id, questionremplirblanc.id as rb_id FROM question INNER JOIN sujet ON question.idSujet = sujet.id LEFT JOIN questionmcq ON question.id = questionmcq.id LEFT JOIN questionvraisfaux ON question.id = questionvraisfaux.id LEFT JOIN questionremplirblanc ON question.id = questionremplirblanc.id  WHERE sujet.texte = ? ORDER BY RAND() LIMIT ?");
            preparedStatement.setString(1, sujetTextValue);
            preparedStatement.setInt(2, nombreQuestions);
            ResultSet resultSet = preparedStatement.executeQuery();

            getQuestionType(questions, resultSet);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des questions");
            e.printStackTrace();
        }

        return questions;
    }

    public static void getQuestionType(List<Question> questions, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            Long id = resultSet.getLong("question.id");
            String questionTexte = resultSet.getString("question.texte");
            String sujetTexte = resultSet.getString("sujet_texte");
            Sujet sujet = new Sujet(sujetTexte, null);
            Question question;

            if (resultSet.getObject("mcq_id") != null) {
                question = new QuestionMCQ(id, questionTexte, sujet, null);
            } else if (resultSet.getObject("vf_id") != null) {
                question = new QuestionVraisFaux(id, questionTexte, sujet, false);
            } else if (resultSet.getObject("rb_id") != null) {
                question = new QuestionRemplirBlanc(id, questionTexte, sujet, "");
            } else {
                question = new Question(id, questionTexte, sujet);
            }

            questions.add(question);
        }
    }

    private Sujet getSujetById(Long idSujet, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM sujet WHERE id = ?");
        preparedStatement.setLong(1, idSujet);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String texte = resultSet.getString("texte");
            return new Sujet(texte, null);
        }

        return null;
    }

    static void remplirSujet(ComboBox<String> sujetCombo) {
        ObservableList<Sujet> sujets = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, texte FROM sujet");

            while (resultSet.next()) {
                Long sujetId = resultSet.getLong("id");
                String sujetTexte = resultSet.getString("texte");
                Sujet sujet = new Sujet(sujetId, sujetTexte, null);

                sujets.add(sujet);
            }
            sujetCombo.getItems().addAll(sujets.stream().map(Sujet::getTexte).toList());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des sujets");
            e.printStackTrace();
        }
    }

    @FXML
    public void retour() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) menuButton.getScene().getWindow();
        stage.setTitle("Générer un quiz");
        stage.setScene(scene);
        stage.show();
    }
}
