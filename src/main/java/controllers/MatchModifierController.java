package controllers;

import entities.Match;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import services.MatchService;

import java.time.LocalDate;

public class MatchModifierController {

    @FXML private TextField tfEquipe1;
    @FXML private TextField tfEquipe2;
    @FXML private DatePicker dpDateMatch;
    @FXML private TextField tfScore;
    @FXML private TextField tfTournoiId;

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

        tfEquipe1.setText(match.getEquipe1());
        tfEquipe2.setText(match.getEquipe2());
        tfScore.setText(match.getScore());
        tfTournoiId.setText(String.valueOf(match.getTournoiId()));

        try {
            dpDateMatch.setValue(LocalDate.parse(match.getDateMatch()));
        } catch (Exception e) {
            dpDateMatch.setValue(null);
        }
    }

    @FXML
    void modifierMatch(ActionEvent event) {
        if (match == null) {
            showAlert("Erreur", "Aucun match sélectionné.");
            return;
        }

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
            match.setEquipe1(equipe1);
            match.setEquipe2(equipe2);
            match.setDateMatch(dpDateMatch.getValue().toString());
            match.setScore(score);
            match.setTournoiId(Integer.parseInt(tournoiIdText));

            service.modifierMatch(match);

            showAlert("Succès", "Match modifié avec succès.");

            if (onDataChanged != null) {
                onDataChanged.run();
            }

            retour(event);

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void retour(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
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