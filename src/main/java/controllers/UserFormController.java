package controllers;

import entities.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;
import utils.PasswordUtils;

import java.sql.Timestamp;

public class UserFormController {

    @FXML
    private Label lblTitle;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfUsername;

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
    private Button btnSave;

    private final UserService userService = new UserService();

    private String mode;
    private User currentUser;
    private UserController parentController;

    @FXML
    public void initialize() {
        cbRoleType.setItems(FXCollections.observableArrayList("PLAYER", "CAPTAIN", "ADMIN"));
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "SUSPENDED", "BANNED"));
    }

    public void initData(String mode, User user, UserController parentController) {
        this.mode = mode;
        this.currentUser = user;
        this.parentController = parentController;

        if ("ADD".equals(mode)) {
            lblTitle.setText("➕ Ajouter un utilisateur");
            btnSave.setText("Ajouter");
        } else if ("EDIT".equals(mode)) {
            lblTitle.setText("✏ Modifier un utilisateur");
            btnSave.setText("Modifier");
            fillFields(user);
        } else if ("VIEW".equals(mode)) {
            lblTitle.setText("👁 Consulter un utilisateur");
            btnSave.setVisible(false);
            fillFields(user);
            disableFields();
        }
    }

    private void fillFields(User user) {
        tfEmail.setText(user.getEmail());
        tfUsername.setText(user.getUsername());
        pfPassword.setText("");
        cbRoleType.setValue(user.getRoleType());
        cbStatus.setValue(user.getStatus());
        taBio.setText(user.getBio());
        tfFavoriteGame.setText(user.getFavoriteGame());
    }

    private void disableFields() {
        tfEmail.setDisable(true);
        tfUsername.setDisable(true);
        pfPassword.setDisable(true);
        cbRoleType.setDisable(true);
        cbStatus.setDisable(true);
        taBio.setDisable(true);
        tfFavoriteGame.setDisable(true);
    }

    @FXML
    public void saveUser() {
        try {
            String email = tfEmail.getText().trim();
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText().trim();
            String role = cbRoleType.getValue();
            String status = cbStatus.getValue();
            String bio = taBio.getText().trim();
            String favoriteGame = tfFavoriteGame.getText().trim();

            if (email.isEmpty()) {
                showMessage("Email obligatoire");
                return;
            }

            if (!email.contains("@")) {
                showMessage("Format email invalide");
                return;
            }

            if (username.isEmpty()) {
                showMessage("Username obligatoire");
                return;
            }

            if (username.length() < 3) {
                showMessage("Username trop court");
                return;
            }

            if (role == null) {
                showMessage("Veuillez sélectionner un rôle");
                return;
            }

            if (status == null) {
                showMessage("Veuillez sélectionner un status");
                return;
            }

            if ("ADD".equals(mode)) {
                if (password.isEmpty()) {
                    showMessage("Mot de passe obligatoire");
                    return;
                }

                if (password.length() < 4) {
                    showMessage("Mot de passe min 4 caractères");
                    return;
                }

                if (userService.emailExiste(email)) {
                    showMessage("Email déjà utilisé");
                    return;
                }

                if (userService.usernameExiste(username)) {
                    showMessage("Username déjà utilisé");
                    return;
                }

                User user = new User(
                        email,
                        PasswordUtils.hashPassword(password),
                        username,
                        role,
                        status,
                        bio,
                        favoriteGame,
                        new Timestamp(System.currentTimeMillis())
                );

                userService.ajouter(user);
                showAlert("Succès", "Utilisateur ajouté avec succès", Alert.AlertType.INFORMATION);
            }

            if ("EDIT".equals(mode)) {
                if (!email.equals(currentUser.getEmail()) && userService.emailExiste(email)) {
                    showMessage("Email déjà utilisé");
                    return;
                }

                if (!username.equals(currentUser.getUsername()) && userService.usernameExiste(username)) {
                    showMessage("Username déjà utilisé");
                    return;
                }

                currentUser.setEmail(email);
                currentUser.setUsername(username);
                currentUser.setRoleType(role);
                currentUser.setStatus(status);
                currentUser.setBio(bio);
                currentUser.setFavoriteGame(favoriteGame);

                if (!password.isEmpty()) {
                    currentUser.setPassword(PasswordUtils.hashPassword(password));
                }

                userService.modifier(currentUser);
                showAlert("Succès", "Utilisateur modifié avec succès", Alert.AlertType.INFORMATION);
            }

            parentController.refreshAfterForm();
            closeWindow();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) tfEmail.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}