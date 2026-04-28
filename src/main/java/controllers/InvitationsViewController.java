package controllers;

import entities.Invitation;
import entities.Player;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.InvitationService;
import services.PlayerService;
import utils.AlertUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class InvitationsViewController {

    @FXML private VBox pendingBox, historyBox;

    private final InvitationService invService    = new InvitationService();
    private final PlayerService     playerService = new PlayerService();

    private Player player;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public void initWithPlayer(Player p) {
        this.player = p;
        loadInvitations();
    }

    private void loadInvitations() {
        pendingBox.getChildren().clear();
        historyBox.getChildren().clear();
        try {
            List<Invitation> all = invService.findInvitationsForPlayer(player.getId());
            if (all.isEmpty()) {
                pendingBox.getChildren().add(buildEmpty("Aucune invitation reçue."));
                historyBox.getChildren().add(buildEmpty("Pas d'historique."));
                return;
            }
            boolean anyPending = false, anyHistory = false;
            for (Invitation inv : all) {
                if (inv.getStatus() == Invitation.Status.PENDING) {
                    pendingBox.getChildren().add(buildPending(inv));
                    anyPending = true;
                } else {
                    historyBox.getChildren().add(buildHistory(inv));
                    anyHistory = true;
                }
            }
            if (!anyPending) pendingBox.getChildren().add(buildEmpty("Aucune invitation en attente."));
            if (!anyHistory) historyBox.getChildren().add(buildEmpty("Pas d'historique."));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    private Label buildEmpty(String msg) {
        Label l = new Label(msg);
        l.getStyleClass().add("subtitle-label");
        return l;
    }

    private VBox buildPending(Invitation inv) {
        Label title = new Label("🎮 " + inv.getTeamName());
        title.getStyleClass().add("title-label");

        Label sub = new Label("Invitation envoyée par " + inv.getSenderName()
                + " • " + fmt.format(inv.getSentAt()));
        sub.getStyleClass().add("subtitle-label");

        Label msg = new Label(inv.getMessage() != null && !inv.getMessage().isBlank()
                ? "💬 " + inv.getMessage() : "(Aucun message)");
        msg.getStyleClass().add("invitation-message");
        msg.setWrapText(true);

        Button accept = new Button("✅ ACCEPTER");
        accept.getStyleClass().add("button");
        accept.setOnAction(e -> handleAccept(inv));

        Button refuse = new Button("❌ REFUSER");
        refuse.getStyleClass().add("danger-button");
        refuse.setOnAction(e -> handleRefuse(inv));

        HBox actions = new HBox(10, accept, refuse);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, title, sub, msg, actions);
        card.getStyleClass().add("invitation-card-pending");
        return card;
    }

    private HBox buildHistory(Invitation inv) {
        Label icon = new Label(inv.getStatus() == Invitation.Status.ACCEPTED ? "✅" : "❌");
        icon.setStyle("-fx-font-size: 18px;");

        VBox text = new VBox(2);
        Label t1 = new Label(inv.getTeamName() + " — " + inv.getStatus());
        t1.getStyleClass().add("comp-name");
        Label t2 = new Label("Reçue le " + fmt.format(inv.getSentAt())
                + (inv.getRepliedAt() != null ? " • Répondu le " + fmt.format(inv.getRepliedAt()) : ""));
        t2.getStyleClass().add("subtitle-label");
        text.getChildren().addAll(t1, t2);

        HBox row = new HBox(10, icon, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("invitation-history-card");
        return row;
    }

    /** Accepte via le service transactionnel (6 règles + verrou SQL). */
    private void handleAccept(Invitation inv) {
        try {
            invService.acceptInvitation(inv.getId(), player.getId());
            AlertUtils.showInfo("Bienvenue !", "Tu as rejoint " + inv.getTeamName() + " ! 🎉");
            // Recharger le joueur (team_id mis à jour)
            Player fresh = playerService.getById(player.getId());
            if (fresh != null) this.player = fresh;
            loadInvitations();
        } catch (IllegalStateException ise) {
            AlertUtils.showError("Refusé", ise.getMessage());
            loadInvitations();
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    private void handleRefuse(Invitation inv) {
        try {
            invService.refuse(inv);
            AlertUtils.showInfo("Refusée", "Invitation refusée.");
            loadInvitations();
        } catch (Exception e) {
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void backToProfile() {
        try {
            MainController mainController = NavigationController.getMainController();
            if (mainController != null) {
                mainController.showPlayerProfile();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'accéder au contrôleur principal.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToTeam() {
        if (player == null) {
            AlertUtils.showError("Erreur", "Aucun joueur chargé.");
            return;
        }
        if (player.getTeamId() <= 0) {
            AlertUtils.showError("Pas d'équipe", "Tu n'es dans aucune équipe.");
            return;
        }
        try {
            MainController mainController = NavigationController.getMainController();
            if (mainController != null) {
                mainController.showTeamView();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'accéder au contrôleur principal.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}