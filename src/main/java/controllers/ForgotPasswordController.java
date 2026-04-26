package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.MailService;
import services.UserService;

import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField tfEmail;
    @FXML private TextField tfCode;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label lblMessage;

    private final UserService userService = new UserService();
    private final MailService mailService = new MailService();

    @FXML
    public void sendCode() {
        try {
            String email = tfEmail.getText().trim();

            if (email.isEmpty()) {
                showAlert("Erreur", "Email obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (!email.contains("@")) {
                showAlert("Erreur", "Format email invalide", Alert.AlertType.ERROR);
                return;
            }

            if (!userService.emailExiste(email)) {
                showAlert("Erreur", "Aucun compte avec cet email", Alert.AlertType.ERROR);
                return;
            }

            String code = generateCode();
            userService.saveResetCode(email, code);
            mailService.sendResetCode(email, code);

            lblMessage.setStyle("-fx-text-fill: #10b981;");
            lblMessage.setText("Code envoyé par email");
            showAlert("Succès", "Code envoyé par email", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void resetPassword() {
        try {
            String email = tfEmail.getText().trim();
            String code = tfCode.getText().trim();
            String newPassword = pfNewPassword.getText().trim();
            String confirmPassword = pfConfirmPassword.getText().trim();

            if (email.isEmpty() || code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Erreur", "Tous les champs sont obligatoires", Alert.AlertType.ERROR);
                return;
            }

            if (newPassword.length() < 4) {
                showAlert("Erreur", "Mot de passe minimum 4 caractères", Alert.AlertType.ERROR);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas", Alert.AlertType.ERROR);
                return;
            }

            if (!userService.verifyResetCode(email, code)) {
                showAlert("Erreur", "Code invalide ou expiré", Alert.AlertType.ERROR);
                return;
            }

            userService.updatePasswordByEmail(email, newPassword);

            showAlert("Succès", "Mot de passe modifié avec succès", Alert.AlertType.INFORMATION);
            goToLogin();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}