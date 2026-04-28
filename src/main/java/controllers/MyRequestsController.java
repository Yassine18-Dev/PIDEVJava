package controllers;

import entities.Player;
import entities.TeamRequest;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.TeamRequestService;
import utils.AlertUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyRequestsController {

    @FXML private VBox pendingBox, historyBox;

    private final TeamRequestService requestService = new TeamRequestService();

    private Player player;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public void initWithPlayer(Player p) {
        this.player = p;
        loadRequests();
    }

    private void loadRequests() {
        pendingBox.getChildren().clear();
        historyBox.getChildren().clear();
        try {
            List<TeamRequest> all = requestService.findRequestsByPlayer(player.getId());
            if (all.isEmpty()) {
                pendingBox.getChildren().add(empty("Tu n'as encore envoyé aucune demande."));
                historyBox.getChildren().add(empty("Pas d'historique."));
                return;
            }
            boolean anyPending = false, anyHistory = false;
            for (TeamRequest r : all) {
                if (r.getStatus() == TeamRequest.Status.PENDING) {
                    pendingBox.getChildren().add(buildPending(r));
                    anyPending = true;
                } else {
                    historyBox.getChildren().add(buildHistory(r));
                    anyHistory = true;
                }
            }
            if (!anyPending) pendingBox.getChildren().add(empty("Aucune demande en attente."));
            if (!anyHistory) historyBox.getChildren().add(empty("Pas d'historique."));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", e.getMessage());
        }
    }

    private Label empty(String msg) {
        Label l = new Label(msg);
        l.getStyleClass().add("subtitle-label");
        return l;
    }

    private VBox buildPending(TeamRequest r) {
        Label title = new Label("🎮 " + r.getTeamName());
        title.getStyleClass().add("title-label");

        Label sub = new Label("Demande envoyée le " + fmt.format(r.getSentAt())
                + " • Jeu : " + r.getTeamGame().toUpperCase());
        sub.getStyleClass().add("subtitle-label");

        Label msg = new Label(r.getMessage() != null && !r.getMessage().isBlank()
                ? "💬 " + r.getMessage() : "(Aucun message)");
        msg.getStyleClass().add("invitation-message");
        msg.setWrapText(true);

        Label status = new Label("⏳ EN ATTENTE DE RÉPONSE");
        status.getStyleClass().add("rank-badge");

        VBox card = new VBox(8, title, sub, msg, status);
        card.getStyleClass().add("invitation-card-pending");
        return card;
    }

    private HBox buildHistory(TeamRequest r) {
        Label icon = new Label(r.getStatus() == TeamRequest.Status.ACCEPTED ? "✅" : "❌");
        icon.setStyle("-fx-font-size: 18px;");

        VBox text = new VBox(2);
        Label t1 = new Label(r.getTeamName() + " — " + r.getStatus());
        t1.getStyleClass().add("comp-name");
        Label t2 = new Label("Envoyée le " + fmt.format(r.getSentAt())
                + (r.getRepliedAt() != null ? " • Répondu le " + fmt.format(r.getRepliedAt()) : ""));
        t2.getStyleClass().add("subtitle-label");
        text.getChildren().addAll(t1, t2);

        HBox row = new HBox(10, icon, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("invitation-history-card");
        return row;
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openAvailable() {
        try {
            MainController mainController = NavigationController.getMainController();
            if (mainController != null) {
                mainController.showAvailableTeams();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'accéder au contrôleur principal.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openInvitations() {
        try {
            MainController mainController = NavigationController.getMainController();
            if (mainController != null) {
                mainController.showInvitations();
            } else {
                AlertUtils.showError("Erreur", "Impossible d'accéder au contrôleur principal.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}