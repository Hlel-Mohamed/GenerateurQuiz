package net.universitecentrale.generateurquiz.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.universitecentrale.generateurquiz.GenerateurQuizApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditQuestionController {

    @FXML
    Label titreLabel; // Label pour le titre de la vue
    @FXML
    private ComboBox<String> sujetCombo; // ComboBox pour le sujet de la question
    @FXML
    private ComboBox<String> questionTypeCombo; // ComboBox pour le type de la question
    @FXML
    private TextField questionTexteTextfield; // TextField pour le texte de la question
    @FXML
    private TextField reponseTexteTextfield; // TextField pour la réponse de la question
    @FXML
    private Button ajouterOptionButton; // Bouton pour ajouter une option à une question à choix multiples
    @FXML
    private Button retournerButton; // Bouton pour retourner à la liste des questions
    @FXML
    private TableView<Option> optionsTableView; // TableView pour afficher les options d'une question à choix multiples
    @FXML
    private TableColumn<Option, String> optionTexteColumn; // Colonne pour le texte des options dans le TableView
    @FXML
    private TableColumn<Option, Boolean> optionCorrecteColumn; // Colonne pour indiquer si une option est correcte dans le TableView
    @FXML
    private RadioButton vraiRadio; // RadioButton pour indiquer que la réponse à une question vrai/faux est "Vrai"
    @FXML
    private RadioButton fauxRadio; // RadioButton pour indiquer que la réponse à une question vrai/faux est "Faux"
    @FXML
    private Label reponseLabel; // Label pour la réponse à une question à remplir
    @FXML
    private Label optionLabel; // Label pour les options d'une question à choix multiples
    private Question question; // La question à éditer
    private boolean isModifying = false; // Indique si l'utilisateur est en train de modifier une question existante
    private ObservableList<Option> options = FXCollections.observableArrayList(); // Liste observable des options d'une question à choix multiples
    private static final Logger LOGGER = Logger.getLogger(EditQuestionController.class.getName()); // Logger pour afficher les messages de débogage

    /**
     * Méthode d'initialisation du contrôleur. Elle est appelée après que tous les champs FXML ont été chargés.
     * Elle remplit le ComboBox des sujets avec les sujets existants.
     * Elle ajoute les types de questions possibles au ComboBox des types de questions.
     * Elle ajoute un listener au ComboBox des types de questions pour afficher les éléments appropriés en fonction du type de question sélectionné.
     * Elle définit les factories pour les cellules des colonnes du TableView des options.
     * Elle ajoute une colonne "Effacer" au TableView des options pour permettre à l'utilisateur de supprimer une option.
     * Si l'utilisateur est en train de modifier une question existante, elle désactive les ComboBox des sujets et des types de questions.
     */
    @FXML
    public void initialize() {
        // Remplit le ComboBox des sujets avec les sujets existants
        GenererController.remplirSujet(sujetCombo);

        // Ajoute les types de questions possibles au ComboBox des types de questions
        questionTypeCombo.getItems().addAll("MCQ", "Vrai/Faux", "Remplissez les blancs");

        // Ajoute un listener au ComboBox des types de questions pour afficher les éléments appropriés en fonction du type de question sélectionné
        questionTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("MCQ".equals(newValue)) {
                optionsTableView.setVisible(true);
                vraiRadio.setVisible(false);
                fauxRadio.setVisible(false);
                reponseTexteTextfield.setVisible(false);
                reponseLabel.setVisible(false);
                optionLabel.setVisible(true);
                ajouterOptionButton.setVisible(true);
            } else if ("Vrai/Faux".equals(newValue)) {
                optionsTableView.setVisible(false);
                vraiRadio.setVisible(true);
                fauxRadio.setVisible(true);
                reponseTexteTextfield.setVisible(false);
                reponseLabel.setVisible(false);
                optionLabel.setVisible(true);
                ajouterOptionButton.setVisible(false);
            } else if ("Remplissez les blancs".equals(newValue)) {
                optionsTableView.setVisible(false);
                vraiRadio.setVisible(false);
                fauxRadio.setVisible(false);
                reponseTexteTextfield.setVisible(true);
                reponseLabel.setVisible(true);
                optionLabel.setVisible(false);
                ajouterOptionButton.setVisible(false);
            } else {
                optionsTableView.setVisible(false);
                vraiRadio.setVisible(false);
                fauxRadio.setVisible(false);
                reponseTexteTextfield.setVisible(false);
                reponseLabel.setVisible(false);
                optionLabel.setVisible(false);
            }
        });

        // Définit les factories pour les cellules des colonnes du TableView des options
        optionsTableView.setItems(options);
        optionTexteColumn.setCellValueFactory(new PropertyValueFactory<>("texte"));
        optionCorrecteColumn.setCellValueFactory(new PropertyValueFactory<>("correcte"));

        // Ajoute une colonne "Effacer" au TableView des options pour permettre à l'utilisateur de supprimer une option
        TableColumn<Option, Void> deleteColumn = getDeleteColumn();
        optionsTableView.getColumns().add(deleteColumn);

        // Si l'utilisateur est en train de modifier une question existante, désactive les ComboBox des sujets et des types de questions
        if (isModifying) {
            sujetCombo.setDisable(true);
            questionTypeCombo.setDisable(true);
        }
    }


    /**
     * Cette méthode est utilisée pour créer une colonne "Effacer" pour le TableView des options.
     * Elle crée une nouvelle TableColumn et définit sa cell factory.
     * La cell factory crée une nouvelle TableCell pour chaque cellule de la colonne.
     * Chaque TableCell contient un bouton "X" qui, lorsqu'il est cliqué, supprime l'option correspondante de la liste des options.
     * Si la cellule est vide, aucun graphique n'est défini pour la cellule.
     *
     * @return La TableColumn créée pour la colonne "Effacer".
     */
    private TableColumn<Option, Void> getDeleteColumn() {
        TableColumn<Option, Void> deleteColumn = new TableColumn<>("Effacer"); // Crée une nouvelle TableColumn avec le titre "Effacer"
        deleteColumn.setCellFactory(param -> new TableCell<>() { // Définit la cell factory pour la TableColumn
            private final Button deleteButton = new Button("X"); // Crée un bouton "X" pour chaque cellule de la TableColumn

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) { // Si la cellule est vide
                    setGraphic(null); // Ne définit aucun graphique pour la cellule
                } else { // Si la cellule n'est pas vide
                    deleteButton.setOnAction(event -> { // Ajoute un gestionnaire d'événements au bouton "X"
                        Option option = getTableView().getItems().get(getIndex()); // Obtient l'option correspondante à la cellule
                        options.remove(option); // Supprime l'option de la liste des options
                    });
                    setGraphic(deleteButton); // Définit le bouton "X" comme graphique de la cellule
                }
            }
        });
        return deleteColumn; // Renvoie la TableColumn créée
    }

    /**
     * Cette méthode est utilisée pour ajouter une option à une question à choix multiples.
     * Elle crée et affiche une boîte de dialogue qui permet à l'utilisateur d'entrer le texte de l'option et d'indiquer si l'option est correcte.
     * Si l'utilisateur clique sur "OK", l'option est ajoutée à la liste des options.
     */
    @FXML
    public void ajouterOption() {
        Dialog<Option> dialog = new Dialog<>(); // Crée une nouvelle boîte de dialogue
        dialog.setTitle("Ajouter Option"); // Définit le titre de la boîte de dialogue

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE); // Crée un nouveau type de bouton pour "OK"
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL); // Ajoute les types de boutons à la boîte de dialogue

        GridPane grid = new GridPane(); // Crée un nouveau GridPane pour organiser les éléments de la boîte de dialogue
        grid.setHgap(10); // Définit l'espacement horizontal entre les cellules du GridPane
        grid.setVgap(10); // Définit l'espacement vertical entre les cellules du GridPane
        grid.setPadding(new Insets(20, 150, 10, 10)); // Définit le padding du GridPane

        TextField texte = new TextField(); // Crée un nouveau TextField pour le texte de l'option
        texte.setPromptText("Texte"); // Définit le texte d'invite du TextField
        CheckBox correcte = new CheckBox(); // Crée une nouvelle CheckBox pour indiquer si l'option est correcte

        grid.add(new Label("Texte:"), 0, 0); // Ajoute un label et le TextField au GridPane
        grid.add(texte, 1, 0);
        grid.add(new Label("Correcte:"), 0, 1); // Ajoute un label et la CheckBox au GridPane
        grid.add(correcte, 1, 1);

        dialog.getDialogPane().setContent(grid); // Définit le GridPane comme contenu de la boîte de dialogue

        // Définit le convertisseur de résultat de la boîte de dialogue pour créer une nouvelle option lorsque l'utilisateur clique sur "OK"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Option(texte.getText(), correcte.isSelected(), null);
            }
            return null;
        });

        Optional<Option> result = dialog.showAndWait(); // Affiche la boîte de dialogue et attend que l'utilisateur ferme la boîte de dialogue

        // Si l'utilisateur a cliqué sur "OK", ajoute l'option à la liste des options
        result.ifPresent(option -> {
            options.add(option);
        });
    }

    /**
     * Méthode pour créer ou modifier une question.
     * Elle obtient les valeurs des champs de la vue.
     * Elle crée une nouvelle question ou modifie la question existante en fonction de ces valeurs.
     * Elle insère la nouvelle question ou met à jour la question existante dans la base de données.
     * Ensuite, elle retourne à la liste des questions.
     */
    @FXML
    public void creerQuestion() {
        // Obtient les valeurs des champs de la vue
        String questionTypeValue = questionTypeCombo.getValue();
        String questionTextValue = questionTexteTextfield.getText();
        String sujetTextValue = sujetCombo.getValue();
        Sujet sujet = getSujetByTexte(sujetTextValue);

        try (Connection connection = DatabaseConnection.getConnection()) {// Établit une connexion à la base de données
            if (isModifying) { // Si l'utilisateur est en train de modifier une question existante
                // Prépare et exécute une requête SQL pour mettre à jour le texte de la question dans la base de données
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE question SET texte = ? WHERE id = ?");
                preparedStatement.setString(1, questionTextValue); // Définit le texte de la question comme premier paramètre de la requête
                preparedStatement.setLong(2, question.getId()); // Définit l'ID de la question comme deuxième paramètre de la requête
                preparedStatement.executeUpdate(); // Exécute la requête

                // Vérifie le type de la question pour effectuer des opérations spécifiques
                if (question instanceof QuestionMCQ) { // Si la question est une question à choix multiples
                    // Prépare et exécute une requête SQL pour supprimer les options existantes de la question dans la base de données
                    PreparedStatement deleteOptionsStatement = connection.prepareStatement("DELETE FROM options WHERE idQuestion = ?");
                    deleteOptionsStatement.setLong(1, question.getId()); // Définit l'ID de la question comme premier paramètre de la requête
                    deleteOptionsStatement.executeUpdate();

                    // Obtient les options de la question à partir du TableView des options
                    List<Option> options = new ArrayList<>(optionsTableView.getItems());
                    for (Option option : options) { // Pour chaque option
                        // Prépare et exécute une requête SQL pour insérer l'option dans la base de données
                        PreparedStatement insertOptionStatement = connection.prepareStatement("INSERT INTO options (texte, correcte, idQuestion) VALUES (?, ?, ?)");
                        insertOptionStatement.setString(1, option.getTexte()); // Définit le texte de l'option comme premier paramètre de la requête
                        insertOptionStatement.setBoolean(2, option.isCorrecte()); // Définit si l'option est correcte comme deuxième paramètre de la requête
                        insertOptionStatement.setLong(3, question.getId()); // Définit l'ID de la question comme troisième paramètre de la requête
                        insertOptionStatement.executeUpdate();
                    }
                } else if (question instanceof QuestionVraisFaux) { // Si la question est une question vrai/faux
                    // Prépare et exécute une requête SQL pour mettre à jour la réponse correcte de la question dans la base de données
                    PreparedStatement updateVFStatement = connection.prepareStatement("UPDATE questionvraisfaux SET reponseCorrecte = ? WHERE id = ?");
                    updateVFStatement.setBoolean(1, vraiRadio.isSelected()); // Définit si la réponse est "Vrai" comme premier paramètre de la requête
                    updateVFStatement.setLong(2, question.getId()); // Définit l'ID de la question comme deuxième paramètre de la requête
                    updateVFStatement.executeUpdate();
                } else if (question instanceof QuestionRemplirBlanc) { // Si la question est une question à remplir
                    // Prépare et exécute une requête SQL pour mettre à jour la réponse de la question dans la base de données
                    PreparedStatement updateRBStatement = connection.prepareStatement("UPDATE questionremplirblanc SET reponse = ? WHERE id = ?");
                    updateRBStatement.setString(1, reponseTexteTextfield.getText()); // Définit la réponse comme premier paramètre de la requête
                    updateRBStatement.setLong(2, question.getId()); // Définit l'ID de la question comme deuxième paramètre de la requête
                    updateRBStatement.executeUpdate();
                }
            } else { // Si l'utilisateur est en train de créer une nouvelle question
                // Prépare et exécute une requête SQL pour insérer la nouvelle question dans la base de données
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO question (texte, idSujet) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, questionTextValue);
                preparedStatement.setLong(2, sujet.getId());
                preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Long questionId = generatedKeys.getLong(1);

                    if ("MCQ".equals(questionTypeValue)) {
                        PreparedStatement preparedStatementMCQ = connection.prepareStatement("INSERT INTO questionmcq (id) VALUES (?)");
                        preparedStatementMCQ.setLong(1, questionId);
                        preparedStatementMCQ.executeUpdate();
                        List<Option> options = new ArrayList<>(optionsTableView.getItems());
                        for (Option option : options) {
                            PreparedStatement preparedStatementOption = connection.prepareStatement("INSERT INTO options (texte, correcte, idQuestion) VALUES (?, ?, ?)");
                            preparedStatementOption.setString(1, option.getTexte());
                            preparedStatementOption.setBoolean(2, option.isCorrecte());
                            preparedStatementOption.setLong(3, questionId);
                            preparedStatementOption.executeUpdate();
                        }
                    } else if ("Vrai/Faux".equals(questionTypeValue)) {
                        boolean reponseCorrecte = vraiRadio.isSelected();
                        PreparedStatement preparedStatementVF = connection.prepareStatement("INSERT INTO questionvraisfaux (id, reponseCorrecte) VALUES (?, ?)");
                        preparedStatementVF.setLong(1, questionId);
                        preparedStatementVF.setBoolean(2, reponseCorrecte);
                        preparedStatementVF.executeUpdate();
                    } else if ("Remplissez les blancs".equals(questionTypeValue)) {
                        String reponseTextValue = reponseTexteTextfield.getText();
                        PreparedStatement preparedStatementRB = connection.prepareStatement("INSERT INTO questionremplirblanc (id, reponse) VALUES (?, ?)");
                        preparedStatementRB.setLong(1, questionId);
                        preparedStatementRB.setString(2, reponseTextValue);
                        preparedStatementRB.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) { // Attrape et gère les exceptions SQL
            LOGGER.log(Level.SEVERE, "Erreur lors de la manipulation de la question", e);
        }
        retournerListeQuestions(); // Retourne à la liste des questions
    }

    /**
     * Méthode pour obtenir un sujet par son texte.
     * Elle exécute une requête SQL pour obtenir le sujet de la base de données.
     * La requête SQL sélectionne toutes les colonnes de la table "sujet" où le texte correspond au texte donné.
     * Si la requête retourne un résultat, elle crée un nouveau sujet avec les valeurs des colonnes et renvoie ce sujet.
     * Si une erreur SQL se produit lors de l'exécution de la requête, elle affiche un message d'erreur et renvoie null.
     *
     * @param texte Le texte du sujet à obtenir.
     * @return Le sujet correspondant au texte donné, ou null si une erreur se produit ou si la requête ne retourne pas de résultat.
     */
    private Sujet getSujetByTexte(String texte) {
        Sujet sujet = null; // Initialise le sujet à null
        try (Connection connection = DatabaseConnection.getConnection()) { // Établit une connexion à la base de données
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM sujet WHERE texte = ?"); // Prépare une requête SQL
            preparedStatement.setString(1, texte); // Définit le texte comme premier paramètre de la requête
            ResultSet resultSet = preparedStatement.executeQuery(); // Exécute la requête et obtient le résultat
            if (resultSet.next()) { // Si la requête retourne un résultat
                Long id = resultSet.getLong("id"); // Obtient l'ID du sujet
                sujet = new Sujet(id, texte, null); // Crée un nouveau sujet avec l'ID et le texte
            }
        } catch (SQLException e) { // Si une erreur SQL se produit lors de l'exécution de la requête
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du sujet", e); // Enregistre l'erreur avec le logger
        }
        return sujet; // Renvoie le sujet ou null si une erreur s'est produite ou si la requête n'a pas retourné de résultat
    }


    /**
     * Méthode pour définir la question à éditer.
     * Elle définit la question, indique que l'utilisateur est en train de modifier une question existante, et remplit les champs de la vue avec les valeurs de la question.
     * Si la question est une question à choix multiples, elle obtient les options de la question de la base de données et les ajoute au TableView des options.
     * Si la question est une question vrai/faux, elle obtient la réponse correcte de la base de données et sélectionne le RadioButton correspondant.
     * Si la question est une question à remplir, elle obtient la réponse de la base de données et la définit comme texte du TextField de la réponse.
     * Si une erreur SQL se produit lors de l'obtention des données de la question, elle affiche un message d'erreur.
     *
     * @param question La question à éditer.
     */
    public void setQuestion(Question question) {
        this.question = question;
        this.isModifying = true;

        sujetCombo.setValue(question.getSujet().getTexte());
        sujetCombo.setDisable(true);

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (question instanceof QuestionMCQ) {
                // Si la question est une question à choix multiples, on définit le type de question comme "MCQ" dans le ComboBox des types de questions
                questionTypeCombo.setValue("MCQ");

                // Prépare une requête SQL pour obtenir les options de la question à choix multiples de la base de données
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM options WHERE idQuestion = ?");
                preparedStatement.setLong(1, question.getId()); // Définit l'ID de la question comme premier paramètre de la requête
                ResultSet resultSet = preparedStatement.executeQuery(); // Exécute la requête et obtient le résultat

                // Crée une liste pour stocker les options de la question
                List<Option> options = new ArrayList<>();
                while (resultSet.next()) { // Parcourt chaque ligne du résultat
                    String texte = resultSet.getString("texte"); // Obtient le texte de l'option
                    boolean correcte = resultSet.getBoolean("correcte"); // Obtient si l'option est correcte
                    options.add(new Option(texte, correcte, null)); // Ajoute une nouvelle option à la liste des options
                }

                // Met à jour les items du TableView des options avec les options obtenues
                optionsTableView.getItems().setAll(options);
                // Rend le label des options et le bouton d'ajout d'option visibles
                optionLabel.setVisible(true);
                ajouterOptionButton.setVisible(true);
            } else if (question instanceof QuestionVraisFaux) {
                // Si la question est une question vrai/faux, on définit le type de question comme "Vrai/Faux" dans le ComboBox des types de questions
                questionTypeCombo.setValue("Vrai/Faux");

                // Prépare une requête SQL pour obtenir la réponse correcte de la question vrai/faux de la base de données
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT reponseCorrecte FROM questionvraisfaux WHERE id = ?");
                preparedStatement.setLong(1, question.getId()); // Définit l'ID de la question comme premier paramètre de la requête
                ResultSet resultSet = preparedStatement.executeQuery(); // Exécute la requête et obtient le résultat

                if (resultSet.next()) { // Si la requête retourne un résultat
                    boolean reponseCorrecte = resultSet.getBoolean("reponseCorrecte"); // Obtient la réponse correcte
                    if (reponseCorrecte) {
                        vraiRadio.setSelected(true); // Si la réponse est "Vrai", sélectionne le RadioButton "Vrai"
                    } else {
                        fauxRadio.setSelected(true); // Si la réponse est "Faux", sélectionne le RadioButton "Faux"
                    }
                }

                // Rend le label des options visible et le bouton d'ajout d'option invisible
                optionLabel.setVisible(true);
                ajouterOptionButton.setVisible(false);
            } else if (question instanceof QuestionRemplirBlanc) {
                questionTypeCombo.setValue("Remplissez les blancs");

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT reponse FROM questionremplirblanc WHERE id = ?");
                preparedStatement.setLong(1, question.getId());
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String reponse = resultSet.getString("reponse");
                    reponseTexteTextfield.setText(reponse);
                }

                reponseLabel.setVisible(true);
                optionLabel.setVisible(false);
                ajouterOptionButton.setVisible(false);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des données de la question", e);
        }

        questionTypeCombo.setDisable(true);
        questionTexteTextfield.setText(question.getTexte());
    }

    /**
     * Méthode pour retourner à la liste des questions.
     * Elle charge la vue de la liste des questions, crée une nouvelle scène avec cette vue, et définit cette scène comme scène de la fenêtre actuelle.
     * Elle définit également le titre de la fenêtre et affiche la fenêtre.
     * Si une erreur se produit lors du chargement de la vue, elle affiche un message d'erreur.
     */
    @FXML
    public void retournerListeQuestions() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/questions-view.fxml")); // Crée un FXMLLoader et charge le fichier FXML de la vue de la liste des questions
            Scene scene = new Scene(fxmlLoader.load()); // Crée une nouvelle scène avec la vue chargée
            Stage stage = (Stage) retournerButton.getScene().getWindow(); // Obtient la fenêtre actuelle à partir du bouton "Retourner"
            stage.setTitle("Liste des questions"); // Définit le titre de la fenêtre
            stage.setScene(scene); // Définit la scène de la fenêtre
            stage.show(); // Affiche la fenêtre
        } catch (
                IOException e) { // Si une erreur d'entrée/sortie se produit lors du chargement de la vue à partir du fichier FXML
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la vue des questions", e); // Enregistre l'erreur avec le logger
        }
    }

}
