package controllers;

import components.RadarChart;
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
import services.TeamService;
import utils.AlertUtils;
import utils.Session;

import java.io.File;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;

public class PlayerProfileController {

    @FXML private ImageView avatarView;
    @FXML private TextField usernameField, emailField;
    @FXML private Label     gameBadge, rankBadge, captainBadge;
    @FXML private Label     skillScoreLabel, aboveAvgLabel;
    @FXML private StackPane radarContainer;
    @FXML private VBox      comparisonBox, teamBox;
    @FXML private Button    teamActionBtn, invitationsBtn;

    private final PlayerService     playerService = new PlayerService();
    private final TeamService       teamService   = new TeamService();
    private final InvitationService invService    = new InvitationService();

    private Player     player;
    private Team       currentTeam;
    private RadarChart radar;

    @FXML
    public void initialize() {
        radar = new RadarChart(380, 380);
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

        int score = player.getLeaguePoints() + (int)(player.getWinrate() * 20);
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
        } catch (Exception e) { avatarView.setImage(null); }
    }

    private void renderRadar() {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Vision",        (double) player.getVision());
        data.put("Tir",           (double) player.getShooting());
        data.put("Réflexes",      (double) player.getReflex());
        data.put("Teamplay",      (double) player.getTeamplay());
        data.put("Communication", (double) player.getCommunication());
        radar.setData(data);
    }

    /** Comparaison à la moyenne des joueurs du même rank */
    private void renderComparison() {
        comparisonBox.getChildren().clear();
        try {
            Map<String, double[]> data = computeComparisonData();
            if (data.isEmpty()) {
                Label l = new Label("Pas assez de joueurs au même rank pour comparer.");
                l.getStyleClass().add("subtitle-label");
                comparisonBox.getChildren().add(l);
                aboveAvgLabel.setText("");
                return;
            }
            double avgPct = data.values().stream().mapToDouble(arr -> arr[2]).average().orElse(0);
            aboveAvgLabel.setText(String.format("%+.0f%% %s AVERAGE",
                    avgPct, avgPct >= 0 ? "ABOVE" : "BELOW"));
            aboveAvgLabel.setStyle(avgPct >= 0
                    ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
            data.forEach((stat, arr) -> comparisonBox.getChildren().add(buildComparisonRow(stat, arr)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Calcul direct (sans service séparé) : valeur joueur, moyenne rank, % diff */
    private Map<String, double[]> computeComparisonData() throws SQLException {
        Map<String, double[]> result = new LinkedHashMap<>();
        java.sql.Connection cnx = utils.Mydatabase.getInstance().getCnx();
        String sql = "SELECT AVG(winrate) AS w, AVG(kda) AS k, AVG(vision) AS v, AVG(mvp_count) AS m " +
                "FROM player WHERE `rank`=? AND game=? AND id<>?";
        try (java.sql.PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, player.getRank());
            ps.setString(2, player.getGame());
            ps.setInt(3,    player.getId());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getObject("w") != null) {
                result.put("Winrate", diffArr(player.getWinrate(),  rs.getDouble("w")));
                result.put("KDA",     diffArr(player.getKda(),      rs.getDouble("k")));
                result.put("Vision",  diffArr(player.getVision(),   rs.getDouble("v")));
                result.put("MVPs",    diffArr(player.getMvpCount(), rs.getDouble("m")));
            }
        }
        return result;
    }

    private double[] diffArr(double mine, double avg) {
        if (avg <= 0) return new double[]{mine, 0, 0};
        return new double[]{mine, avg, ((mine - avg) / avg) * 100.0};
    }

    private VBox buildComparisonRow(String stat, double[] arr) {
        double mine = arr[0], avg = arr[1], pct = arr[2];
        boolean above = pct >= 0;

        Label name  = new Label(stat); name.getStyleClass().add("comp-name");
        Label badge = new Label(String.format("%+.0f%% vs avg", pct));
        badge.getStyleClass().add(above ? "badge-positive" : "badge-negative");

        ProgressBar bar = new ProgressBar();
        double maxRef = Math.max(avg * 1.5, 1);
        bar.setProgress(Math.min(1.0, mine / maxRef));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add(above ? "bar-positive" : "bar-negative");

        Label value = new Label(String.format("Toi : %.1f   |   Moyenne : %.1f", mine, avg));
        value.getStyleClass().add("subtitle-label");

        HBox header = new HBox(10, name, badge); header.setAlignment(Pos.CENTER_LEFT);
        return new VBox(4, header, bar, value);
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
            Label sub  = new Label(currentTeam.getCurrentPlayers() + "/" + currentTeam.getMaxPlayers()
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void renderInvitationBadge() {
        try {
            int n = invService.countPendingForPlayer(player.getId());
            invitationsBtn.setText(n > 0 ? "📩  Invitations (" + n + ")" : "📩  Invitations");
        } catch (Exception ignored) {}
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
            String ext  = file.getName().substring(file.getName().lastIndexOf('.'));
            String name = "avatar_" + player.getId() + "_" + UUID.randomUUID() + ext;
            Path   dest = destDir.resolve(name);
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