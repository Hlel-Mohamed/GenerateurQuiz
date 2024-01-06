package net.universitecentrale.generateurquiz.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.universitecentrale.generateurquiz.HelloApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.*;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizController {
    @FXML
    private VBox questionContainer; // Conteneur pour les questions du quiz
    @FXML
    private Button menuButton; // Bouton pour retourner au menu
    private List<Question> questions; // Liste des questions du quiz
    private final Map<Question, Integer> vraiFauxIndices = new HashMap<>(); // Map pour stocker les indices des questions vrai/faux
    private final Map<Question, List<Integer>> mcqIndices = new HashMap<>(); // Map pour stocker les indices des questions à choix multiples
    private final Map<Question, Label> remplirBlancLabels = new HashMap<>(); // Map pour stocker les labels des questions à remplir
    private static final Logger LOGGER = Logger.getLogger(QuizController.class.getName()); // Logger pour enregistrer les erreurs

    /**
     * Cette méthode est utilisée pour définir les questions du quiz.
     * Après avoir défini les questions, elle appelle la méthode displayQuestions() pour afficher les questions.
     *
     * @param questions Une liste de questions à définir pour le quiz.
     *                  Chaque question est une instance de la classe Question ou de l'une de ses sous-classes.
     */
    public void setQuestions(List<Question> questions) {
        this.questions = questions; // Définit les questions du quiz.
        displayQuestions(); // Affiche les questions du quiz.
    }

    /**
     * Cette méthode est utilisée pour afficher les questions du quiz dans le conteneur de questions.
     * Elle parcourt chaque question dans la liste des questions et crée un label pour chaque question.
     * Ensuite, en fonction du type de question, elle crée et ajoute les éléments appropriés au conteneur de questions.
     * Pour les questions vrai/faux, elle crée deux CheckBox pour "Vrai" et "Faux".
     * Pour les questions à choix multiples, elle crée une CheckBox pour chaque option.
     * Pour les questions à remplir, elle crée un label avec des underscores pour représenter l'espace de réponse.
     */
    private void displayQuestions() {
        for (Question question : questions) {
            Label questionLabel = new Label("- " + question.getTexte()); // Crée un label pour la question
            questionContainer.getChildren().add(questionLabel); // Ajoute le label à la liste des enfants du conteneur de questions
            if (question instanceof QuestionVraisFaux) {
                CheckBox vraiCheckBox = new CheckBox("Vrai"); // Crée une CheckBox pour "Vrai"
                vraiCheckBox.setMouseTransparent(true);
                vraiCheckBox.setPadding(new Insets(0, 0, 0, 20)); // Ajoute du padding
                CheckBox fauxCheckBox = new CheckBox("Faux"); // Crée une CheckBox pour "Faux"
                fauxCheckBox.setMouseTransparent(true);
                fauxCheckBox.setPadding(new Insets(0, 0, 0, 20)); // Ajoute du padding
                questionContainer.getChildren().addAll(vraiCheckBox, fauxCheckBox); // Ajoute les CheckBox à la liste des enfants du conteneur de questions
                vraiFauxIndices.put(question, questionContainer.getChildren().size() - 2); // Stocke l'indice de la CheckBox "Vrai" dans la map vraiFauxIndices
            } else if (question instanceof QuestionMCQ) {
                List<Option> options = getOptionsForMCQQuestion(question.getId()); // Obtient les options pour la question à choix multiples
                List<Integer> indices = new ArrayList<>();
                for (Option option : options) {
                    CheckBox optionCheckBox = new CheckBox(option.getTexte()); // Crée une CheckBox pour chaque option
                    optionCheckBox.setMouseTransparent(true);
                    optionCheckBox.setPadding(new Insets(0, 0, 0, 20)); // Ajoute du padding
                    questionContainer.getChildren().add(optionCheckBox); // Ajoute la CheckBox à la liste des enfants du conteneur de questions
                    indices.add(questionContainer.getChildren().size() - 1); // Ajoute l'indice de la CheckBox à la liste des indices
                }
                mcqIndices.put(question, indices); // Stocke la liste des indices dans la map mcqIndices
            } else if (question instanceof QuestionRemplirBlanc) {
                Label responseLabel = new Label("_____________________________"); // Crée un label pour l'espace de réponse
                responseLabel.setPadding(new Insets(0, 0, 0, 20)); // Ajoute du padding
                questionContainer.getChildren().add(responseLabel); // Ajoute le label à la liste des enfants du conteneur de questions
                remplirBlancLabels.put(question, responseLabel); // Stocke le label dans la map remplirBlancLabels
            }
        }
    }

    /**
     * Cette méthode est utilisée pour générer les réponses du quiz.
     * Elle parcourt chaque question dans la liste des questions et, en fonction du type de question, elle génère la réponse appropriée.
     * Pour les questions vrai/faux, elle obtient la réponse correcte et sélectionne la CheckBox correspondante.
     * Pour les questions à choix multiples, elle obtient les options correctes et sélectionne les CheckBox correspondantes.
     * Pour les questions à remplir, elle obtient la réponse et la définit comme texte du label correspondant.
     */
    @FXML
    public void genererReponse() {
        for (Question question : questions) {
            if (question instanceof QuestionVraisFaux) {
                boolean correctAnswer = getCorrectAnswerForVraisFauxQuestion(question.getId()); // Obtient la réponse correcte
                CheckBox vraiCheckBox = (CheckBox) questionContainer.getChildren().get(vraiFauxIndices.get(question)); // Obtient la CheckBox pour "Vrai"
                CheckBox fauxCheckBox = (CheckBox) questionContainer.getChildren().get(vraiFauxIndices.get(question) + 1); // Obtient la CheckBox pour "Faux"
                vraiCheckBox.setSelected(correctAnswer); // Sélectionne la CheckBox "Vrai" si la réponse est vraie
                fauxCheckBox.setSelected(!correctAnswer); // Sélectionne la CheckBox "Faux" si la réponse est fausse
            } else if (question instanceof QuestionMCQ) {
                List<Option> correctOptions = getCorrectOptionsForMCQQuestion(question.getId()); // Obtient les options correctes
                List<Integer> indices = mcqIndices.get(question); // Obtient les indices des CheckBox pour les options
                for (int i = 0; i < correctOptions.size(); i++) {
                    CheckBox optionCheckBox = (CheckBox) questionContainer.getChildren().get(indices.get(i)); // Obtient la CheckBox pour l'option
                    optionCheckBox.setSelected(correctOptions.get(i).isCorrecte()); // Sélectionne la CheckBox si l'option est correcte
                }
            } else if (question instanceof QuestionRemplirBlanc) {
                String response = getResponseForRemplirBlancQuestion(question.getId()); // Obtient la réponse
                Label responseLabel = remplirBlancLabels.get(question); // Obtient le label pour la réponse
                responseLabel.setText(response); // Définit la réponse comme texte du label
            }
        }
    }

    /**
     * Cette méthode est utilisée pour exporter le quiz en format txt.
     * Elle crée un FileWriter pour écrire dans le fichier "quiz.txt".
     * Ensuite, elle parcourt chaque enfant du conteneur de questions.
     * Si l'enfant est un label, elle écrit le texte du label dans le fichier.
     * Si l'enfant est une CheckBox, elle écrit le texte de la CheckBox dans le fichier.
     * Si la CheckBox est sélectionnée, elle ajoute " Correcte" après le texte.
     * En cas d'erreur d'écriture dans le fichier, elle enregistre l'erreur avec le logger.
     */
    @FXML
    public void exporterTxt() {
        try (FileWriter writer = new FileWriter("quiz.txt")) { // Crée un FileWriter pour écrire dans le fichier "quiz.txt"
            for (Node child : questionContainer.getChildren()) { // Parcourt chaque enfant du conteneur de questions
                if (child instanceof Label label) { // Si l'enfant est un label
                    String labelText = label.getText();
                    if (!labelText.startsWith("-")) {
                        labelText = "\t\t" + labelText; // Add tabulation at the beginning of the response
                    }
                    writer.write(labelText + "\n"); // Écrit le texte du label dans le fichier
                } else if (child instanceof CheckBox checkBox) { // Si l'enfant est une CheckBox
                    if (checkBox.isSelected()) { // Si la CheckBox est sélectionnée
                        writer.write("\t\t"+checkBox.getText() + "\tCorrecte\n"); // Écrit le texte de la CheckBox et " Correcte" dans le fichier
                    } else {
                        writer.write("\t\t"+checkBox.getText() + "\n"); // Sinon, écrit seulement le texte de la CheckBox dans le fichier
                    }
                }
            }
        } catch (IOException e) { // En cas d'erreur d'écriture dans le fichier
            LOGGER.log(Level.SEVERE, "Erreur lors de l'écriture du fichier", e); // Enregistre l'erreur avec le logger
        }
    }

    /**
     * Cette méthode est utilisée pour obtenir la réponse correcte pour une question vrai/faux.
     * Elle exécute une requête SQL pour obtenir la réponse correcte de la base de données.
     * La requête SQL sélectionne la colonne "reponseCorrecte" de la table "questionvraisfaux" où l'ID correspond à l'ID de la question.
     * Si la requête retourne un résultat, elle renvoie la valeur de la colonne "reponseCorrecte" comme réponse correcte.
     * Si une erreur SQL se produit lors de l'exécution de la requête, elle enregistre l'erreur avec le logger.
     * Si la requête ne retourne pas de résultat, elle renvoie false comme réponse par défaut.
     *
     * @param questionId L'ID de la question pour laquelle obtenir la réponse correcte.
     * @return La réponse correcte pour la question, ou false si la requête ne retourne pas de résultat.
     */
    private boolean getCorrectAnswerForVraisFauxQuestion(Long questionId) {
        String query = "SELECT reponseCorrecte FROM questionvraisfaux WHERE id = ?"; // La requête SQL pour obtenir la réponse correcte
        try (Connection connection = DatabaseConnection.getConnection(); // Obtient une connexion à la base de données
             PreparedStatement preparedStatement = createPreparedStatement(connection, query, questionId); // Crée un PreparedStatement pour exécuter la requête
             ResultSet resultSet = preparedStatement.executeQuery()) { // Exécute la requête et obtient le résultat
            if (resultSet.next()) { // Si la requête retourne un résultat
                return resultSet.getBoolean("reponseCorrecte"); // Renvoie la valeur de la colonne "reponseCorrecte" comme réponse correcte
            }
        } catch (SQLException e) { // Si une erreur SQL se produit lors de l'exécution de la requête
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la réponse correcte pour la question Vrais/Faux", e); // Enregistre l'erreur avec le logger
        }
        return false; // Renvoie false comme réponse par défaut si la requête ne retourne pas de résultat
    }

    /**
     * Cette méthode est utilisée pour obtenir les options correctes pour une question à choix multiples.
     * Elle exécute une requête SQL pour obtenir les options de la base de données.
     * La requête SQL sélectionne toutes les colonnes de la table "options" où l'ID de la question correspond à l'ID de la question donné.
     * Elle parcourt ensuite chaque ligne du résultat de la requête.
     * Pour chaque ligne, elle obtient le texte de l'option et si elle est correcte, puis elle ajoute une nouvelle option à la liste des options.
     * Si une erreur SQL se produit lors de l'exécution de la requête, elle enregistre l'erreur avec le logger.
     *
     * @param questionId L'ID de la question pour laquelle obtenir les options correctes.
     * @return Une liste des options correctes pour la question. Chaque option est une instance de la classe Option.
     */
    private List<Option> getCorrectOptionsForMCQQuestion(Long questionId) {
        List<Option> options = new ArrayList<>(); // Liste pour stocker les options correctes
        String query = "SELECT * FROM options WHERE idQuestion = ?"; // La requête SQL pour obtenir les options
        try (Connection connection = DatabaseConnection.getConnection(); // Obtient une connexion à la base de données
             PreparedStatement preparedStatement = createPreparedStatement(connection, query, questionId); // Crée un PreparedStatement pour exécuter la requête
             ResultSet resultSet = preparedStatement.executeQuery()) { // Exécute la requête et obtient le résultat
            while (resultSet.next()) { // Parcourt chaque ligne du résultat
                String texte = resultSet.getString("texte"); // Obtient le texte de l'option
                boolean correcte = resultSet.getBoolean("correcte"); // Obtient si l'option est correcte
                options.add(new Option(texte, correcte, null)); // Ajoute une nouvelle option à la liste des options
            }
        } catch (SQLException e) { // Si une erreur SQL se produit lors de l'exécution de la requête
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des options correctes pour la question MCQ", e); // Enregistre l'erreur avec le logger
        }
        return options; // Renvoie la liste des options correctes
    }

    /**
     * Cette méthode est utilisée pour obtenir la réponse à une question à remplir.
     * Elle exécute une requête SQL pour obtenir la réponse de la base de données.
     * La requête SQL sélectionne la colonne "reponse" de la table "questionremplirblanc" où l'ID correspond à l'ID de la question.
     * Si la requête retourne un résultat, elle renvoie la valeur de la colonne "reponse" comme réponse.
     * Si une erreur SQL se produit lors de l'exécution de la requête, elle enregistre l'erreur avec le logger.
     * Si la requête ne retourne pas de résultat, elle renvoie une chaîne vide comme réponse par défaut.
     *
     * @param questionId L'ID de la question pour laquelle obtenir la réponse.
     * @return La réponse à la question, ou une chaîne vide si la requête ne retourne pas de résultat.
     */
    private String getResponseForRemplirBlancQuestion(Long questionId) {
        String query = "SELECT reponse FROM questionremplirblanc WHERE id = ?"; // La requête SQL pour obtenir la réponse
        try (Connection connection = DatabaseConnection.getConnection(); // Obtient une connexion à la base de données
             PreparedStatement preparedStatement = createPreparedStatement(connection, query, questionId); // Crée un PreparedStatement pour exécuter la requête
             ResultSet resultSet = preparedStatement.executeQuery()) { // Exécute la requête et obtient le résultat
            if (resultSet.next()) { // Si la requête retourne un résultat
                return resultSet.getString("reponse"); // Renvoie la valeur de la colonne "reponse" comme réponse
            }
        } catch (SQLException e) { // Si une erreur SQL se produit lors de l'exécution de la requête
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la réponse pour la question RemplirBlanc", e); // Enregistre l'erreur avec le logger
        }
        return ""; // Renvoie une chaîne vide comme réponse par défaut si la requête ne retourne pas de résultat
    }

    /**
     * Cette méthode est utilisée pour obtenir les options pour une question à choix multiples.
     * Elle exécute une requête SQL pour obtenir les options de la base de données.
     * La requête SQL sélectionne toutes les colonnes de la table "options" où l'ID de la question correspond à l'ID de la question donné.
     * Elle parcourt ensuite chaque ligne du résultat de la requête.
     * Pour chaque ligne, elle obtient le texte de l'option et si elle est correcte, puis elle ajoute une nouvelle option à la liste des options.
     * Si une erreur SQL se produit lors de l'exécution de la requête, elle enregistre l'erreur avec le logger.
     *
     * @param questionId L'ID de la question pour laquelle obtenir les options.
     * @return Une liste des options pour la question. Chaque option est une instance de la classe Option.
     */
    private List<Option> getOptionsForMCQQuestion(Long questionId) {
        List<Option> options = new ArrayList<>(); // Liste pour stocker les options
        String query = "SELECT * FROM options WHERE idQuestion = ?"; // La requête SQL pour obtenir les options
        try (Connection connection = DatabaseConnection.getConnection(); // Obtient une connexion à la base de données
             PreparedStatement preparedStatement = createPreparedStatement(connection, query, questionId); // Crée un PreparedStatement pour exécuter la requête
             ResultSet resultSet = preparedStatement.executeQuery()) { // Exécute la requête et obtient le résultat
            while (resultSet.next()) { // Parcourt chaque ligne du résultat
                String texte = resultSet.getString("texte"); // Obtient le texte de l'option
                boolean correcte = resultSet.getBoolean("correcte"); // Obtient si l'option est correcte
                options.add(new Option(texte, correcte, null)); // Ajoute une nouvelle option à la liste des options
            }
        } catch (SQLException e) { // Si une erreur SQL se produit lors de l'exécution de la requête
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des options pour la question MCQ", e); // Enregistre l'erreur avec le logger
        }
        return options; // Renvoie la liste des options
    }

    /**
     * Cette méthode est utilisée pour retourner au menu de l'application.
     * Elle crée un FXMLLoader pour charger la vue du menu à partir du fichier FXML.
     * Ensuite, elle crée une nouvelle scène avec la vue chargée et définit cette scène comme scène de la fenêtre actuelle.
     * Elle définit également le titre de la fenêtre et affiche la fenêtre.
     *
     * @throws IOException Si une erreur se produit lors du chargement de la vue à partir du fichier FXML.
     */
    @FXML
    public void retourMenu() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/generer-view.fxml")); // Crée un FXMLLoader pour charger la vue
        Scene scene = new Scene(fxmlLoader.load()); // Crée une nouvelle scène avec la vue chargée
        Stage stage = (Stage) menuButton.getScene().getWindow(); // Obtient la fenêtre actuelle
        stage.setTitle("Générateur de quiz"); // Définit le titre de la fenêtre
        stage.setScene(scene); // Définit la scène de la fenêtre
        stage.show(); // Affiche la fenêtre
    }

    /**
     * Cette méthode est utilisée pour créer un PreparedStatement pour exécuter une requête SQL.
     * Elle crée un PreparedStatement à partir de la connexion à la base de données, de la requête SQL et de l'ID de la question.
     * Ensuite, elle définit l'ID de la question comme premier paramètre de la requête SQL.
     * Enfin, elle renvoie le PreparedStatement créé.
     *
     * @param connection La connexion à la base de données.
     * @param query      La requête SQL.
     * @param questionId L'ID de la question pour laquelle exécuter la requête.
     * @return Le PreparedStatement créé pour exécuter la requête.
     * @throws SQLException Si une erreur se produit lors de la création du PreparedStatement.
     */
    private PreparedStatement createPreparedStatement(Connection connection, String query, Long questionId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query); // Crée un PreparedStatement pour exécuter la requête
        preparedStatement.setLong(1, questionId); // Définit l'ID de la question comme premier paramètre de la requête
        return preparedStatement; // Renvoie le PreparedStatement créé
    }
}