package controllers;

import components.RadarChart;
import entities.Invitation;
import entities.Player;
import entities.Team;
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
import services.InvitationService;
import services.PlayerService;
import services.TeamService;
import utils.AlertUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.function.ToIntFunction;

public class TeamViewController {

    @FXML private Label     modeLabel;
    @FXML private Label     teamNameLabel, teamGameLabel, teamRosterLabel;
    @FXML private Label     powerScoreLabel;
    @FXML private StackPane teamRadarContainer;
    @FXML private Label     balanceScoreLabel, weakestLabel, recommendationLabel;
    @FXML private VBox      rosterBox;
    @FXML private Button    inviteBtn, invitationsListBtn;

    private final TeamService       teamService    = new TeamService();
    private final PlayerService     playerService  = new PlayerService();
    private final InvitationService invService     = new InvitationService();

    private Team       team;
    private Player     viewer;
    private boolean    isCaptain;
    private RadarChart teamRadar;
    private List<Player> roster;

    @FXML
    public void initialize() {
        teamRadar = new RadarChart(320, 320);
        teamRadar.setStrokeColor(Color.web("#7a5af8"));
        teamRadarContainer.getChildren().add(teamRadar);
        teamRadarContainer.setAlignment(Pos.CENTER);
    }

    public void initWithTeam(Team t, Player viewer) {
        this.team      = t;
        this.viewer    = viewer;
        this.isCaptain = (t.getCaptainId() == viewer.getId());
        applyPermissions();
        renderAll();
    }

    private void applyPermissions() {
        if (isCaptain) {
            modeLabel.setText("⭐ MODE CAPITAINE — Tu peux gérer ton équipe");
            modeLabel.getStyleClass().setAll("mode-banner", "mode-captain");
        } else {
            modeLabel.setText("👁 MODE LECTURE — Seul le capitaine peut modifier l'équipe");
            modeLabel.getStyleClass().setAll("mode-banner", "mode-readonly");
        }
        inviteBtn.setVisible(isCaptain);          inviteBtn.setManaged(isCaptain);
        invitationsListBtn.setVisible(isCaptain); invitationsListBtn.setManaged(isCaptain);
    }

    private void renderAll() {
        teamNameLabel.setText(team.getName());
        teamGameLabel.setText(team.getGame() != null ? team.getGame().toUpperCase() : "—");
        teamRosterLabel.setText(team.getCurrentPlayers() + "/" + team.getMaxPlayers() + " JOUEURS");
        powerScoreLabel.setText(String.valueOf(team.getPowerScore()));
        renderRoster();
        renderTeamRadar();
    }

