package controllers;

import components.SpiderChart;
import entities.Player;
import entities.Team;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.InvitationService;
import services.PlayerService;
import services.RankAverageService;
import services.RankAverageService.RankComparison;
import services.TeamService;
import utils.AlertUtils;
import utils.Session;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public class PlayerProfileController {

    @FXML private ImageView avatarView;
    @FXML private TextField usernameField, emailField;
    @FXML private Label     gameBadge, rankBadge, captainBadge;
    @FXML private Label     skillScoreLabel, aboveAvgLabel;
    @FXML private StackPane radarContainer;
    @FXML private VBox      comparisonBox, teamBox;
    @FXML private Button    teamActionBtn, invitationsBtn;

    private final PlayerService      playerService = new PlayerService();
    private final TeamService        teamService   = new TeamService();
    private final InvitationService  invService    = new InvitationService();
    private final RankAverageService rankSvc       = new RankAverageService();

    private Player      player;
    private Team        currentTeam;
    private SpiderChart radar;

    @FXML
    public void initialize() {
        radar = new SpiderChart(380, 380);
        radarContainer.getChildren().add(radar);
        radarContainer.setAlignment(Pos.CENTER);
    }

    public void initWithPlayer(Player p) {
        this.player = p;
        Session.setCurrentPlayer(p);
        renderAll();
    }

    private void renderAll() {
        if (player == null) return;
        usernameField.setText(player.getUsername());
        emailField.setText(player.getEmail());
        gameBadge.setText(player.getGame() != null ? player.getGame().toUpperCase() : "—");
        rankBadge.setText(player.getRank() != null ? player.getRank().toUpperCase() : "UNRANKED");

        int score = player.getLeaguePoints() + (int) (player.getWinrate() * 20);
        skillScoreLabel.setText(String.valueOf(score));

        renderAvatar();
        renderRadar();
        renderComparison();
        renderTeamCard();
        renderInvitationBadge();
    }

    private void renderAvatar() {
        try {
            String path = player.getAvatar();
            if (path != null && new File(path).exists()) {
                avatarView.setImage(new Image(new File(path).toURI().toString(), 100, 100, true, true));
            } else {
                avatarView.setImage(null);
            }
        } catch (Exception e) {
            avatarView.setImage(null);
        }
    }

    /** Spider chart 5 axes avec highlight des points > 80 (jaunes). */
    private void renderRadar() {
        int[] values = new int[]{
                player.getVision(),
                player.getCommunication(),
                player.getTeamplay(),
                player.getReflex(),
                player.getShooting()
        };
        radar.setValues(values);
    }

    /** VS Rank Average — comparaison LP du joueur à la moyenne du même rank. */
    private void renderComparison() {
        comparisonBox.getChildren().clear();
        try {
            RankComparison cmp = rankSvc.compareToRankAverage(player);

            // Header
            Label title = new Label("LEAGUE POINTS vs RANK AVERAGE");
            title.getStyleClass().add("comp-name");

            Label sub = new Label(String.format(
                    "Toi : %d LP   |   Moyenne %s : %.0f LP   |   Basé sur %d joueurs",
                    cmp.playerLP(), player.getRank(), cmp.averageLP(), cmp.playersInRank()));
            sub.getStyleClass().add("subtitle-label");
            sub.setWrapText(true);

            // Barre de progression colorée selon le résultat
            ProgressBar bar = new ProgressBar();
            double maxRef = Math.max(cmp.averageLP() * 2, 1);
            bar.setProgress(Math.min(1.0, cmp.playerLP() / maxRef));
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setStyle("-fx-accent: " + cmp.color() + ";");

            // Label résultat
            Label diffLabel = new Label(cmp.label());
            diffLabel.setStyle("-fx-text-fill: " + cmp.color() + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            VBox box = new VBox(6, title, sub, bar, diffLabel);
            comparisonBox.getChildren().add(box);

            // Header global
            if (cmp.playersInRank() > 0) {
                aboveAvgLabel.setText(String.format("%+.0f%% %s AVERAGE",
                        cmp.diffPercent(), cmp.isAbove() ? "ABOVE" : "BELOW"));
                aboveAvgLabel.setStyle("-fx-text-fill: " + cmp.color() + ";");
            } else {
                aboveAvgLabel.setText("");
            }

            // Bonus : rappel des compétences clés
            VBox skills = new VBox(8);
            skills.getChildren().add(buildSkillLine("🎯 Vision",        player.getVision()));
            skills.getChildren().add(buildSkillLine("📢 Communication", player.getCommunication()));
            skills.getChildren().add(buildSkillLine("🤝 Teamplay",      player.getTeamplay()));
            skills.getChildren().add(buildSkillLine("⚡ Réflexes",      player.getReflex()));
            skills.getChildren().add(buildSkillLine("🏹 Tir",           player.getShooting()));

            Label skillsTitle = new Label("DÉTAIL DES COMPÉTENCES");
            skillsTitle.getStyleClass().add("comp-name");
            skillsTitle.setStyle("-fx-padding: 18 0 0 0;");

            comparisonBox.getChildren().addAll(skillsTitle, skills);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buildSkillLine(String label, int value) {
        Label name = new Label(label);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        name.setMinWidth(140);

        ProgressBar bar = new ProgressBar(value / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);

        if (value > 80) bar.getStyleClass().add("bar-positive");
        else if (value < 50) bar.getStyleClass().add("bar-negative");

        Label val = new Label(value + "/100");
        val.setStyle((value > 80 ? "-fx-text-fill: #f1c40f;" : "-fx-text-fill: #b9c4d0;")
                + " -fx-font-weight: bold; -fx-font-size: 12px;");
        val.setMinWidth(55);

        HBox row = new HBox(10, name, bar, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void renderTeamCard() {
        teamBox.getChildren().clear();
        captainBadge.setVisible(false);
        captainBadge.setManaged(false);

        try {
            if (player.getTeamId() <= 0) {
                Label l = new Label("⚠ Tu n'es dans aucune équipe");
                l.getStyleClass().add("subtitle-label");
                Label hint = new Label("Reçois une invitation pour en rejoindre une.");
                hint.getStyleClass().add("subtitle-label");
                teamBox.getChildren().addAll(l, hint);
                teamActionBtn.setText("VOIR LES INVITATIONS");
                teamActionBtn.setOnAction(e -> openInvitations());
                currentTeam = null;
                return;
            }

            currentTeam = teamService.getById(player.getTeamId());
            if (currentTeam == null) return;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            StackPane logo = new StackPane(new Circle(35, Color.web("#1b2940")));
            logo.setMaxSize(70, 70);
            VBox info = new VBox(4);
            Label name = new Label(currentTeam.getName());
            name.getStyleClass().add("title-label");
            Label sub = new Label(currentTeam.getCurrentPlayers() + "/" + currentTeam.getMaxPlayers()
                    + " joueurs • Power " + currentTeam.getPowerScore());
            sub.getStyleClass().add("subtitle-label");
            Label game = new Label("🎮 " + currentTeam.getGame().toUpperCase());
            game.getStyleClass().add("game-badge");
            HBox badges = new HBox(8, game);
            info.getChildren().addAll(name, sub, badges);
            row.getChildren().addAll(logo, info);
            teamBox.getChildren().add(row);

            boolean isCaptain = currentTeam.isCaptain(player.getId());
            captainBadge.setVisible(isCaptain);
            captainBadge.setManaged(isCaptain);
            teamActionBtn.setText(isCaptain ? "GÉRER MON ÉQUIPE" : "VOIR MON ÉQUIPE");
            teamActionBtn.setOnAction(e -> openMyTeam());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderInvitationBadge() {
        try {
            int n = invService.countPendingForPlayer(player.getId());
            invitationsBtn.setText(n > 0 ? "📩  Invitations (" + n + ")" : "📩  Invitations");
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void changeAvatar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un avatar");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(avatarView.getScene().getWindow());
        if (file == null) return;
        try {
            Path destDir = Paths.get("avatars");
            if (!Files.exists(destDir)) Files.createDirectories(destDir);
            String ext = file.getName().substring(file.getName().lastIndexOf('.'));
            String name = "avatar_" + player.getId() + "_" + UUID.randomUUID() + ext;
            Path dest = destDir.resolve(name);
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            player.setAvatar(dest.toString());
            renderAvatar();
            AlertUtils.showInfo("Avatar mis à jour", "Clique Sauvegarder pour valider.");
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void saveProfile() {
        player.setUsername(usernameField.getText());
        player.setEmail(emailField.getText());
        try {
            playerService.updateProfile(player);
            AlertUtils.showInfo("Sauvegardé", "Profil mis à jour.");
        } catch (Exception e) {
            AlertUtils.showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void openMyTeam() {
        if (currentTeam == null) {
            AlertUtils.showError("Pas d'équipe", "Tu n'as pas encore d'équipe. Consulte tes invitations.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/team_view.fxml"));
            Parent root = loader.load();
            TeamViewController ctrl = loader.getController();
            ctrl.initWithTeam(currentTeam, player);
            ((Stage) avatarView.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void openInvitations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invitations_view.fxml"));
            Parent root = loader.load();
            InvitationsViewController ctrl = loader.getController();
            ctrl.initWithPlayer(player);
            ((Stage) avatarView.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void switchUser() {
        try {
            List<Player> all = playerService.findAll();
            if (all.isEmpty()) return;
            ChoiceDialog<Player> dlg = new ChoiceDialog<>(all.get(0), all);
            dlg.setTitle("Changer d'utilisateur");
            dlg.setHeaderText("Connecte-toi comme :");
            dlg.setContentText(null);
            dlg.showAndWait().ifPresent(p -> {
                Session.setCurrentPlayer(p);
                this.player = p;
                renderAll();
            });
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }
}