package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/Tournoi.fxml")));
        stage.setScene(scene);
        stage.setTitle("Gestion Tournoi");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}