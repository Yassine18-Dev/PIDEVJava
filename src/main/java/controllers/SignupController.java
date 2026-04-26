package controllers;

import entities.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;
import utils.PasswordUtils;

import java.sql.Timestamp;

public class SignupController {

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private PasswordField pfConfirmPassword;

    @FXML
    private ComboBox<String> cbRoleType;

    @FXML
    private ComboBox<String> cbStatus;

    @FXML
    private TextArea taBio;

    @FXML
    private TextField tfFavoriteGame;

    @FXML
    private Label lblMessage;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        cbRoleType.setItems(FXCollections.observableArrayList(
                "PLAYER", "CAPTAIN"
        ));

        cbStatus.setItems(FXCollections.observableArrayList(
                "ACTIVE"
        ));
    }

    @FXML
    public void signup() {
        try {
            String username = tfUsername.getText().trim();
            String email = tfEmail.getText().trim();
            String password = pfPassword.getText().trim();
            String confirmPassword = pfConfirmPassword.getText().trim();
            String role = cbRoleType.getValue();
            String status = cbStatus.getValue();
            String bio = taBio.getText().trim();
            String favoriteGame = tfFavoriteGame.getText().trim();

            if (username.isEmpty()) {
                lblMessage.setText("Username obligatoire");
                showAlert("Erreur", "Username obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (username.length() < 3) {
                lblMessage.setText("Username trop court");
                showAlert("Erreur", "Username doit contenir au moins 3 caractères", Alert.AlertType.ERROR);
                return;
            }

            if (email.isEmpty()) {
                lblMessage.setText("Email obligatoire");
                showAlert("Erreur", "Email obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (!email.contains("@")) {
                lblMessage.setText("Format email invalide");
                showAlert("Erreur", "Format email invalide", Alert.AlertType.ERROR);
                return;
            }

            if (password.isEmpty()) {
                lblMessage.setText("Mot de passe obligatoire");
                showAlert("Erreur", "Mot de passe obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (password.length() < 4) {
                lblMessage.setText("Mot de passe trop court");
                showAlert("Erreur", "Mot de passe min 4 caractères", Alert.AlertType.ERROR);
                return;
            }

            if (confirmPassword.isEmpty()) {
                lblMessage.setText("Confirmation mot de passe obligatoire");
                showAlert("Erreur", "Confirmation mot de passe obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (!password.equals(confirmPassword)) {
                lblMessage.setText("Les mots de passe ne correspondent pas");
                showAlert("Erreur", "Les mots de passe ne correspondent pas", Alert.AlertType.ERROR);
                return;
            }

            if (role == null) {
                lblMessage.setText("Veuillez sélectionner un rôle");
                showAlert("Erreur", "Veuillez sélectionner un rôle", Alert.AlertType.ERROR);
                return;
            }

            if (status == null) {
                lblMessage.setText("Veuillez sélectionner un status");
                showAlert("Erreur", "Veuillez sélectionner un status", Alert.AlertType.ERROR);
                return;
            }

            if (userService.emailExiste(email)) {
                lblMessage.setText("Email déjà utilisé");
                showAlert("Erreur", "Email déjà utilisé", Alert.AlertType.ERROR);
                return;
            }

            if (userService.usernameExiste(username)) {
                lblMessage.setText("Username déjà utilisé");
                showAlert("Erreur", "Username déjà utilisé", Alert.AlertType.ERROR);
                return;
            }

            String hashedPassword = PasswordUtils.hashPassword(password);

            User user = new User(
                    email,
                    hashedPassword,
                    username,
                    role,
                    status,
                    bio,
                    favoriteGame,
                    new Timestamp(System.currentTimeMillis())
            );

            userService.ajouter(user);

            lblMessage.setStyle("-fx-text-fill: #10b981;");
            lblMessage.setText("Compte créé avec succès ✅");
            showAlert("Succès", "Compte créé avec succès ✅", Alert.AlertType.INFORMATION);

            clearFields();

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: #f87171;");
            lblMessage.setText("Erreur : " + e.getMessage());
            showAlert("Erreur", "Problème : " + e.getMessage(), Alert.AlertType.ERROR);
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
            lblMessage.setText("Erreur ouverture login");
            showAlert("Erreur", "Impossible d’ouvrir login", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tfUsername.clear();
        tfEmail.clear();
        pfPassword.clear();
        pfConfirmPassword.clear();
        cbRoleType.setValue(null);
        cbStatus.setValue(null);
        taBio.clear();
        tfFavoriteGame.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}