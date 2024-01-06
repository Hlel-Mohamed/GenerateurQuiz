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
import net.universitecentrale.generateurquiz.HelloApplication;
import net.universitecentrale.generateurquiz.databaseConnection.DatabaseConnection;
import net.universitecentrale.generateurquiz.entity.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditQuestionController {

    @FXML
    Label titreLabel;
    @FXML
    private ComboBox<String> sujetCombo;
    @FXML
    private ComboBox<String> questionTypeCombo;
    @FXML
    private TextField questionTexteTextfield;
    @FXML
    private TextField reponseTexteTextfield;
    @FXML
    private Button ajouterOptionButton;
    @FXML
    private Button retournerButton;
    @FXML
    private TableView<Option> optionsTableView;
    @FXML
    private TableColumn<Option, String> optionTexteColumn;
    @FXML
    private TableColumn<Option, Boolean> optionCorrecteColumn;
    @FXML
    private RadioButton vraiRadio;
    @FXML
    private RadioButton fauxRadio;
    @FXML
    private Label reponseLabel;
    @FXML
    private Label optionLabel;
    private Question question;
    private boolean isModifying = false;
    private ObservableList<Option> options = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        GenererController.remplirSujet(sujetCombo);

        questionTypeCombo.getItems().addAll("MCQ", "Vrai/Faux", "Remplissez les blancs");

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


        optionsTableView.setItems(options);
        optionTexteColumn.setCellValueFactory(new PropertyValueFactory<>("texte"));
        optionCorrecteColumn.setCellValueFactory(new PropertyValueFactory<>("correcte"));

        TableColumn<Option, Void> deleteColumn = getDeleteColumn();
        optionsTableView.getColumns().add(deleteColumn);

        if (isModifying) {
            sujetCombo.setDisable(true);
            questionTypeCombo.setDisable(true);
        }

    }

    private TableColumn<Option, Void> getDeleteColumn() {
        TableColumn<Option, Void> deleteColumn = new TableColumn<>("Effacer");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("X");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    deleteButton.setOnAction(event -> {
                        Option option = getTableView().getItems().get(getIndex());
                        options.remove(option);
                    });
                    setGraphic(deleteButton);
                }
            }
        });
        return deleteColumn;
    }

    @FXML
    public void ajouterOption() {
        Dialog<Option> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Option");

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField texte = new TextField();
        texte.setPromptText("Texte");
        CheckBox correcte = new CheckBox();

        grid.add(new Label("Texte:"), 0, 0);
        grid.add(texte, 1, 0);
        grid.add(new Label("Correcte:"), 0, 1);
        grid.add(correcte, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Option(texte.getText(), correcte.isSelected(), null);
            }
            return null;
        });

        Optional<Option> result = dialog.showAndWait();

        result.ifPresent(option -> {
            options.add(option);
        });
    }

    @FXML
    public void creerQuestion() {
        String questionTypeValue = questionTypeCombo.getValue();
        String questionTextValue = questionTexteTextfield.getText();
        String sujetTextValue = sujetCombo.getValue();
        Sujet sujet = getSujetByTexte(sujetTextValue);

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (isModifying) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE question SET texte = ? WHERE id = ?");
                preparedStatement.setString(1, questionTextValue);
                preparedStatement.setLong(2, question.getId());
                preparedStatement.executeUpdate();

                if (question instanceof QuestionMCQ) {
                    PreparedStatement deleteOptionsStatement = connection.prepareStatement("DELETE FROM options WHERE idQuestion = ?");
                    deleteOptionsStatement.setLong(1, question.getId());
                    deleteOptionsStatement.executeUpdate();

                    List<Option> options = new ArrayList<>(optionsTableView.getItems());
                    for (Option option : options) {
                        PreparedStatement insertOptionStatement = connection.prepareStatement("INSERT INTO options (texte, correcte, idQuestion) VALUES (?, ?, ?)");
                        insertOptionStatement.setString(1, option.getTexte());
                        insertOptionStatement.setBoolean(2, option.isCorrecte());
                        insertOptionStatement.setLong(3, question.getId());
                        insertOptionStatement.executeUpdate();
                    }
                } else if (question instanceof QuestionVraisFaux) {
                    // Update the reponseCorrecte in the database
                    PreparedStatement updateVFStatement = connection.prepareStatement("UPDATE questionvraisfaux SET reponseCorrecte = ? WHERE id = ?");
                    updateVFStatement.setBoolean(1, vraiRadio.isSelected());
                    updateVFStatement.setLong(2, question.getId());
                    updateVFStatement.executeUpdate();
                } else if (question instanceof QuestionRemplirBlanc) {
                    // Update the reponse in the database
                    PreparedStatement updateRBStatement = connection.prepareStatement("UPDATE questionremplirblanc SET reponse = ? WHERE id = ?");
                    updateRBStatement.setString(1, reponseTexteTextfield.getText());
                    updateRBStatement.setLong(2, question.getId());
                    updateRBStatement.executeUpdate();
                }
            } else {
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
                            System.out.println(option.getTexte());
                            System.out.println(option.isCorrecte());
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

        } catch (SQLException e) {
            System.out.println("Erreur lors de la création de la question");
            e.printStackTrace();
        }
        retournerListeQuestions();
    }

    private Sujet getSujetByTexte(String texte) {
        Sujet sujet = null;
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM sujet WHERE texte = ?");
            preparedStatement.setString(1, texte);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Long id = resultSet.getLong("id");
                sujet = new Sujet(id, texte, null);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du sujet");
            e.printStackTrace();
        }
        return sujet;
    }


    public void setQuestion(Question question) {
        this.question = question;
        this.isModifying = true;

        sujetCombo.setValue(question.getSujet().getTexte());
        sujetCombo.setDisable(true);

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (question instanceof QuestionMCQ) {
                questionTypeCombo.setValue("MCQ");

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM options WHERE idQuestion = ?");
                preparedStatement.setLong(1, question.getId());
                ResultSet resultSet = preparedStatement.executeQuery();

                List<Option> options = new ArrayList<>();
                while (resultSet.next()) {
                    String texte = resultSet.getString("texte");
                    boolean correcte = resultSet.getBoolean("correcte");
                    options.add(new Option(texte, correcte, null));
                }

                optionsTableView.getItems().setAll(options);
                optionLabel.setVisible(true);
                ajouterOptionButton.setVisible(true);
            } else if (question instanceof QuestionVraisFaux) {
                questionTypeCombo.setValue("Vrai/Faux");

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT reponseCorrecte FROM questionvraisfaux WHERE id = ?");
                preparedStatement.setLong(1, question.getId());
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    boolean reponseCorrecte = resultSet.getBoolean("reponseCorrecte");
                    if (reponseCorrecte) {
                        vraiRadio.setSelected(true);
                    } else {
                        fauxRadio.setSelected(true);
                    }
                }

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
            System.out.println("Erreur lors de la récupération des données de la question");
            e.printStackTrace();
        }

        questionTypeCombo.setDisable(true);
        questionTexteTextfield.setText(question.getTexte());
    }

    @FXML
    public void retournerListeQuestions() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("view/questions-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) retournerButton.getScene().getWindow();
            stage.setTitle("Liste des questions");
            stage.setScene(scene);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de la vue des questions");
            e.printStackTrace();
        }
    }

}
