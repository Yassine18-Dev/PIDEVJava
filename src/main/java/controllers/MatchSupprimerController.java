package controllers;

import entities.Match;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.MatchService;

public class MatchSupprimerController {

    @FXML private Label lblMessage;

    private Match match;
    private Runnable onClose;
    private Runnable onDataChanged;
    private final MatchService service = new MatchService();

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void setMatch(Match match) {
        this.match = match;
        lblMessage.setText("Voulez-vous vraiment supprimer le match : "
                + match.getEquipe1() + " VS " + match.getEquipe2() + " ?");
    }

    @FXML
    void supprimerMatch(ActionEvent event) {
        if (match == null) {
            showAlert("Erreur", "Aucun match sélectionné.");
            return;
        }

        try {
            service.supprimerMatch(match.getId());
            showAlert("Succès", "Match supprimé avec succès.");
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
            Parent root = FXMLLoader.load(getClass().getResource("/match.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Matchs");
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