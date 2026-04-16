package controllers;

import app.MainApp;
import entities.Team;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.TeamService;
import utils.AlertUtils;

import java.sql.SQLException;
import java.util.List;

public class TeamController {

    @FXML private TableView<Team> teamTable;
    @FXML private TableColumn<Team, Integer> colId;
    @FXML private TableColumn<Team, String> colName;
    @FXML private TableColumn<Team, String> colGame;
    @FXML private TableColumn<Team, String> colPlayers;
    @FXML private TableColumn<Team, Integer> colPowerScore;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> gameBox;
    @FXML private TextField maxPlayersField;
    @FXML private TextField currentPlayersField;
    @FXML private TextField powerScoreField;
    @FXML private TextField searchField;

    private final TeamService teamService = new TeamService();

    @FXML
    public void initialize() {
        gameBox.getItems().addAll("lol", "valorant");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colPlayers.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getCurrentPlayers() + "/" + cellData.getValue().getMaxPlayers()
                )
        );
        colPowerScore.setCellValueFactory(new PropertyValueFactory<>("powerScore"));

        loadTeams();

        teamTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                nameField.setText(selected.getName());
                gameBox.setValue(selected.getGame());
                maxPlayersField.setText(String.valueOf(selected.getMaxPlayers()));
                currentPlayersField.setText(String.valueOf(selected.getCurrentPlayers()));
                powerScoreField.setText(String.valueOf(selected.getPowerScore()));
            }
        });
    }

    private void loadTeams() {
        try {
            List<Team> teams = teamService.recuperer();
            teamTable.setItems(FXCollections.observableArrayList(teams));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les équipes.");
        }
    }

    @FXML
    public void addTeam() {
        try {
            String name = nameField.getText().trim();
            String game = gameBox.getValue();
            int maxPlayers = Integer.parseInt(maxPlayersField.getText().trim());
            int currentPlayers = Integer.parseInt(currentPlayersField.getText().trim());
            int powerScore = Integer.parseInt(powerScoreField.getText().trim());

            if (!validateTeamForm(name, game, maxPlayers, currentPlayers)) return;

            if (!teamService.searchByName(name).isEmpty()) {
                AlertUtils.showError("Erreur", "Ce nom d'équipe existe déjà.");
                return;
            }

            Team team = new Team();
            team.setName(name);
            team.setGame(game);
            team.setMaxPlayers(maxPlayers);
            team.setCurrentPlayers(currentPlayers);
            team.setPowerScore(powerScore);

            teamService.ajouter(team);
            clearForm();
            loadTeams();
            AlertUtils.showInfo("Succès", "Équipe ajoutée avec succès.");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Les champs numériques doivent contenir des nombres valides.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible d'ajouter l'équipe.");
        }
    }

    @FXML
    public void updateTeam() {
        Team selected = teamTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Sélectionnez une équipe à modifier.");
            return;
        }

        try {
            String name = nameField.getText().trim();
            String game = gameBox.getValue();
            int maxPlayers = Integer.parseInt(maxPlayersField.getText().trim());
            int currentPlayers = Integer.parseInt(currentPlayersField.getText().trim());
            int powerScore = Integer.parseInt(powerScoreField.getText().trim());

            if (!validateTeamForm(name, game, maxPlayers, currentPlayers)) return;

            selected.setName(name);
            selected.setGame(game);
            selected.setMaxPlayers(maxPlayers);
            selected.setCurrentPlayers(currentPlayers);
            selected.setPowerScore(powerScore);

            teamService.modifier(selected);
            clearForm();
            loadTeams();
            AlertUtils.showInfo("Succès", "Équipe modifiée avec succès.");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Les champs numériques doivent contenir des nombres valides.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de modifier l'équipe.");
        }
    }

    @FXML
    public void deleteTeam() {
        Team selected = teamTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Sélectionnez une équipe à supprimer.");
            return;
        }

        try {
            teamService.supprimer(selected.getId());
            clearForm();
            loadTeams();
            AlertUtils.showInfo("Succès", "Équipe supprimée avec succès.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de supprimer l'équipe.");
        }
    }

    @FXML
    public void searchTeam() {
        try {
            String search = searchField.getText().trim();
            teamTable.setItems(FXCollections.observableArrayList(teamService.searchByName(search)));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de rechercher les équipes.");
        }
    }

    @FXML
    public void sortByPowerScore() {
        try {
            teamTable.setItems(FXCollections.observableArrayList(teamService.sortByPowerScoreDesc()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de trier les équipes.");
        }
    }

    @FXML
    public void showRecruitingTeams() {
        try {
            teamTable.setItems(FXCollections.observableArrayList(teamService.getRecruitingTeams()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de filtrer les équipes.");
        }
    }

    @FXML
    public void refreshTeams() {
        clearForm();
        loadTeams();
    }

    @FXML
    public void goBack() {
        MainApp.loadScene("/fxml/dashboard.fxml", "Arena MIND");
    }

    private boolean validateTeamForm(String name, String game, int maxPlayers, int currentPlayers) {
        if (name == null || name.isEmpty()) {
            AlertUtils.showError("Erreur", "Le nom de l'équipe est obligatoire.");
            return false;
        }
        if (game == null || game.isEmpty()) {
            AlertUtils.showError("Erreur", "Le jeu est obligatoire.");
            return false;
        }
        if (!game.equals("lol") && !game.equals("valorant")) {
            AlertUtils.showError("Erreur", "Le jeu doit être LOL ou Valorant.");
            return false;
        }
        if (maxPlayers <= 0 || maxPlayers > 5) {
            AlertUtils.showError("Erreur", "Le nombre maximum de joueurs doit être compris entre 1 et 5.");
            return false;
        }
        if (currentPlayers < 0 || currentPlayers > maxPlayers) {
            AlertUtils.showError("Erreur", "Le nombre de joueurs actuels est invalide.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        nameField.clear();
        gameBox.setValue(null);
        maxPlayersField.clear();
        currentPlayersField.clear();
        powerScoreField.clear();
        searchField.clear();
        teamTable.getSelectionModel().clearSelection();
    }
}