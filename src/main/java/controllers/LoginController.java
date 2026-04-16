package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UserService;

public class LoginController {

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Label lblMessage;

    private final UserService userService = new UserService();

    @FXML
    public void login() {
        try {
            if (tfEmail.getText().isEmpty()) {
                lblMessage.setText("Email obligatoire");
                return;
            }

            if (pfPassword.getText().isEmpty()) {
                lblMessage.setText("Mot de passe obligatoire");
                return;
            }

            boolean ok = userService.login(tfEmail.getText(), pfPassword.getText());
            System.out.println("Résultat login = " + ok);

            if (ok) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Dashboard Utilisateur");
                stage.setScene(new Scene(root));
                stage.show();

                Stage currentStage = (Stage) tfEmail.getScene().getWindow();
                currentStage.close();
            } else {
                lblMessage.setText("Connexion échouée");
            }

        } catch (Exception e) {
            lblMessage.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signup.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Sign Up");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) tfEmail.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            lblMessage.setText("Erreur ouverture signup");
            e.printStackTrace();
        }
    }
}