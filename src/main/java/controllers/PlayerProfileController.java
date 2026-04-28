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
import services.EmailConfirmationService;
import services.InvitationService;
import services.PlayerService;
import services.RankAverageService;
import services.RankAverageService.RankComparison;
import services.TeamService;
import utils.AlertUtils;
import utils.Session;
import utils.Validator;
import utils.Validator.ValidationResult;

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

    private final PlayerService             playerService = new PlayerService();
    private final TeamService               teamService   = new TeamService();
    private final InvitationService         invService    = new InvitationService();
    private final RankAverageService        rankSvc       = new RankAverageService();
    private final EmailConfirmationService  emailService  = new EmailConfirmationService();

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
        renderEmailPendingState();
    }

    /** Affiche un état visuel si email en attente de confirmation. */
    private void renderEmailPendingState() {
        if (player.hasPendingEmailChange()) {
            emailField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                    "-fx-prompt-text-fill: #9aa8b6; -fx-pref-height: 38; " +
                    "-fx-border-color: #f1c40f; -fx-border-width: 2; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8;");
            emailField.setTooltip(new Tooltip("⏳ Email en attente de confirmation : "
                    + player.getPendingEmail()));
        } else {
            emailField.setStyle("");
            emailField.setTooltip(null);
        }
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

    private void renderComparison() {
        comparisonBox.getChildren().clear();
        try {
            RankComparison cmp = rankSvc.compareToRankAverage(player);

            Label title = new Label("LEAGUE POINTS vs RANK AVERAGE");
            title.getStyleClass().add("comp-name");

            Label sub = new Label(String.format(
                    "Toi : %d LP   |   Moyenne %s : %.0f LP   |   Basé sur %d joueurs",
                    cmp.playerLP(), player.getRank(), cmp.averageLP(), cmp.playersInRank()));
            sub.getStyleClass().add("subtitle-label");
            sub.setWrapText(true);

            ProgressBar bar = new ProgressBar();
            double maxRef = Math.max(cmp.averageLP() * 2, 1);
            bar.setProgress(Math.min(1.0, cmp.playerLP() / maxRef));
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setStyle("-fx-accent: " + cmp.color() + ";");

            Label diffLabel = new Label(cmp.label());
            diffLabel.setStyle("-fx-text-fill: " + cmp.color() + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            VBox box = new VBox(6, title, sub, bar, diffLabel);
            comparisonBox.getChildren().add(box);

            if (cmp.playersInRank() > 0) {
                aboveAvgLabel.setText(String.format("%+.0f%% %s AVERAGE",
                        cmp.diffPercent(), cmp.isAbove() ? "ABOVE" : "BELOW"));
                aboveAvgLabel.setStyle("-fx-text-fill: " + cmp.color() + ";");
            } else {
                aboveAvgLabel.setText("");
            }

            // Bonus : compétences détaillées
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
                Label hint = new Label("Postule à une équipe ou attends une invitation.");
                hint.getStyleClass().add("subtitle-label");

                Button browseBtn = new Button("🔍 PARCOURIR LES ÉQUIPES");
                browseBtn.getStyleClass().add("button");
                browseBtn.setMaxWidth(Double.MAX_VALUE);
                browseBtn.setOnAction(e -> openAvailableTeams());

                Button myReqBtn = new Button("📤 MES DEMANDES");
                myReqBtn.getStyleClass().add("secondary-button");
                myReqBtn.setMaxWidth(Double.MAX_VALUE);
                myReqBtn.setOnAction(e -> openMyRequests());

                teamBox.getChildren().addAll(l, hint, browseBtn, myReqBtn);
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

    // ============================================================
    // SAUVEGARDE PROFIL AVEC VALIDATIONS + CONFIRMATION EMAIL
    // ============================================================
    @FXML
    private void saveProfile() {
        String newUsername = usernameField.getText().trim();
        String newEmail    = emailField.getText().trim();

        // ===== 1) VALIDATIONS DE BASE =====
        ValidationResult vUser = Validator.validateUsername(newUsername);
        if (!vUser.ok) {
            AlertUtils.showError("Pseudo invalide", vUser.message);
            usernameField.requestFocus();
            return;
        }

        ValidationResult vEmail = Validator.validateEmail(newEmail);
        if (!vEmail.ok) {
            AlertUtils.showError("Email invalide", vEmail.message);
            emailField.requestFocus();
            return;
        }

        try {
            // ===== 2) Vérifier unicité username =====
            if (!newUsername.equals(player.getUsername())) {
                if (playerService.isUsernameTaken(newUsername, player.getId())) {
                    AlertUtils.showError("Pseudo déjà pris",
                            "Le pseudo \"" + newUsername + "\" est déjà utilisé par un autre joueur.");
                    return;
                }
            }

            // ===== 3) Mettre à jour le username + autres champs (sauf email) =====
            player.setUsername(newUsername);
            playerService.updateProfileSafe(player);

            // ===== 4) Si l'email a changé → procédure de confirmation =====
            if (!newEmail.equalsIgnoreCase(player.getEmail())) {
                handleEmailChange(newEmail);
            } else {
                AlertUtils.showInfo("✅ Profil sauvegardé", "Tes modifications ont été enregistrées.");
                renderAll();
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur SQL", e.getMessage());
        }
    }

    private void handleEmailChange(String newEmail) {
        try {
            EmailConfirmationService.EmailChangeRequest req =
                    emailService.requestEmailChange(player.getId(), newEmail);

            // Recharger pour voir le pending_email à jour
            Player fresh = playerService.getById(player.getId());
            if (fresh != null) this.player = fresh;
            renderAll();

            showEmailConfirmationDialog(newEmail, req.gamingCode);
        } catch (IllegalStateException ise) {
            AlertUtils.showError("Email refusé", ise.getMessage());
            // Restaurer l'ancien email à l'écran
            emailField.setText(player.getEmail());
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    /** Dialog gaming pro pour confirmer le changement d'email. */
    private void showEmailConfirmationDialog(String newEmail, String gamingCode) {
        Dialog<String> dlg = new Dialog<>();
        dlg.setTitle("🎮 ARENA MIND — Confirmation Email");
        dlg.setHeaderText(null);

        Label headerEmoji = new Label("📧");
        headerEmoji.setStyle("-fx-font-size: 50px;");

        Label title = new Label("CHECK YOUR MAILBOX, AGENT");
        title.setStyle("-fx-text-fill: #1fb3d2; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Un code de confirmation a été envoyé à :");
        subtitle.setStyle("-fx-text-fill: #b9c4d0; -fx-font-size: 13px;");

        Label emailLabel = new Label(newEmail);
        emailLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label instruction = new Label("Saisis le code reçu pour valider ton nouvel email.\n" +
                "⏳ Le code expire dans 24h.");
        instruction.setStyle("-fx-text-fill: #b9c4d0; -fx-font-size: 12px;");
        instruction.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("XXXX-XXXX-XXXX");
        codeField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #5a6878; -fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-alignment: CENTER; -fx-pref-height: 50; " +
                "-fx-background-radius: 10; -fx-border-color: #1fb3d2; -fx-border-radius: 10;");
        codeField.setMaxWidth(280);

        Label devHint = new Label("💡 Mode démo : code = " + gamingCode);
        devHint.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox content = new VBox(12, headerEmoji, title, subtitle, emailLabel,
                instruction, codeField, devHint);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-padding: 20; -fx-background-color: #0d1d38; -fx-background-radius: 14;");
        content.setMinWidth(400);

        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().setStyle("-fx-background-color: #0d1d38;");

        ButtonType confirmBtn = new ButtonType("✅ CONFIRMER", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn  = new ButtonType("❌ ANNULER",   ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(confirmBtn, cancelBtn);

        dlg.getDialogPane().lookupButton(confirmBtn).setStyle(
                "-fx-background-color: #1fb3d2; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38; -fx-cursor: hand;");
        dlg.getDialogPane().lookupButton(cancelBtn).setStyle(
                "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38; -fx-cursor: hand;");

        dlg.setResultConverter(b -> b == confirmBtn ? codeField.getText() : null);

        dlg.showAndWait().ifPresentOrElse(code -> {
            if (code == null || code.isBlank()) {
                AlertUtils.showError("Code requis", "Tu dois saisir le code reçu par email.");
                return;
            }
            try {
                if (emailService.confirmEmail(player.getId(), code)) {
                    AlertUtils.showInfo("✅ Email confirmé !",
                            "Ton nouvel email a été validé avec succès.\nBienvenue, "
                                    + player.getUsername() + " !");
                    Player fresh = playerService.getById(player.getId());
                    if (fresh != null) {
                        this.player = fresh;
                        renderAll();
                    }
                }
            } catch (IllegalStateException ise) {
                AlertUtils.showError("❌ Confirmation échouée", ise.getMessage());
            } catch (Exception e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }, () -> {
            try {
                emailService.cancelChange(player.getId());
                Player fresh = playerService.getById(player.getId());
                if (fresh != null) {
                    this.player = fresh;
                    renderAll();
                }
            } catch (Exception ignored) {}
            AlertUtils.showInfo("Annulé", "Le changement d'email a été annulé.");
        });
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
    private void openAvailableTeams() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/available_teams.fxml"));
            Parent root = loader.load();
            AvailableTeamsController ctrl = loader.getController();
            ctrl.initWithPlayer(player);
            ((Stage) avatarView.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void openMyRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_requests.fxml"));
            Parent root = loader.load();
            MyRequestsController ctrl = loader.getController();
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