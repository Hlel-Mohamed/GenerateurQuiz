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
import net.universitecentrale.generateurquiz.GenerateurQuizApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cette classe est utilisée pour gérer la génération d'un quiz.
 */
public class GenererController {
    @FXML
    private ComboBox<String> sujetCombo; // ComboBox pour sélectionner le sujet du quiz
    @FXML
    private TextField nombreTextField; // TextField pour entrer le nombre de questions du quiz
    @FXML
    private Button menuButton; // Bouton pour retourner au menu
    private static final Logger LOGGER = Logger.getLogger(EditQuestionController.class.getName()); // Logger pour enregistrer les erreurs et les informations

    /**
     * Méthode d'initialisation du contrôleur. Elle est appelée après que tous les champs FXML ont été chargés.
     * Elle remplit le ComboBox des sujets avec les sujets existants.
     * Elle ajoute un listener au TextField du nombre de questions pour s'assurer que l'utilisateur ne peut entrer que des chiffres.
     */
    @FXML
    public void initialize() {
        remplirSujet(sujetCombo);

        nombreTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nombreTextField.setText(oldValue);
            }
        });
    }

    /**
     * Méthode pour générer un quiz.
     * Elle obtient le sujet et le nombre de questions à partir des champs de la vue.
     * Elle obtient les questions correspondantes de la base de données.
     * Ensuite, elle charge la vue du quiz, crée une nouvelle scène avec cette vue, et définit cette scène comme scène de la fenêtre actuelle.
     * Elle définit également les questions du quiz dans le contrôleur de la vue du quiz.
     * Si une erreur se produit lors du chargement de la vue, elle enregistre l'erreur avec le logger.
     */
    @FXML
    public void genererQuiz() {
        String sujetTextValue = sujetCombo.getValue();
        int nombreQuestions = Integer.parseInt(nombreTextField.getText());

        List<Question> questions = getQuestionsBySujet(sujetTextValue, nombreQuestions);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/quiz-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            QuizController quizController = fxmlLoader.getController();
            quizController.setQuestions(questions);

            Stage stage = (Stage) menuButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue du quiz", e);
        }
    }

    /**
     * Cette méthode est utilisée pour obtenir une liste de questions par sujet.
     * Elle exécute une requête SQL pour obtenir les questions de la base de données.
     * La requête SQL sélectionne toutes les colonnes de la table "question" et des tables associées où le texte du sujet correspond au texte du sujet donné.
     * Elle limite le nombre de questions retournées au nombre de questions donné et les ordonne de manière aléatoire.
     * Ensuite, elle appelle la méthode getQuestionType() pour obtenir le type de chaque question et ajoute la question à la liste des questions.
     * Si une erreur SQL se produit lors de l'exécution de la requête, elle enregistre l'erreur avec le logger.
     *
     * @param sujetTextValue  Le texte du sujet pour lequel obtenir les questions.
     * @param nombreQuestions Le nombre de questions à obtenir.
     * @return Une liste des questions pour le sujet donné. Chaque question est une instance de la classe Question ou de l'une de ses sous-classes.
     */
    private List<Question> getQuestionsBySujet(String sujetTextValue, int nombreQuestions) {
        List<Question> questions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT question.*, sujet.texte as sujet_texte, questionmcq.id as mcq_id, questionvraisfaux.id as vf_id, questionremplirblanc.id as rb_id FROM question INNER JOIN sujet ON question.idSujet = sujet.id LEFT JOIN questionmcq ON question.id = questionmcq.id LEFT JOIN questionvraisfaux ON question.id = questionvraisfaux.id LEFT JOIN questionremplirblanc ON question.id = questionremplirblanc.id  WHERE sujet.texte = ? ORDER BY RAND() LIMIT ?");
            preparedStatement.setString(1, sujetTextValue);
            preparedStatement.setInt(2, nombreQuestions);
            ResultSet resultSet = preparedStatement.executeQuery();

            getQuestionType(questions, resultSet);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des questions", e);
        }

        return questions;
    }

    /**
     * Cette méthode est utilisée pour obtenir le type d'une question.
     * Elle parcourt chaque ligne du ResultSet donné.
     * Pour chaque ligne, elle obtient l'ID, le texte de la question et le texte du sujet.
     * Ensuite, en fonction des valeurs des colonnes "mcq_id", "vf_id" et "rb_id", elle crée une nouvelle question du type approprié et l'ajoute à la liste des questions.
     *
     * @param questions Une liste pour stocker les questions.
     * @param resultSet Un ResultSet contenant les résultats d'une requête SQL pour obtenir les questions.
     * @throws SQLException Si une erreur SQL se produit lors de l'obtention des valeurs des colonnes du ResultSet.
     */
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

    /**
     * Cette méthode est utilisée pour obtenir un sujet par son ID.
     * Elle exécute une requête SQL pour obtenir le sujet de la base de données.
     * La requête SQL sélectionne toutes les colonnes de la table "sujet" où l'ID correspond à l'ID du sujet donné.
     * Si la requête retourne un résultat, elle crée un nouveau sujet avec les valeurs des colonnes et renvoie ce sujet.
     *
     * @param idSujet    L'ID du sujet à obtenir.
     * @param connection Une connexion à la base de données.
     * @return Le sujet correspondant à l'ID donné, ou null si la requête ne retourne pas de résultat.
     * @throws SQLException Si une erreur SQL se produit lors de l'exécution de la requête.
     */
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

    /**
     * Cette méthode est utilisée pour remplir le ComboBox des sujets avec les sujets existants.
     * Elle exécute une requête SQL pour obtenir les sujets de la base de données.
     * La requête SQL sélectionne les colonnes "id" et "texte" de la table "sujet".
     * Ensuite, elle parcourt chaque ligne du résultat de la requête.
     * Pour chaque ligne, elle crée un nouveau sujet et l'ajoute à la liste des sujets.
     * Enfin, elle ajoute les textes des sujets à la liste des items du ComboBox des sujets.
     *
     * @param sujetCombo Un ComboBox pour afficher les sujets.
     */
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
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des sujets", e);
        }
    }

    /**
     * Cette méthode est utilisée pour retourner au menu de l'application.
     * Elle charge la vue du menu à partir du fichier FXML, crée une nouvelle scène avec cette vue, et définit cette scène comme scène de la fenêtre actuelle.
     * Elle définit également le titre de la fenêtre comme "Générer un quiz" et affiche la fenêtre.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    public void retour() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) menuButton.getScene().getWindow();
        stage.setTitle("Générer un quiz");
        stage.setScene(scene);
        stage.show();
    }
}
