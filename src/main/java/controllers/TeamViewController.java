package controllers;

import components.SpiderChart;
import entities.Invitation;
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
import services.InvitationService;
import services.PlayerService;
import services.TeamRadarService;
import services.TeamRadarService.SkillStats;
import services.TeamRadarService.TeamAnalysis;
import services.TeamRequestService;
import services.TeamService;
import utils.AlertUtils;
import utils.Validator;
import utils.Validator.ValidationResult;

import java.util.List;
import java.util.Optional;

public class TeamViewController {

    @FXML private Label     modeLabel;
    @FXML private Label     teamNameLabel, teamGameLabel, teamRosterLabel;
    @FXML private Label     powerScoreLabel;
    @FXML private StackPane teamRadarContainer;
    @FXML private Label     balanceScoreLabel, weakestLabel, recommendationLabel;
    @FXML private VBox      rosterBox;
    @FXML private Button    inviteBtn, invitationsListBtn, editTeamBtn;
    @FXML private VBox      requestsCard, requestsBox;
    @FXML private Label     requestsCountBadge;

    private final TeamService        teamService    = new TeamService();
    private final PlayerService      playerService  = new PlayerService();
    private final InvitationService  invService     = new InvitationService();
    private final TeamRadarService   teamRadarSvc   = new TeamRadarService();
    private final TeamRequestService requestService = new TeamRequestService();

    private Team         team;
    private Player       viewer;
    private boolean      isCaptain;
    private SpiderChart  teamRadar;
    private List<Player> roster;

    @FXML
    public void initialize() {
        teamRadar = new SpiderChart(320, 320);
        teamRadar.setLabels(new String[]{"Vision", "Communication", "Teamplay", "Réflexes", "Tir"});
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
        requestsCard.setVisible(isCaptain);       requestsCard.setManaged(isCaptain);
        editTeamBtn.setVisible(isCaptain);        editTeamBtn.setManaged(isCaptain);
    }

