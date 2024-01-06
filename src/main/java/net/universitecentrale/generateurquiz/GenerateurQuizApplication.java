package net.universitecentrale.generateurquiz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class GenerateurQuizApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GenerateurQuizApplication.class.getResource("view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Image icon = new Image("quiz.png");
        stage.getIcons().add(icon);
        stage.setTitle("Générateur de Quiz");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}