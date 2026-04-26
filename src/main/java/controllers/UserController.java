package controllers;

import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.UserService;

public class UserController {

    @FXML
    private TextField tfRecherche;

    @FXML
    private ListView<User> listUsers;

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblRoleStatus;

    @FXML
    private Label lblFavoriteGame;

    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configListView();
        actualiser();

        listUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                afficherDetails(newUser);
            } else {
                clearDetails();
            }
        });
    }

    private void configListView() {
        listUsers.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label username = new Label(user.getUsername());
                    username.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                    Label email = new Label(user.getEmail());
                    email.setStyle("-fx-text-fill: #cbd5e1;");

                    Label info = new Label(user.getRoleType() + " | " + user.getStatus() + " | " + user.getFavoriteGame());
                    info.setStyle("-fx-text-fill: #94a3b8;");

                    VBox box = new VBox(4, username, email, info);
                    box.setStyle("-fx-background-color: #111827; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #1f2937; -fx-border-radius: 10;");

                    setGraphic(box);
                }
            }
        });
    }

    private void afficherDetails(User user) {
        lblUsername.setText(user.getUsername());
        lblEmail.setText(user.getEmail());
        lblRoleStatus.setText(user.getRoleType() + " / " + user.getStatus());
        lblFavoriteGame.setText(user.getFavoriteGame() == null || user.getFavoriteGame().isEmpty() ? "-" : user.getFavoriteGame());
    }

    private void clearDetails() {
        lblUsername.setText("Aucun utilisateur sélectionné");
        lblEmail.setText("-");
        lblRoleStatus.setText("-");
        lblFavoriteGame.setText("-");
    }

    @FXML
    public void actualiser() {
        try {
            userList.setAll(userService.afficher());
            listUsers.setItems(userList);
            clearDetails();
            listUsers.getSelectionModel().clearSelection();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void rechercherUser() {
        try {
            userList.setAll(userService.rechercherParUsername(tfRecherche.getText()));
            listUsers.setItems(userList);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void ouvrirAjout() {
        ouvrirFormulaire("ADD", null);
    }

    @FXML
    public void ouvrirModification() {
        User selected = listUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur", Alert.AlertType.ERROR);
            return;
        }
        ouvrirFormulaire("EDIT", selected);
    }

    @FXML
    public void ouvrirConsultation() {
        User selected = listUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur", Alert.AlertType.ERROR);
            return;
        }
        ouvrirFormulaire("VIEW", selected);
    }

    @FXML
    public void supprimerUser() {
        try {
            User selected = listUsers.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Erreur", "Sélectionnez un utilisateur", Alert.AlertType.ERROR);
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous supprimer " + selected.getUsername() + " ?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                userService.supprimer(selected.getId());
                actualiser();
                showAlert("Succès", "Utilisateur supprimé avec succès", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ouvrirFormulaire(String mode, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_form.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.initData(mode, user, this);

            Stage stage = new Stage();
            stage.setTitle("User Form");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void refreshAfterForm() {
        actualiser();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}