    private void renderAll() {
        teamNameLabel.setText(team.getName());
        teamGameLabel.setText(team.getGame() != null ? team.getGame().toUpperCase() : "—");
        teamRosterLabel.setText(team.getCurrentPlayers() + "/" + team.getMaxPlayers() + " JOUEURS");
        powerScoreLabel.setText(String.valueOf(team.getPowerScore()));
        renderRoster();
        renderTeamRadar();
        if (isCaptain) renderRequests();
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
            for (Player p : roster) rosterBox.getChildren().add(buildPlayerCard(p));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        HBox stats = new HBox(15);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
                buildMiniStat("WR",  String.format("%.0f%%", p.getWinrate())),
                buildMiniStat("KDA", String.format("%.1f",   p.getKda())),
                buildMiniStat("MVP", String.valueOf(p.getMvpCount()))
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(15, avatarPane, info, spacer, stats);
        row.setAlignment(Pos.CENTER_LEFT);

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
            TeamAnalysis analysis = teamRadarSvc.analyze(team);

            if (analysis.stats().isEmpty()) {
                balanceScoreLabel.setText("—");
                weakestLabel.setText("—");
                recommendationLabel.setText("Pas de joueurs dans l'équipe.");
                return;
            }

            int[] vals = new int[5];
            int i = 0;
            for (SkillStats s : analysis.stats().values()) {
                if (i >= 5) break;
                vals[i++] = (int) Math.round(s.avg());
            }
            teamRadar.setValues(vals);

            balanceScoreLabel.setText(String.format("%.0f/100", analysis.teamCohesion()));
            weakestLabel.setText(analysis.weakestSkill());

            if (isCaptain) {
                recommendationLabel.setText(analysis.recommendation());
            } else {
                recommendationLabel.setText(String.format(
                        "💪 Force de l'équipe : %s (%.0f/100). Cohésion globale : %.0f/100.",
                        analysis.strongestSkill(), analysis.strongestValue(), analysis.teamCohesion()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // DEMANDES REÇUES (capitaine)
    // ============================================================
    private void renderRequests() {
        requestsBox.getChildren().clear();
        try {
            List<TeamRequest> all = requestService.findRequestsForTeam(team.getId());
            List<TeamRequest> pending = all.stream()
                    .filter(r -> r.getStatus() == TeamRequest.Status.PENDING)
                    .toList();

            requestsCountBadge.setText(String.valueOf(pending.size()));

            if (pending.isEmpty()) {
                Label l = new Label("Aucune demande en attente.");
                l.getStyleClass().add("subtitle-label");
                requestsBox.getChildren().add(l);
                return;
            }
            for (TeamRequest r : pending) requestsBox.getChildren().add(buildRequestCard(r));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buildRequestCard(TeamRequest r) {
        Label name = new Label("👤 " + r.getPlayerName());
        name.getStyleClass().add("title-label");

        Label sub = new Label(r.getPlayerRank() + " • " + r.getPlayerLP() + " LP");
        sub.getStyleClass().add("subtitle-label");

        Label msg = new Label(r.getMessage() != null && !r.getMessage().isBlank()
                ? "💬 " + r.getMessage() : "(Aucun message)");
        msg.getStyleClass().add("invitation-message");
        msg.setWrapText(true);

        Button accept = new Button("✅ ACCEPTER");
        accept.getStyleClass().add("button");
        accept.setOnAction(e -> handleAcceptRequest(r));

        Button refuse = new Button("❌ REFUSER");
        refuse.getStyleClass().add("danger-button");
        refuse.setOnAction(e -> handleRefuseRequest(r));

        HBox actions = new HBox(10, accept, refuse);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, name, sub, msg, actions);
        card.getStyleClass().add("invitation-card-pending");
        return card;
    }

    private void handleAcceptRequest(TeamRequest r) {
        try {
            requestService.acceptRequest(r.getId(), viewer.getId());
            AlertUtils.showInfo("Accepté",
                    r.getPlayerName() + " a rejoint ton équipe ! 🎉");
            team = teamService.getById(team.getId());
            renderAll();
        } catch (IllegalStateException ise) {
            AlertUtils.showError("Refusé", ise.getMessage());
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    private void handleRefuseRequest(TeamRequest r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Refuser la demande de " + r.getPlayerName() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    requestService.refuseRequest(r.getId(), viewer.getId());
                    AlertUtils.showInfo("Refusée", "La demande a été refusée.");
                    renderRequests();
                } catch (Exception e) {
                    AlertUtils.showError("Erreur", e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // INVITATIONS (capitaine)
    // ============================================================
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
            msgDlg.setTitle("Message");
            msgDlg.setHeaderText("Message (optionnel) :");
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
            a.setTitle("Invitations envoyées");
            a.setHeaderText("Historique");
            TextArea ta = new TextArea(sb.toString());
            ta.setEditable(false);
            ta.setPrefSize(500, 300);
            a.getDialogPane().setContent(ta);
            a.showAndWait();
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    // ============================================================
    // MODIFICATION ÉQUIPE (capitaine) — VALIDATIONS
    // ============================================================
    @FXML
    private void editTeamInfo() {
        if (!isCaptain) return;

        Dialog<Team> dlg = new Dialog<>();
        dlg.setTitle("✏ Modifier l'équipe");
        dlg.setHeaderText("Modifie les informations de " + team.getName());

        TextField nameField = new TextField(team.getName());
        nameField.setPromptText("Nom de l'équipe");
        nameField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-pref-height: 38; -fx-background-radius: 8;");

        Spinner<Integer> maxPlayersSpinner = new Spinner<>(2, 10, team.getMaxPlayers());
        maxPlayersSpinner.setEditable(true);

        Label nameLabel = new Label("Nom de l'équipe :");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label maxLabel  = new Label("Nombre max de joueurs :");
        maxLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label currentInfo = new Label("Joueurs actuels : " + team.getCurrentPlayers());
        currentInfo.setStyle("-fx-text-fill: #b9c4d0; -fx-font-size: 12px;");

        Label warning = new Label("⚠ Tu ne peux pas réduire la taille en dessous du nombre actuel de joueurs.");
        warning.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 11px;");
        warning.setWrapText(true);

        VBox box = new VBox(12, nameLabel, nameField, maxLabel, maxPlayersSpinner,
                currentInfo, warning);
        box.setStyle("-fx-padding: 20; -fx-background-color: #0d1d38;");
        box.setMinWidth(400);
        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().setStyle("-fx-background-color: #0d1d38;");

        ButtonType saveBtn   = new ButtonType("💾 SAUVEGARDER", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("❌ ANNULER",      ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        dlg.getDialogPane().lookupButton(saveBtn).setStyle(
                "-fx-background-color: #1fb3d2; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38; -fx-cursor: hand;");
        dlg.getDialogPane().lookupButton(cancelBtn).setStyle(
                "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38; -fx-cursor: hand;");

        dlg.setResultConverter(b -> {
            if (b != saveBtn) return null;
            Team updated = new Team();
            updated.setId(team.getId());
            updated.setName(nameField.getText());
            updated.setMaxPlayers(maxPlayersSpinner.getValue());
            updated.setLogo(team.getLogo());
            updated.setBanner(team.getBanner());
            return updated;
        });

        dlg.showAndWait().ifPresent(this::handleSaveTeam);
    }

    private void handleSaveTeam(Team updated) {
        ValidationResult vName = Validator.validateTeamName(updated.getName());
        if (!vName.ok) {
            AlertUtils.showError("Nom invalide", vName.message);
            return;
        }

        ValidationResult vMax = Validator.validateMaxPlayers(updated.getMaxPlayers());
        if (!vMax.ok) {
            AlertUtils.showError("Taille invalide", vMax.message);
            return;
        }

        if (updated.getMaxPlayers() < team.getCurrentPlayers()) {
            AlertUtils.showError("Taille trop petite",
                    "⚠ Tu ne peux pas mettre une taille max ("
                            + updated.getMaxPlayers() + ") inférieure au nombre actuel de joueurs ("
                            + team.getCurrentPlayers() + ").\nRetire d'abord des joueurs.");
            return;
        }

        try {
            if (!updated.getName().trim().equals(team.getName())) {
                if (teamService.isTeamNameTaken(updated.getName().trim(), team.getId())) {
                    AlertUtils.showError("Nom déjà pris",
                            "Une autre équipe utilise déjà le nom \"" + updated.getName() + "\".");
                    return;
                }
            }

            updated.setName(updated.getName().trim());
            teamService.updateTeamInfo(updated);

            Team fresh = teamService.getById(team.getId());
            if (fresh != null) {
                this.team = fresh;
                renderAll();
            }

            AlertUtils.showInfo("✅ Équipe mise à jour",
                    "Les informations de l'équipe ont été enregistrées avec succès.");
        } catch (Exception e) {
            AlertUtils.showError("Erreur SQL", e.getMessage());
        }
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    private void backToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_profile.fxml"));
            Parent root = loader.load();
            PlayerProfileController ctrl = loader.getController();
            Player fresh = playerService.getById(viewer.getId());
            ctrl.initWithPlayer(fresh != null ? fresh : viewer);
            ((Stage) modeLabel.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openInvitations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invitations_view.fxml"));
            Parent root = loader.load();
            InvitationsViewController ctrl = loader.getController();
            ctrl.initWithPlayer(viewer);
            ((Stage) modeLabel.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}