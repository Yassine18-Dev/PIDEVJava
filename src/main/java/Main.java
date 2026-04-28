import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // On charge ton fichier FXML. 
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/AfficherPosts.fxml")));
        
        // On crée la scène (le contenu de la fenêtre)
        Scene scene = new Scene(root, 600, 400);
        
        // On configure et on affiche la fenêtre principale
        primaryStage.setTitle("Blog - Ajouter un Post");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}