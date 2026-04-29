package controllers;

import entities.Match;
import entities.Team;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.MatchService;
import services.TeamService;

public class MatchAjouterController {

    @FXML private ComboBox<Team> cbTeam1;
    @FXML private ComboBox<Team> cbTeam2;
    @FXML private DatePicker dpDateMatch;
    @FXML private TextField tfHeureMatch;
    @FXML private TextField tfScore;
    @FXML private TextField tfTournoiId;

    private Runnable onClose;
    private Runnable onDataChanged;

    private final MatchService service = new MatchService();
    private final TeamService teamService = new TeamService();

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    @FXML
    public void initialize() {
        try {
            cbTeam1.setItems(FXCollections.observableArrayList(teamService.findAll()));
            cbTeam2.setItems(FXCollections.observableArrayList(teamService.findAll()));
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void ajouterMatch(ActionEvent event) {
        Team team1 = cbTeam1.getValue();
        Team team2 = cbTeam2.getValue();

        if (team1 == null || team2 == null || dpDateMatch.getValue() == null) {
            showAlert("Attention", "Sélectionne les équipes et la date.");
            return;
        }

        if (team1.getId() == team2.getId()) {
            showAlert("Attention", "Les équipes doivent être différentes.");
            return;
        }

        String heure = tfHeureMatch.getText().trim();

        if (heure.isEmpty()) {
            showAlert("Attention", "L'heure du match est obligatoire. Exemple : 18:30");
            return;
        }

        if (!heure.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            showAlert("Attention", "Format heure invalide. Exemple correct : 18:30");
            return;
        }

        String score = tfScore.getText().trim();

        if (!score.matches("\\d+-\\d+")) {
            showAlert("Attention", "Score format : 2-1");
            return;
        }

        int tournoiId = 0;

        try {
            if (!tfTournoiId.getText().trim().isEmpty()) {
                tournoiId = Integer.parseInt(tfTournoiId.getText().trim());
            }

            Match match = new Match(
                    team1.getName(),
                    team2.getName(),
                    dpDateMatch.getValue().toString(),
                    heure,
                    score,
                    tournoiId,
                    team1.getId(),
                    team2.getId()
            );

            service.ajouterMatch(match);

            showAlert("Succès", "Match ajouté avec succès.");

            if (onDataChanged != null) onDataChanged.run();
            if (onClose != null) onClose.run();

        } catch (NumberFormatException e) {
            showAlert("Attention", "Tournoi ID doit être un nombre.");
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