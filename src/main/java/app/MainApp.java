package app;

import controllers.PlayerProfileController;
import entities.Player;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.PlayerService;
import utils.Session;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        PlayerService svc = new PlayerService();
        Player player = svc.findFirst();

        if (player == null) {
            System.err.println("⚠ Aucun joueur dans la BDD.");
            return;
        }

        Session.setCurrentPlayer(player);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_profile.fxml"));
        Parent root = loader.load();
        PlayerProfileController ctrl = loader.getController();
        ctrl.initWithPlayer(player);

        stage.setTitle("ArenaMind — " + player.getUsername());
        stage.setScene(new Scene(root, 1280, 800));
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}