    private void renderRoster() {
        rosterBox.getChildren().clear();
        try {
            roster = playerService.findByTeam(team.getId());
            if (roster.isEmpty()) {
                Label l = new Label("Aucun joueur dans l'équipe.");
                l.getStyleClass().add("subtitle-label");
                rosterBox.getChildren().add(l);
                return;
            }
            for (Player p : roster) {
                rosterBox.getChildren().add(buildPlayerCard(p));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HBox buildPlayerCard(Player p) {
        boolean isCap = (team.getCaptainId() == p.getId());

        Circle avatar = new Circle(28, Color.web(isCap ? "#f1c40f" : "#1fb3d2"));
        Label initial = new Label(p.getUsername().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        StackPane avatarPane = new StackPane(avatar, initial);
        avatarPane.setMinSize(56, 56);

        Label name = new Label(p.getUsername() + (isCap ? " ⭐" : ""));
        name.getStyleClass().add("player-card-name");

        Label meta = new Label(p.getRank() + " • " + p.getLeaguePoints() + " LP");
        meta.getStyleClass().add("player-card-meta");

        VBox info = new VBox(2, name, meta);

        // Mini stats
        HBox stats = new HBox(15);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
                buildMiniStat("WR",    String.format("%.0f%%", p.getWinrate())),
                buildMiniStat("KDA",   String.format("%.1f",   p.getKda())),
                buildMiniStat("MVP",   String.valueOf(p.getMvpCount()))
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(15, avatarPane, info, spacer, stats);
        row.setAlignment(Pos.CENTER_LEFT);

        // Bouton retirer (capitaine seulement, pas sur soi-même)
        if (isCaptain && p.getId() != viewer.getId()) {
            Button removeBtn = new Button("✕");
            removeBtn.getStyleClass().add("danger-button");
            removeBtn.setStyle("-fx-pref-width: 36; -fx-pref-height: 36; -fx-background-radius: 50;");
            removeBtn.setOnAction(e -> handleRemovePlayer(p));
            row.getChildren().add(removeBtn);
        }

        row.getStyleClass().add(isCap ? "player-card-captain" : "player-card");
        return row;
    }

    private VBox buildMiniStat(String label, String value) {
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #1fb3d2; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label l = new Label(label);
        l.getStyleClass().add("score-label");
        VBox box = new VBox(0, v, l);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void renderTeamRadar() {
        try {
            if (roster == null || roster.isEmpty()) {
                balanceScoreLabel.setText("—");
                weakestLabel.setText("—");
                recommendationLabel.setText("Pas de joueurs dans l'équipe.");
                return;
            }

            Map<String, Double> avgs = new LinkedHashMap<>();
            avgs.put("Vision",        avg(roster, Player::getVision));
            avgs.put("Tir",           avg(roster, Player::getShooting));
            avgs.put("Réflexes",      avg(roster, Player::getReflex));
            avgs.put("Teamplay",      avg(roster, Player::getTeamplay));
            avgs.put("Communication", avg(roster, Player::getCommunication));

            teamRadar.setData(avgs);

            double balance = Math.max(0, 100 - stdDev(avgs.values()));
            String weakest = "—";
            double weakestValue = Double.MAX_VALUE;
            for (var e : avgs.entrySet()) {
                if (e.getValue() < weakestValue) {
                    weakestValue = e.getValue();
                    weakest      = e.getKey();
                }
            }

            balanceScoreLabel.setText(String.format("%.0f/100", balance));
            weakestLabel.setText(weakest);

            if (isCaptain) {
                recommendationLabel.setText(String.format(
                        "💡 Suggestion : recrute un joueur fort en %s (score actuel : %.0f/100). " +
                                "Équilibre global : %.0f/100.", weakest, weakestValue, balance));
            } else {
                recommendationLabel.setText(String.format(
                        "Équilibre global de l'équipe : %.0f/100.", balance));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private double avg(List<Player> list, ToIntFunction<Player> getter) {
        return list.stream().mapToInt(getter).average().orElse(0);
    }

    private double stdDev(java.util.Collection<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double var  = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        return Math.sqrt(var);
    }

    @FXML
    private void invitePlayer() {
        if (!isCaptain) return;
        if (team.getCurrentPlayers() >= team.getMaxPlayers()) {
            AlertUtils.showError("Équipe complète", "Plus de place dans l'équipe.");
            return;
        }
        try {
            List<Player> free = playerService.findFreePlayers(team.getGame());
            if (free.isEmpty()) {
                AlertUtils.showInfo("Personne", "Aucun joueur libre pour " + team.getGame() + ".");
                return;
            }
            ChoiceDialog<Player> dlg = new ChoiceDialog<>(free.get(0), free);
            dlg.setTitle("Inviter un joueur");
            dlg.setHeaderText("Choisis un joueur libre :");
            dlg.setContentText(null);
            Optional<Player> sel = dlg.showAndWait();
            if (sel.isEmpty()) return;

            TextInputDialog msgDlg = new TextInputDialog("Rejoins-nous !");
            msgDlg.setTitle("Message"); msgDlg.setHeaderText("Message (optionnel) :");
            String msg = msgDlg.showAndWait().orElse("");

            invService.sendInvitation(team, sel.get(), viewer.getId(), msg);
            AlertUtils.showInfo("Invitation envoyée",
                    "✉ Invitation envoyée à " + sel.get().getUsername() + ".");
        } catch (IllegalStateException ise) {
            AlertUtils.showError("Refusé", ise.getMessage());
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    private void handleRemovePlayer(Player target) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Retirer " + target.getUsername() + " de l'équipe ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    playerService.setTeam(target.getId(), null);
                    teamService.decrementCurrentPlayers(team.getId());
                    team.setCurrentPlayers(team.getCurrentPlayers() - 1);
                    renderAll();
                } catch (Exception ex) {
                    AlertUtils.showError("Erreur", ex.getMessage());
                }
            }
        });
    }

    @FXML
    private void viewSentInvitations() {
        if (!isCaptain) return;
        try {
            List<Invitation> invs = invService.findInvitationsForTeam(team.getId());
            if (invs.isEmpty()) {
                AlertUtils.showInfo("Aucune invitation", "Tu n'as envoyé aucune invitation.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (Invitation i : invs) {
                String icon = switch (i.getStatus()) {
                    case PENDING  -> "⏳";
                    case ACCEPTED -> "✅";
                    case REFUSED  -> "❌";
                };
                sb.append(icon).append(" ").append(i.getPlayerName()).append(" — ")
                        .append(i.getStatus()).append(" (").append(i.getSentAt()).append(")\n");
            }
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Invitations envoyées"); a.setHeaderText("Historique");
            TextArea ta = new TextArea(sb.toString()); ta.setEditable(false);
            ta.setPrefSize(500, 300);
            a.getDialogPane().setContent(ta);
            a.showAndWait();
        } catch (Exception e) { AlertUtils.showError("Erreur", e.getMessage()); }
    }

    @FXML
    private void backToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_profile.fxml"));
            Parent root = loader.load();
            PlayerProfileController ctrl = loader.getController();
            Player fresh = playerService.getById(viewer.getId());
            ctrl.initWithPlayer(fresh != null ? fresh : viewer);
            ((Stage) modeLabel.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openInvitations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invitations_view.fxml"));
            Parent root = loader.load();
            InvitationsViewController ctrl = loader.getController();
            ctrl.initWithPlayer(viewer);
            ((Stage) modeLabel.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
    }
}