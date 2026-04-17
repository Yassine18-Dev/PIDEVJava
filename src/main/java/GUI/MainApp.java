package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/gui/MainLayout.fxml")
        );
        
        // Debug: vérifier si la ressource est trouvée
        URL resourceUrl = getClass().getResource("/gui/MainLayout.fxml");
        System.out.println("FXML Resource URL: " + resourceUrl);
        
        if (resourceUrl == null) {
            System.err.println("FXML file not found! Trying alternative paths...");
            // Essayer d'autres chemins possibles
            resourceUrl = getClass().getClassLoader().getResource("gui/MainLayout.fxml");
            System.out.println("Alternative FXML Resource URL: " + resourceUrl);
            
            if (resourceUrl != null) {
                loader.setLocation(resourceUrl);
            } else {
                throw new RuntimeException("FXML file not found in any location!");
            }
        }
        Scene scene = new Scene(loader.load());

        stage.setTitle("Esports Shop");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(); // ⚠️ important
    }

}