package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController {

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

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

    @FXML
    public void initialize() {
        cbRoleType.setItems(FXCollections.observableArrayList(
                "PLAYER", "CAPTAIN", "ADMIN"
        ));

        cbStatus.setItems(FXCollections.observableArrayList(
                "ACTIVE", "SUSPENDED", "BANNED"
        ));
    }

    @FXML
    public void signup() {
        if (tfUsername.getText().isEmpty()) {
            lblMessage.setText("Username obligatoire");
            return;
        }

        if (tfEmail.getText().isEmpty()) {
            lblMessage.setText("Email obligatoire");
            return;
        }

        if (!tfEmail.getText().contains("@")) {
            lblMessage.setText("Format email invalide");
            return;
        }

        if (pfPassword.getText().isEmpty()) {
            lblMessage.setText("Mot de passe obligatoire");
            return;
        }

        if (cbRoleType.getValue() == null) {
            lblMessage.setText("Veuillez sélectionner un rôle");
            return;
        }

        if (cbStatus.getValue() == null) {
            lblMessage.setText("Veuillez sélectionner un status");
            return;
        }

        lblMessage.setText("Compte créé avec succès ");

        System.out.println("===== SIGN UP =====");
        System.out.println("Username : " + tfUsername.getText());
        System.out.println("Email : " + tfEmail.getText());
        System.out.println("Password : " + pfPassword.getText());
        System.out.println("Role : " + cbRoleType.getValue());
        System.out.println("Status : " + cbStatus.getValue());
        System.out.println("Bio : " + taBio.getText());
        System.out.println("Favorite Game : " + tfFavoriteGame.getText());
    }

    @FXML
    public void goToLogin() {
        System.out.println("Retour vers Login");
    }
}