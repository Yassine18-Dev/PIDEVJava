package controllers;

import app.MainApp;
import entities.Player;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.PlayerService;
import utils.AlertUtils;

import java.sql.SQLException;

public class PlayerController {

    @FXML private TableView<Player> playerTable;
    @FXML private TableColumn<Player, Integer> colId;
    @FXML private TableColumn<Player, String> colUsername;
    @FXML private TableColumn<Player, String> colEmail;
    @FXML private TableColumn<Player, String> colGame;
    @FXML private TableColumn<Player, String> colRank;
    @FXML private TableColumn<Player, String> colTeam;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> gameBox;
    @FXML private TextField rankField;
    @FXML private TextField leaguePointsField;
    @FXML private TextField teamIdField;
    @FXML private TextField searchField;

    private final PlayerService playerService = new PlayerService();

    @FXML
    public void initialize() {
        gameBox.getItems().addAll("lol", "valorant", "fifa");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colTeam.setCellValueFactory(cellData -> {
            int teamId = cellData.getValue().getTeamId();
            return new javafx.beans.property.SimpleStringProperty(teamId > 0 ? String.valueOf(teamId) : "Aucune");
        });

        loadPlayers();

        playerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                usernameField.setText(selected.getUsername());
                emailField.setText(selected.getEmail());
                passwordField.setText(selected.getPassword());
                gameBox.setValue(selected.getGame());
                rankField.setText(selected.getRank());
                leaguePointsField.setText(String.valueOf(selected.getLeaguePoints()));
                teamIdField.setText(selected.getTeamId() > 0 ? String.valueOf(selected.getTeamId()) : "");
            }
        });

        gameBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("fifa".equalsIgnoreCase(newVal)) {
                teamIdField.clear();
                teamIdField.setDisable(true);
            } else {
                teamIdField.setDisable(false);
            }
        });
    }

    private void loadPlayers() {
        try {
            playerTable.setItems(FXCollections.observableArrayList(playerService.recuperer()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les joueurs.");
        }
    }

    @FXML
    public void addPlayer() {
        try {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String game = gameBox.getValue();
            String rank = rankField.getText().trim();
            int leaguePoints = Integer.parseInt(leaguePointsField.getText().trim());
            int teamId = teamIdField.isDisabled() || teamIdField.getText().trim().isEmpty()
                    ? -1
                    : Integer.parseInt(teamIdField.getText().trim());

            if (!validatePlayerForm(username, email, game, teamId)) return;

            if (!playerService.searchByUsername(username).isEmpty()) {
                AlertUtils.showError("Erreur", "Ce pseudo existe déjà.");
                return;
            }

            for (Player p : playerService.recuperer()) {
                if (p.getEmail().equalsIgnoreCase(email)) {
                    AlertUtils.showError("Erreur", "Cet email existe déjà.");
                    return;
                }
            }

            Player player = new Player();
            player.setUsername(username);
            player.setEmail(email);
            player.setPassword(password);
            player.setGame(game);
            player.setRank(rank);
            player.setLeaguePoints(leaguePoints);
            player.setTeamId(teamId);

            playerService.ajouter(player);
            clearForm();
            loadPlayers();
            AlertUtils.showInfo("Succès", "Joueur ajouté avec succès.");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Les champs numériques doivent contenir des nombres valides.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible d'ajouter le joueur.");
        }
    }

    @FXML
    public void updatePlayer() {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Sélectionnez un joueur à modifier.");
            return;
        }

        try {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String game = gameBox.getValue();
            String rank = rankField.getText().trim();
            int leaguePoints = Integer.parseInt(leaguePointsField.getText().trim());
            int teamId = teamIdField.isDisabled() || teamIdField.getText().trim().isEmpty()
                    ? -1
                    : Integer.parseInt(teamIdField.getText().trim());

            if (!validatePlayerForm(username, email, game, teamId)) return;

            selected.setUsername(username);
            selected.setEmail(email);
            selected.setPassword(password);
            selected.setGame(game);
            selected.setRank(rank);
            selected.setLeaguePoints(leaguePoints);
            selected.setTeamId(teamId);

            playerService.modifier(selected);
            clearForm();
            loadPlayers();
            AlertUtils.showInfo("Succès", "Joueur modifié avec succès.");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Les champs numériques doivent contenir des nombres valides.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de modifier le joueur.");
        }
    }

    @FXML
    public void deletePlayer() {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Attention", "Sélectionnez un joueur à supprimer.");
            return;
        }

        try {
            playerService.supprimer(selected.getId());
            clearForm();
            loadPlayers();
            AlertUtils.showInfo("Succès", "Joueur supprimé avec succès.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de supprimer le joueur.");
        }
    }

    @FXML
    public void searchPlayer() {
        try {
            playerTable.setItems(FXCollections.observableArrayList(
                    playerService.searchByUsername(searchField.getText().trim())
            ));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de rechercher les joueurs.");
        }
    }

    @FXML
    public void filterByGame() {
        try {
            String game = gameBox.getValue();
            if (game == null || game.isEmpty()) {
                AlertUtils.showWarning("Attention", "Choisissez un jeu pour filtrer.");
                return;
            }
            playerTable.setItems(FXCollections.observableArrayList(playerService.filterByGame(game)));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de filtrer les joueurs.");
        }
    }

    @FXML
    public void showTop3() {
        try {
            playerTable.setItems(FXCollections.observableArrayList(playerService.getTop3Players()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible d'afficher le top 3.");
        }
    }

    @FXML
    public void showFreeAgents() {
        try {
            playerTable.setItems(FXCollections.observableArrayList(playerService.getFreeAgents()));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible d'afficher les joueurs sans équipe.");
        }
    }

    @FXML
    public void refreshPlayers() {
        clearForm();
        loadPlayers();
    }

    @FXML
    public void goBack() {
        MainApp.loadScene("/fxml/dashboard.fxml", "Arena MIND");
    }

    private boolean validatePlayerForm(String username, String email, String game, int teamId) {
        if (username == null || username.length() < 3) {
            AlertUtils.showError("Erreur", "Le pseudo doit contenir au moins 3 caractères.");
            return false;
        }
        if (email == null || !email.contains("@")) {
            AlertUtils.showError("Erreur", "L'email doit être valide.");
            return false;
        }
        if (game == null || game.isEmpty()) {
            AlertUtils.showError("Erreur", "Le jeu est obligatoire.");
            return false;
        }
        if ("fifa".equalsIgnoreCase(game) && teamId > 0) {
            AlertUtils.showError("Erreur", "Un joueur FIFA ne peut pas être ajouté à une équipe.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        gameBox.setValue(null);
        rankField.clear();
        leaguePointsField.clear();
        teamIdField.clear();
        searchField.clear();
        teamIdField.setDisable(false);
        playerTable.getSelectionModel().clearSelection();
    }
}   