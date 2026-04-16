package controllers;

import app.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class DashboardController {

    @FXML
    public void goToTeams(ActionEvent event) {
        MainApp.loadScene("/fxml/TeamManagement.fxml", "Gestion des équipes");
    }

    @FXML
    public void goToPlayers(ActionEvent event) {
        MainApp.loadScene("/fxml/PlayerManagement.fxml", "Gestion des joueurs");
    }

    @FXML
    public void quitApp(ActionEvent event) {
        System.exit(0);
    }
}