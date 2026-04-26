package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.SessionManager;

public class PlayerHomeController {

    @FXML
    private Label lblCurrentUser;

    @FXML
    public void initialize() {
        User user = SessionManager.getUser();

        if (user != null) {
            lblCurrentUser.setText(
                    "Welcome " + user.getUsername()
                            + "\nEmail: " + user.getEmail()
                            + "\nRole: " + user.getRoleType()
            );
        }
    }

    @FXML
    public void logout() {
        try {
            SessionManager.logout();

            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));

            Stage stage = (Stage) lblCurrentUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}