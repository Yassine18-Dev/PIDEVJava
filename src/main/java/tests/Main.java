package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tournoi.fxml"));
        Scene scene = new Scene(loader.load(), 1350, 820);

        stage.setTitle("Gestion Tournoi");
        stage.setScene(scene);
        stage.setMinWidth(1350);
        stage.setMinHeight(820);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}