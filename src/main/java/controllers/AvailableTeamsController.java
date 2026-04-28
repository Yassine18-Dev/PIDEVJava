package controllers;

import entities.Player;
import entities.Team;
import entities.TeamRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import services.PlayerService;
import services.TeamRequestService;
import services.TeamService;
import utils.AlertUtils;

import java.util.List;

public class AvailableTeamsController {

    @FXML private Label subtitleLabel;
    @FXML private VBox  teamsBox;

    private final TeamService        teamService    = new TeamService();
    private final PlayerService      playerService  = new PlayerService();
    private final TeamRequestService requestService = new TeamRequestService();

    private Player player;

    public void initWithPlayer(Player p) {
        this.player = p;
        loadTeams();
    }

    private void loadTeams() {
        teamsBox.getChildren().clear();

        if (player.getTeamId() > 0) {
            subtitleLabel.setText("⚠ Tu es déjà dans une équipe. Quitte-la avant de postuler ailleurs.");
            return;
        }

        try {
            List<Team> teams = teamService.findRecruitingTeams(player.getGame(), player.getId());
            if (teams.isEmpty()) {
                subtitleLabel.setText("Aucune équipe ne recrute en ce moment pour " + player.getGame() + ".");
                return;
            }

            subtitleLabel.setText(teams.size() + " équipe(s) recrutent en " + player.getGame().toUpperCase()
                    + ". Choisis-en une pour faire une demande.");

            List<TeamRequest> myRequests = requestService.findRequestsByPlayer(player.getId());

            for (Team t : teams) {
                boolean alreadyRequested = myRequests.stream().anyMatch(r ->
                        r.getTeamId() == t.getId() && r.getStatus() == TeamRequest.Status.PENDING);
                teamsBox.getChildren().add(buildTeamCard(t, alreadyRequested));
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    private HBox buildTeamCard(Team team, boolean alreadyRequested) {
        Circle logo = new Circle(35, Color.web("#1fb3d2"));
        Label initial = new Label(team.getName().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 26px;");
        StackPane logoPane = new StackPane(logo, initial);
        logoPane.setMinSize(70, 70);

        Label name = new Label(team.getName());
        name.getStyleClass().add("title-label");
        Label sub = new Label(team.getCurrentPlayers() + "/" + team.getMaxPlayers()
                + " joueurs • Power " + team.getPowerScore());
        sub.getStyleClass().add("subtitle-label");
        Label game = new Label("🎮 " + team.getGame().toUpperCase());
        game.getStyleClass().add("game-badge");
        HBox badges = new HBox(8, game);
        VBox info = new VBox(4, name, sub, badges);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int free = team.getMaxPlayers() - team.getCurrentPlayers();
        Label slotsLabel = new Label(free + " place" + (free > 1 ? "s" : "") + " libre" + (free > 1 ? "s" : ""));
        slotsLabel.getStyleClass().add(free > 0 ? "badge-positive" : "badge-negative");

        Button actionBtn;
        if (alreadyRequested) {
            actionBtn = new Button("⏳ DEMANDE ENVOYÉE");
            actionBtn.setDisable(true);
            actionBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white; " +
                    "-fx-background-radius: 14; -fx-pref-height: 40; -fx-font-weight: bold;");
        } else {
            actionBtn = new Button("✉ DEMANDER À REJOINDRE");
            actionBtn.getStyleClass().add("button");
            actionBtn.setOnAction(e -> handleRequest(team));
        }

        VBox right = new VBox(8, slotsLabel, actionBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(15, logoPane, info, spacer, right);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("player-card");
        return row;
    }

    private void handleRequest(Team team) {
        TextInputDialog dlg = new TextInputDialog("Bonjour, je souhaite rejoindre votre équipe !");
        dlg.setTitle("Demande à rejoindre");
        dlg.setHeaderText("Envoyer une demande à " + team.getName());
        dlg.setContentText("Message :");
        String msg = dlg.showAndWait().orElse(null);
        if (msg == null) return;

        try {
            requestService.sendRequest(player, team, msg);
            AlertUtils.showInfo("Demande envoyée",
                    "Ta demande a été envoyée au capitaine de " + team.getName() + " ! 🚀");
            loadTeams();
        } catch (IllegalStateException ise) {
            AlertUtils.showError("Refusé", ise.getMessage());
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void backToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_profile.fxml"));
            Parent root = loader.load();
            PlayerProfileController ctrl = loader.getController();
            Player fresh = playerService.getById(player.getId());
            ctrl.initWithPlayer(fresh != null ? fresh : player);
            ((Stage) teamsBox.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openInvitations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invitations_view.fxml"));
            Parent root = loader.load();
            InvitationsViewController ctrl = loader.getController();
            ctrl.initWithPlayer(player);
            ((Stage) teamsBox.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openMyRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_requests.fxml"));
            Parent root = loader.load();
            MyRequestsController ctrl = loader.getController();
            ctrl.initWithPlayer(player);
            ((Stage) teamsBox.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
    }
}