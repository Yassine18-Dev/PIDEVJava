package controllers;

import entities.Match;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.MatchService;

public class MatchAjouterController {

    @FXML private TextField tfEquipe1;
    @FXML private TextField tfEquipe2;
    @FXML private DatePicker dpDateMatch;
    @FXML private TextField tfScore;
    @FXML private TextField tfTournoiId;

    private Runnable onClose;
    private Runnable onDataChanged;
    private final MatchService service = new MatchService();

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    @FXML
    void ajouterMatch(ActionEvent event) {
        String equipe1 = tfEquipe1.getText().trim();
        String equipe2 = tfEquipe2.getText().trim();
        String score = tfScore.getText().trim();
        String tournoiIdText = tfTournoiId.getText().trim();

        if (equipe1.isEmpty() || equipe2.isEmpty() || score.isEmpty() || tournoiIdText.isEmpty() || dpDateMatch.getValue() == null) {
            showAlert("Attention", "Tous les champs sont obligatoires.");
            return;
        }

        if (!tournoiIdText.matches("\\d+")) {
            showAlert("Attention", "Le Tournoi ID doit être un nombre.");
            return;
        }

        if (!score.matches("\\d+-\\d+")) {
            showAlert("Attention", "Le score doit être sous la forme : 2-1.");
            return;
        }

        if (equipe1.equalsIgnoreCase(equipe2)) {
            showAlert("Attention", "Les deux équipes doivent être différentes.");
            return;
        }

        try {
            Match match = new Match(equipe1, equipe2, dpDateMatch.getValue().toString(), score, Integer.parseInt(tournoiIdText));
            service.ajouterMatch(match);
            showAlert("Succès", "Match ajouté avec succès.");
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
        openPage(event, "/match.fxml", "Gestion des Matchs");
    }

    private void openPage(ActionEvent event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
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