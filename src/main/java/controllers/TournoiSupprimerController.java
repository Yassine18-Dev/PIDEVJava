package controllers;

import entities.Tournoi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.TournoiService;

public class TournoiSupprimerController {

    @FXML private Label lblMessage;

    private Tournoi tournoi;
    private Runnable onClose;
    private Runnable onDataChanged;
    private final TournoiService service = new TournoiService();

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void setTournoi(Tournoi tournoi) {
        this.tournoi = tournoi;
        lblMessage.setText("Voulez-vous vraiment supprimer le tournoi : " + tournoi.getNom() + " ?");
    }

    @FXML
    void supprimerTournoi(ActionEvent event) {
        if (tournoi == null) {
            showAlert("Erreur", "Aucun tournoi sélectionné.");
            return;
        }

        try {
            service.supprimerTournoi(tournoi.getId());
            showAlert("Succès", "Tournoi supprimé avec succès.");
            if (onDataChanged != null) onDataChanged.run();
            retour(event);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void retour(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tournoi.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Tournois");
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}