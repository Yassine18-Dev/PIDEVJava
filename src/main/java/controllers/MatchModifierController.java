package controllers;

import entities.Match;
import entities.Team;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.MatchService;
import services.TeamService;

import java.time.LocalDateTime;

public class MatchModifierController {

    @FXML private ComboBox<Team> cbTeam1;
    @FXML private ComboBox<Team> cbTeam2;
    @FXML private DatePicker dpDateMatch;
    @FXML private TextField tfHeureMatch;
    @FXML private TextField tfScore;
    @FXML private TextField tfTournoiId;
    @FXML private ComboBox<String> cbEtat;

    private Match match;
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

            cbEtat.setItems(FXCollections.observableArrayList(
                    "A_VENIR",
                    "EN_COURS",
                    "TERMINE"
            ));

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    public void setMatch(Match match) {
        this.match = match;

        cbTeam1.getItems().forEach(t -> {
            if (t.getId() == match.getTeam1Id()) cbTeam1.setValue(t);
        });

        cbTeam2.getItems().forEach(t -> {
            if (t.getId() == match.getTeam2Id()) cbTeam2.setValue(t);
        });

        dpDateMatch.setValue(java.time.LocalDate.parse(match.getDateMatch()));
        tfHeureMatch.setText(match.getHeureMatch() == null ? "" : match.getHeureMatch());
        tfTournoiId.setText(match.getTournoiId() > 0 ? String.valueOf(match.getTournoiId()) : "");

        String etat = match.getEtat() == null || match.getEtat().isBlank()
                ? "A_VENIR"
                : match.getEtat();

        cbEtat.setValue(etat);

        appliquerReglesEtat(etat);
    }

    private void appliquerReglesEtat(String etat) {
        if ("A_VENIR".equals(etat)) {
            tfScore.setText("0-0");
            tfScore.setDisable(true);
        } else {
            tfScore.setDisable(false);
            tfScore.setText(match.getScore() == null || match.getScore().isBlank()
                    ? "0-0"
                    : match.getScore());
        }
    }

    @FXML
    void modifierMatch(ActionEvent event) {
        if (match == null) {
            showAlert("Erreur", "Aucun match sélectionné.");
            return;
        }

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

        String etat = cbEtat.getValue();

        if (etat == null || etat.isBlank()) {
            showAlert("Attention", "Choisis l'état du match.");
            return;
        }

        LocalDateTime dateHeureMatch = LocalDateTime.parse(dpDateMatch.getValue() + "T" + heure);
        LocalDateTime maintenant = LocalDateTime.now();

        String score;

        if (dateHeureMatch.isAfter(maintenant)) {
            etat = "A_VENIR";
            score = "0-0";
        } else {
            score = tfScore.getText().trim();

            if (!score.matches("\\d+-\\d+")) {
                showAlert("Attention", "Score format : 2-1");
                return;
            }
        }

        int tournoiId = 0;

        try {
            if (!tfTournoiId.getText().trim().isEmpty()) {
                tournoiId = Integer.parseInt(tfTournoiId.getText().trim());
            }

            match.setEquipe1(team1.getName());
            match.setEquipe2(team2.getName());
            match.setTeam1Id(team1.getId());
            match.setTeam2Id(team2.getId());
            match.setDateMatch(dpDateMatch.getValue().toString());
            match.setHeureMatch(heure);
            match.setScore(score);
            match.setEtat(etat);
            match.setTournoiId(tournoiId);

            service.modifierMatch(match);

            showAlert("Succès", "Match modifié avec succès.");

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