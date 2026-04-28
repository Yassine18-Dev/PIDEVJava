package controllers;

import components.SpiderChart;
import entities.Player;
import entities.Team;
import javafx.application.Platform;
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
import services.AvatarService;
import services.DataDragonService;
import services.DataDragonService.GameCharacter;
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

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerProfileController {

    @FXML private ImageView avatarView;
    @FXML private TextField usernameField, emailField;
    @FXML private Label     gameBadge, rankBadge, captainBadge;
    @FXML private Label     skillScoreLabel, aboveAvgLabel;
    @FXML private StackPane radarContainer;
    @FXML private VBox      comparisonBox, teamBox;
    @FXML private VBox      discordCardContainer, championsCardContainer;
    @FXML private Button    teamActionBtn, invitationsBtn;

    private final PlayerService             playerService = new PlayerService();
    private final TeamService               teamService   = new TeamService();
    private final InvitationService         invService    = new InvitationService();
    private final RankAverageService        rankSvc       = new RankAverageService();
    private final EmailConfirmationService  emailService  = new EmailConfirmationService();
    private final AvatarService             avatarService = new AvatarService();
    private final DataDragonService         dataDragon    = new DataDragonService();

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
        renderDiscordCard();
        renderChampionsCard();
    }

    private void renderEmailPendingState() {
        if (player.hasPendingEmailChange()) {
            emailField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                    "-fx-pref-height: 38; -fx-border-color: #f1c40f; -fx-border-width: 2; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8;");
            emailField.setTooltip(new Tooltip("⏳ Email en attente : " + player.getPendingEmail()));
        } else {
            emailField.setStyle("");
            emailField.setTooltip(null);
        }
    }

    private void renderAvatar() {
        try {
            String path = player.getAvatar();
            if (path != null && !path.isBlank()) {
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    avatarView.setImage(new Image(path, 100, 100, true, true, true));
                } else if (new File(path).exists()) {
                    avatarView.setImage(new Image(new File(path).toURI().toString(), 100, 100, true, true));
                } else {
                    avatarView.setImage(null);
                }
            } else avatarView.setImage(null);
        } catch (Exception e) { avatarView.setImage(null); }
    }

    private void renderRadar() {
        radar.setValues(new int[]{
                player.getVision(), player.getCommunication(), player.getTeamplay(),
                player.getReflex(), player.getShooting()
        });
    }

    private void renderComparison() {
        comparisonBox.getChildren().clear();
        try {
            RankComparison cmp = rankSvc.compareToRankAverage(player);
            Label title = new Label("LEAGUE POINTS vs RANK AVERAGE");
            title.getStyleClass().add("comp-name");
            Label sub = new Label(String.format(
                    "Toi : %d LP   |   Moyenne %s : %.0f LP   |   %d joueurs",
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
            } else aboveAvgLabel.setText("");

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
        } catch (Exception e) { e.printStackTrace(); }
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void renderInvitationBadge() {
        try {
            int n = invService.countPendingForPlayer(player.getId());
            invitationsBtn.setText(n > 0 ? "📩  Invitations (" + n + ")" : "📩  Invitations");
        } catch (Exception ignored) {}
    }

    // ============================================================
    // CARTE DISCORD STYLÉE
    // ============================================================
    private void renderDiscordCard() {
        discordCardContainer.getChildren().clear();

        if (!player.hasDiscordConnected()) {
            VBox card = new VBox(10);
            card.getStyleClass().add("discord-card-disconnected");
            card.setAlignment(Pos.CENTER);

            Label icon = new Label("🎮");
            icon.setStyle("-fx-font-size: 36px;");

            Label title = new Label("DISCORD NON CONNECTÉ");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label info = new Label("Connecte ton Discord pour\nrejoindre les serveurs e-sport");
            info.setStyle("-fx-text-fill: #b9c4d0; -fx-font-size: 11px;");
            info.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Button connectBtn = new Button("🔗 CONNECTER DISCORD");
            connectBtn.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 14; -fx-pref-height: 36; " +
                    "-fx-cursor: hand;");
            connectBtn.setOnAction(e -> connectDiscord());

            card.getChildren().addAll(icon, title, info, connectBtn);
            discordCardContainer.getChildren().add(card);
            return;
        }

        VBox card = new VBox(10);
        card.getStyleClass().add("discord-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = new ImageView();
        avatar.setFitWidth(60);
        avatar.setFitHeight(60);
        avatar.setPreserveRatio(true);
        try {
            String url = player.getDiscordAvatarUrl();
            if (url == null || url.isBlank())
                url = avatarService.generateFromUsername(player.getDiscordUsername());
            avatar.setImage(new Image(url, 60, 60, true, true, true));
        } catch (Exception ignored) {}

        Circle clip = new Circle(30, 30, 30);
        avatar.setClip(clip);
        StackPane avatarPane = new StackPane(avatar);
        avatarPane.setMinSize(60, 60);

        VBox info = new VBox(2);
        Label username = new Label(player.getDiscordUsername());
        username.getStyleClass().add("discord-username");

        Label tag = new Label("#" + (player.getDiscordTag() != null ? player.getDiscordTag() : "0000"));
        tag.getStyleClass().add("discord-tag");

        HBox statusRow = new HBox(6);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Circle statusDot = new Circle(5);
        Label statusLabel = new Label();
        String status = player.getDiscordStatus() != null ? player.getDiscordStatus() : "OFFLINE";
        switch (status) {
            case "ONLINE" -> {
                statusDot.setFill(Color.web("#3ba55d"));
                statusLabel.setText("● En ligne");
                statusLabel.getStyleClass().add("discord-status-online");
            }
            case "IDLE" -> {
                statusDot.setFill(Color.web("#faa81a"));
                statusLabel.setText("● Absent");
                statusLabel.getStyleClass().add("discord-status-idle");
            }
            case "DND" -> {
                statusDot.setFill(Color.web("#ed4245"));
                statusLabel.setText("● Ne pas déranger");
                statusLabel.getStyleClass().add("discord-status-dnd");
            }
            default -> {
                statusDot.setFill(Color.web("#747f8d"));
                statusLabel.setText("○ Hors ligne");
                statusLabel.getStyleClass().add("discord-status-offline");
            }
        }
        statusRow.getChildren().addAll(statusDot, statusLabel);

        info.getChildren().addAll(username, tag, statusRow);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label discordLogo = new Label("⌬");
        discordLogo.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-opacity: 0.5;");

        header.getChildren().addAll(avatarPane, info, spacer, discordLogo);
        card.getChildren().add(header);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        if (player.getDiscordServerInvite() != null && !player.getDiscordServerInvite().isBlank()) {
            Button joinBtn = new Button("🚀 REJOINDRE LE SERVEUR");
            joinBtn.getStyleClass().add("discord-button");
            joinBtn.setOnAction(e -> openUrl(player.getDiscordServerInvite()));
            actions.getChildren().add(joinBtn);
        }

        Button editBtn = new Button("⚙ MODIFIER");
        editBtn.getStyleClass().add("discord-button");
        editBtn.setOnAction(e -> connectDiscord());
        actions.getChildren().add(editBtn);

        card.getChildren().add(actions);
        discordCardContainer.getChildren().add(card);
    }

    @FXML
    private void connectDiscord() {
        Dialog<Player> dlg = new Dialog<>();
        dlg.setTitle("🎮 Connecter Discord");
        dlg.setHeaderText(null);

        Label header = new Label("⌬");
        header.setStyle("-fx-font-size: 50px; -fx-text-fill: #5865F2;");

        Label title = new Label("DISCORD CONNECT");
        title.setStyle("-fx-text-fill: #5865F2; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label("Renseigne ton profil Discord pour le partager avec ton équipe.");
        info.setStyle("-fx-text-fill: #b9c4d0; -fx-font-size: 12px;");
        info.setWrapText(true);

        TextField usernameField = new TextField(player.getDiscordUsername() != null
                ? player.getDiscordUsername() : "");
        usernameField.setPromptText("Username Discord (ex: yassine_dev)");
        usernameField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-pref-height: 38; -fx-background-radius: 8;");

        TextField tagField = new TextField(player.getDiscordTag() != null ? player.getDiscordTag() : "");
        tagField.setPromptText("1234");
        tagField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-pref-height: 38; -fx-background-radius: 8;");
        tagField.setMaxWidth(80);

        Label hash = new Label("#");
        hash.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        HBox idRow = new HBox(8, usernameField, hash, tagField);
        idRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(usernameField, Priority.ALWAYS);

        Label statusLabel = new Label("Statut :");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("ONLINE", "IDLE", "DND", "OFFLINE");
        statusCombo.setValue(player.getDiscordStatus() != null ? player.getDiscordStatus() : "ONLINE");
        statusCombo.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; -fx-pref-height: 38;");

        Label inviteLabel = new Label("Lien d'invitation serveur (optionnel) :");
        inviteLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        TextField inviteField = new TextField(player.getDiscordServerInvite() != null
                ? player.getDiscordServerInvite() : "");
        inviteField.setPromptText("https://discord.gg/xxxxx");
        inviteField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-pref-height: 38; -fx-background-radius: 8;");

        VBox box = new VBox(10, header, title, info, idRow, statusLabel, statusCombo, inviteLabel, inviteField);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-padding: 20; -fx-background-color: #0d1d38;");
        box.setMinWidth(420);

        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().setStyle("-fx-background-color: #0d1d38;");

        ButtonType saveBtn   = new ButtonType("💾 SAUVEGARDER",  ButtonBar.ButtonData.OK_DONE);
        ButtonType disconnect = new ButtonType("🔌 DÉCONNECTER", ButtonBar.ButtonData.OTHER);
        ButtonType cancelBtn = new ButtonType("❌ ANNULER",       ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, disconnect, cancelBtn);

        dlg.getDialogPane().lookupButton(saveBtn).setStyle(
                "-fx-background-color: #5865F2; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38;");
        dlg.getDialogPane().lookupButton(disconnect).setStyle(
                "-fx-background-color: #555; -fx-text-fill: white; -fx-background-radius: 14; -fx-pref-height: 38;");
        dlg.getDialogPane().lookupButton(cancelBtn).setStyle(
                "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-background-radius: 14; -fx-pref-height: 38;");

        dlg.setResultConverter(b -> {
            if (b == saveBtn) {
                Player p = new Player();
                p.setDiscordUsername(usernameField.getText().trim());
                p.setDiscordTag(tagField.getText().trim());
                p.setDiscordStatus(statusCombo.getValue());
                p.setDiscordServerInvite(inviteField.getText().trim());
                return p;
            } else if (b == disconnect) {
                Player p = new Player();
                p.setDiscordUsername(null);
                return p;
            }
            return null;
        });

        dlg.showAndWait().ifPresent(this::handleSaveDiscord);
    }

    private void handleSaveDiscord(Player input) {
        try {
            if (input.getDiscordUsername() == null) {
                playerService.updateDiscordProfile(player.getId(), null, null, null, "OFFLINE", null);
                AlertUtils.showInfo("🔌 Déconnecté", "Ton compte Discord a été retiré.");
            } else {
                String dUser = input.getDiscordUsername();
                String dTag  = input.getDiscordTag();
                if (dUser.isBlank()) {
                    AlertUtils.showError("Username vide", "Le username Discord est requis.");
                    return;
                }
                if (dUser.length() < 2 || dUser.length() > 32) {
                    AlertUtils.showError("Username invalide", "Le username doit faire entre 2 et 32 caractères.");
                    return;
                }
                if (!dTag.isBlank() && !dTag.matches("\\d{1,5}")) {
                    AlertUtils.showError("Tag invalide", "Le tag doit être numérique (ex: 1234).");
                    return;
                }
                String invite = input.getDiscordServerInvite();
                if (invite != null && !invite.isBlank() && !invite.startsWith("http")) {
                    AlertUtils.showError("Lien invalide", "Le lien doit commencer par https://");
                    return;
                }

                String avatarUrl = avatarService.generateFromUsername(dUser);

                playerService.updateDiscordProfile(
                        player.getId(),
                        dUser,
                        dTag.isBlank() ? "0000" : dTag,
                        avatarUrl,
                        input.getDiscordStatus(),
                        invite.isBlank() ? null : invite);

                AlertUtils.showInfo("✅ Discord connecté !",
                        "Ton profil " + dUser + "#" + (dTag.isBlank() ? "0000" : dTag)
                                + " est maintenant lié à ArenaMind.");
            }

            Player fresh = playerService.getById(player.getId());
            if (fresh != null) {
                this.player = fresh;
                renderAll();
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur SQL", e.getMessage());
        }
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir : " + url);
        }
    }

    // ============================================================
    // CARTE CHAMPIONS / AGENTS FAVORIS (DataDragon)
    // ============================================================
    private void renderChampionsCard() {
        championsCardContainer.getChildren().clear();

        String game = player.getGame() != null ? player.getGame().toLowerCase() : "";
        boolean supported = game.equals("lol") || game.equals("valorant");

        Label title = new Label(getCardTitle(game));
        title.getStyleClass().add("card-title");

        Label sub = new Label(supported
                ? "Sélectionne tes 3 mains préférés"
                : "⚠ Cette fonctionnalité n'est disponible que pour LoL et Valorant");
        sub.getStyleClass().add("subtitle-label");

        VBox card = new VBox(12, title, sub);
        card.getStyleClass().add("card");

        if (!supported) {
            championsCardContainer.getChildren().add(card);
            return;
        }

        List<String> favorites = player.getFavoriteChampionsList();
        HBox favRow = new HBox(12);
        favRow.setAlignment(Pos.CENTER_LEFT);

        if (favorites.isEmpty()) {
            Label noFav = new Label("Aucun " + getCharacterTerm(game) + " sélectionné.");
            noFav.getStyleClass().add("subtitle-label");
            favRow.getChildren().add(noFav);
        } else {
            new Thread(() -> {
                try {
                    List<GameCharacter> all = dataDragon.fetchByGame(game);
                    Platform.runLater(() -> {
                        favRow.getChildren().clear();
                        for (String favName : favorites) {
                            GameCharacter ch = all.stream()
                                    .filter(c -> c.name().equalsIgnoreCase(favName))
                                    .findFirst().orElse(null);
                            if (ch != null) favRow.getChildren().add(buildFavoriteTile(ch));
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }

        Button selectBtn = new Button("🎯 SÉLECTIONNER MES " + getCharacterTerm(game).toUpperCase() + "S");
        selectBtn.getStyleClass().add("button");
        selectBtn.setMaxWidth(Double.MAX_VALUE);
        selectBtn.setOnAction(e -> openCharacterSelection(game));

        card.getChildren().addAll(favRow, selectBtn);
        championsCardContainer.getChildren().add(card);
    }

    private String getCardTitle(String game) {
        return switch (game) {
            case "lol"      -> "🏆 MES CHAMPIONS FAVORIS";
            case "valorant" -> "🎯 MES AGENTS FAVORIS";
            default         -> "⭐ MES FAVORIS";
        };
    }

    private String getCharacterTerm(String game) {
        return "lol".equals(game) ? "champion" : "agent";
    }

    private VBox buildFavoriteTile(GameCharacter ch) {
        ImageView img = new ImageView();
        img.setFitWidth(70);
        img.setFitHeight(70);
        img.setPreserveRatio(true);
        try {
            img.setImage(new Image(ch.imageUrl(), 70, 70, true, true, true));
        } catch (Exception ignored) {}
        Circle clip = new Circle(35, 35, 35);
        img.setClip(clip);

        Label name = new Label(ch.name());
        name.getStyleClass().add("champion-name");

        Label role = new Label(ch.role());
        role.getStyleClass().add("champion-role");

        VBox tile = new VBox(4, img, name, role);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("champion-card-selected");
        tile.setMinWidth(90);
        return tile;
    }

    private void openCharacterSelection(String game) {
        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("⏳ Chargement");
        loading.setHeaderText(null);
        loading.setContentText("Connexion à Riot Data Dragon...\nChargement de tous les " + getCharacterTerm(game) + "s...");
        loading.show();

        new Thread(() -> {
            try {
                List<GameCharacter> all = dataDragon.fetchByGame(game);
                Platform.runLater(() -> {
                    loading.close();
                    showSelectionDialog(game, all);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loading.close();
                    AlertUtils.showError("Erreur API", "Impossible de charger les données : " + e.getMessage());
                });
            }
        }).start();
    }

    private void showSelectionDialog(String game, List<GameCharacter> all) {
        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle("🎯 Sélection des " + getCharacterTerm(game) + "s");
        dlg.setHeaderText(null);

        // État (final pour lambdas)
        final List<String> initialFavs = player.getFavoriteChampionsList();
        final List<String> selected = new ArrayList<>(
                initialFavs.size() > 3 ? initialFavs.subList(0, 3) : initialFavs);

        Label title = new Label("CHOISIS JUSQU'À 3 " + getCharacterTerm(game).toUpperCase() + "S");
        title.setStyle("-fx-text-fill: #1fb3d2; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label countLabel = new Label("Sélectionnés : " + selected.size() + "/3");
        countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher...");
        searchField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-pref-height: 38; -fx-background-radius: 8;");

        HBox rolesBox = new HBox(6);
        rolesBox.setAlignment(Pos.CENTER_LEFT);
        final List<Button> roleButtons = new ArrayList<>();
        final List<String> roles = "lol".equals(game)
                ? dataDragon.getAllRoles()
                : List.of("All", "Duelist", "Sentinel", "Initiator", "Controller");
        final String[] currentRole = {"All"};

        FlowPane grid = new FlowPane(10, 10);
        grid.setPrefWrapLength(700);
        grid.setStyle("-fx-padding: 10;");

        Runnable refresh = () -> {
            grid.getChildren().clear();
            String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            List<GameCharacter> filtered = new ArrayList<>();
            for (GameCharacter c : all) {
                boolean matchSearch = search.isBlank() ||
                        c.name().toLowerCase().contains(search) ||
                        c.title().toLowerCase().contains(search);
                boolean matchRole = "All".equals(currentRole[0]) ||
                        c.tags().stream().anyMatch(t -> t.equalsIgnoreCase(currentRole[0]));
                if (matchSearch && matchRole) filtered.add(c);
            }
            for (GameCharacter c : filtered) {
                grid.getChildren().add(buildSelectableTile(c, selected, countLabel));
            }
        };

        for (String r : roles) {
            Button b = new Button(r);
            b.getStyleClass().add(r.equals("All") ? "role-filter-btn-active" : "role-filter-btn");
            b.setOnAction(e -> {
                currentRole[0] = r;
                for (Button rb : roleButtons) {
                    rb.getStyleClass().setAll(rb.getText().equals(r) ? "role-filter-btn-active" : "role-filter-btn");
                }
                refresh.run();
            });
            roleButtons.add(b);
            rolesBox.getChildren().add(b);
        }

        searchField.textProperty().addListener((obs, o, n) -> refresh.run());

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(450);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox box = new VBox(12, title, countLabel, searchField, rolesBox, scroll);
        box.setStyle("-fx-padding: 18; -fx-background-color: #07152b;");
        box.setMinWidth(780);

        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().setStyle("-fx-background-color: #07152b;");

        ButtonType saveBtn   = new ButtonType("💾 SAUVEGARDER",  ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("❌ ANNULER",       ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        dlg.getDialogPane().lookupButton(saveBtn).setStyle(
                "-fx-background-color: #1fb3d2; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38;");
        dlg.getDialogPane().lookupButton(cancelBtn).setStyle(
                "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-background-radius: 14; -fx-pref-height: 38;");

        dlg.setResultConverter(b -> b == saveBtn ? selected : null);

        refresh.run();

        dlg.showAndWait().ifPresent(list -> {
            try {
                String csv = String.join(",", list);
                playerService.updateFavoriteChampions(player.getId(), csv);
                player.setFavoriteChampions(csv);
                renderChampionsCard();
                AlertUtils.showInfo("✅ Sauvegardé",
                        list.size() + " " + getCharacterTerm(game) + "(s) sélectionné(s).");
            } catch (Exception e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        });
    }

    private VBox buildSelectableTile(GameCharacter ch, List<String> selected, Label countLabel) {
        ImageView img = new ImageView();
        img.setFitWidth(70);
        img.setFitHeight(70);
        img.setPreserveRatio(true);
        try {
            img.setImage(new Image(ch.imageUrl(), 70, 70, true, true, true));
        } catch (Exception ignored) {}

        Label name = new Label(ch.name());
        name.getStyleClass().add("champion-name");

        Label role = new Label(ch.role());
        role.getStyleClass().add("champion-role");

        VBox tile = new VBox(4, img, name, role);
        tile.setAlignment(Pos.CENTER);
        tile.setMinWidth(90);
        tile.setMaxWidth(90);

        boolean isSelected = selected.contains(ch.name());
        tile.getStyleClass().add(isSelected ? "champion-card-selected" : "champion-card");

        tile.setOnMouseClicked(e -> {
            if (selected.contains(ch.name())) {
                selected.remove(ch.name());
            } else {
                if (selected.size() >= 3) {
                    AlertUtils.showError("Maximum atteint", "Tu peux sélectionner maximum 3 favoris.");
                    return;
                }
                selected.add(ch.name());
            }
            countLabel.setText("Sélectionnés : " + selected.size() + "/3");
            tile.getStyleClass().setAll(selected.contains(ch.name())
                    ? "champion-card-selected" : "champion-card");
        });

        return tile;
    }

    // ============================================================
    // ACTIONS PROFIL
    // ============================================================
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
    private void generateRandomAvatar() {
        String url = avatarService.generateRandomAvatarUrl();
        new Thread(() -> {
            try {
                Image img = new Image(url, 100, 100, true, true);
                Platform.runLater(() -> {
                    avatarView.setImage(img);
                    player.setAvatar(url);
                    try {
                        playerService.updateAvatar(player.getId(), url);
                        AlertUtils.showInfo("🎲 Avatar généré",
                                "Un nouvel avatar gaming a été appliqué !");
                    } catch (Exception ex) {
                        AlertUtils.showError("Erreur SQL", ex.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtils.showError("Erreur",
                        "Impossible de charger l'avatar : " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void saveProfile() {
        String newUsername = usernameField.getText().trim();
        String newEmail    = emailField.getText().trim();

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
            if (!newUsername.equals(player.getUsername())) {
                if (playerService.isUsernameTaken(newUsername, player.getId())) {
                    AlertUtils.showError("Pseudo déjà pris",
                            "Le pseudo \"" + newUsername + "\" est déjà utilisé.");
                    return;
                }
            }
            player.setUsername(newUsername);
            playerService.updateProfileSafe(player);

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
            Player fresh = playerService.getById(player.getId());
            if (fresh != null) this.player = fresh;
            renderAll();
            showEmailConfirmationDialog(newEmail, req.gamingCode);
        } catch (IllegalStateException ise) {
            AlertUtils.showError("Email refusé", ise.getMessage());
            emailField.setText(player.getEmail());
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

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
        Label instruction = new Label("Saisis le code reçu pour valider ton nouvel email.\n⏳ Le code expire dans 24h.");
        instruction.setStyle("-fx-text-fill: #b9c4d0; -fx-font-size: 12px;");
        instruction.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("XXXX-XXXX-XXXX");
        codeField.setStyle("-fx-background-color: #1b2940; -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: CENTER; " +
                "-fx-pref-height: 50; -fx-background-radius: 10; " +
                "-fx-border-color: #1fb3d2; -fx-border-radius: 10;");
        codeField.setMaxWidth(280);

        Label devHint = new Label("💡 Mode démo : code = " + gamingCode);
        devHint.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox content = new VBox(12, headerEmoji, title, subtitle, emailLabel, instruction, codeField, devHint);
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
                        "-fx-background-radius: 14; -fx-pref-height: 38;");
        dlg.getDialogPane().lookupButton(cancelBtn).setStyle(
                "-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14; -fx-pref-height: 38;");

        dlg.setResultConverter(b -> b == confirmBtn ? codeField.getText() : null);

        dlg.showAndWait().ifPresentOrElse(code -> {
            if (code == null || code.isBlank()) {
                AlertUtils.showError("Code requis", "Tu dois saisir le code reçu par email.");
                return;
            }
            try {
                if (emailService.confirmEmail(player.getId(), code)) {
                    AlertUtils.showInfo("✅ Email confirmé !",
                            "Ton nouvel email a été validé.");
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
            AlertUtils.showError("Pas d'équipe", "Tu n'as pas encore d'équipe.");
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openAvailableTeams() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/available_teams.fxml"));
            Parent root = loader.load();
            AvailableTeamsController ctrl = loader.getController();
            ctrl.initWithPlayer(player);
            ((Stage) avatarView.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openMyRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_requests.fxml"));
            Parent root = loader.load();
            MyRequestsController ctrl = loader.getController();
            ctrl.initWithPlayer(player);
            ((Stage) avatarView.getScene().getWindow()).setScene(new Scene(root, 1280, 800));
        } catch (Exception e) { e.printStackTrace(); }
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