package controllers;

import entities.Team;
import entities.Tournoi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import services.StripeService;
import services.TeamService;
import services.TournoiService;
import utils.BrowserUtils;
import utils.SessionUtilisateur;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TournoiController {

    @FXML private StackPane modalOverlay;
    @FXML private StackPane modalContent;

    @FXML private ListView<Tournoi> listTournois;
    @FXML private TextField tfRecherche;
    @FXML private ComboBox<Team> cbTeams;

    @FXML private Label lblNom;
    @FXML private Label lblLieu;
    @FXML private Label lblDateDebut;
    @FXML private Label lblDateFin;
    @FXML private Label lblPrixInscription;

    @FXML private Label lblTotalTournois;
    @FXML private Label lblTournoisActifs;
    @FXML private Label lblTournoisTermines;
    @FXML private Label lblTotalInfo;
    @FXML private Label lblActifsInfo;
    @FXML private Label lblTerminesInfo;

    private final TournoiService service = new TournoiService();
    private final TeamService teamService = new TeamService();
    private final StripeService stripe = new StripeService();
    private final ObservableList<Tournoi> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        chargerTeams();
        listTournois.setItems(data);

        listTournois.setCellFactory(param -> {
            ListCell<Tournoi> cell = new ListCell<>() {
                @Override
                protected void updateItem(Tournoi tournoi, boolean empty) {
                    super.updateItem(tournoi, empty);

                    if (empty || tournoi == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    if (tournoi.getId() == 0) {
                        HBox row = new HBox(20);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.getStyleClass().add("pro-row");

                        Label icon = new Label("+");
                        icon.setPrefSize(34, 34);
                        icon.getStyleClass().add("row-icon");

                        Label text = new Label("Ajouter un tournoi");
                        text.setPrefWidth(560);
                        text.getStyleClass().add("row-title");

                        row.getChildren().addAll(icon, text);
                        row.setOnMouseClicked(e -> ouvrirAjouter(null));

                        setText(null);
                        setGraphic(row);
                        return;
                    }

                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    HBox row = new HBox(20);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("pro-row");

                    Label icon = new Label("🏆");
                    icon.setPrefSize(34, 34);
                    icon.getStyleClass().add("row-icon");

                    Label nom = new Label(tournoi.getNom());
                    nom.setPrefWidth(150);
                    nom.getStyleClass().add("row-title");

                    Label lieu = new Label(tournoi.getLieu());
                    lieu.setPrefWidth(90);
                    lieu.getStyleClass().add("row-text");

                    Label debut = new Label(tournoi.getDateDebut());
                    debut.setPrefWidth(100);
                    debut.getStyleClass().add("row-text");

                    Label fin = new Label(tournoi.getDateFin());
                    fin.setPrefWidth(100);
                    fin.getStyleClass().add("row-text");

                    Label prix = new Label(formatPrixListe(tournoi));
                    prix.setPrefWidth(135);
                    prix.setAlignment(Pos.CENTER);

                    String statut = getStatut(tournoi);

                    if (tournoi.getCurrentParticipants() >= tournoi.getMaxParticipants()) {
                        prix.getStyleClass().add("badge-ended");
                    } else if (statut.equals("TERMINE")) {
                        prix.getStyleClass().add("badge-ended");
                    } else if (statut.equals("EN_COURS")) {
                        prix.getStyleClass().add("badge-active");
                    } else {
                        prix.getStyleClass().add("badge-coming");
                    }

                    row.getChildren().addAll(icon, nom, lieu, debut, fin, prix);

                    HBox actions = new HBox(8);
                    actions.setAlignment(Pos.CENTER_RIGHT);
                    actions.setVisible(false);
                    actions.setManaged(false);
                    actions.getStyleClass().add("row-actions");

                    Button btnEdit = new Button("✎");
                    btnEdit.getStyleClass().add("btn-edit");
                    btnEdit.setOnAction(e -> {
                        listTournois.getSelectionModel().select(tournoi);
                        remplirDetails(tournoi);
                        ouvrirModifier(null);
                    });

                    Button btnDelete = new Button("🗑");
                    btnDelete.getStyleClass().add("btn-delete");
                    btnDelete.setOnAction(e -> {
                        listTournois.getSelectionModel().select(tournoi);
                        remplirDetails(tournoi);
                        ouvrirSupprimer(null);
                    });

                    actions.getChildren().addAll(btnEdit, btnDelete);

                    container.setOnMouseEntered(e -> {
                        actions.setVisible(true);
                        actions.setManaged(true);
                    });

                    container.setOnMouseExited(e -> {
                        actions.setVisible(false);
                        actions.setManaged(false);
                    });

                    container.getChildren().addAll(row, actions);

                    setText(null);
                    setGraphic(container);
                }
            };

            cell.setOnMouseClicked(event -> {
                Tournoi item = cell.getItem();

                if (!cell.isEmpty() && item != null && item.getId() != 0) {
                    listTournois.getSelectionModel().select(item);
                    remplirDetails(item);

                    if (event.getClickCount() == 2) {
                        ouvrirConsultationDepuisDoubleClick();
                    }
                }
            });

            return cell;
        });

        listTournois.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null && selected.getId() != 0) {
                remplirDetails(selected);
            }
        });

        loadData();
    }

    private void chargerTeams() {
        try {
            cbTeams.setItems(FXCollections.observableArrayList(teamService.findAll()));
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private Tournoi ligneAjouterTournoi() {
        Tournoi t = new Tournoi();
        t.setId(0);
        t.setNom("Ajouter un tournoi");
        return t;
    }

    private void remplirDetails(Tournoi selected) {
        lblNom.setText(selected.getNom());
        lblLieu.setText(selected.getLieu());
        lblDateDebut.setText(selected.getDateDebut());
        lblDateFin.setText(selected.getDateFin());

        String statut = getStatut(selected);

        if (selected.getCurrentParticipants() >= selected.getMaxParticipants()) {
            lblPrixInscription.setText("❌ COMPLET | "
                    + selected.getCurrentParticipants() + "/" + selected.getMaxParticipants());
            lblPrixInscription.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        } else if (statut.equals("TERMINE")) {
            lblPrixInscription.setText("🔴 Terminé | "
                    + selected.getCurrentParticipants() + "/" + selected.getMaxParticipants());
            lblPrixInscription.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        } else if (selected.isDiscountAvailable()) {
            lblPrixInscription.setText(
                    "🟡 À venir | 🔥 Promo : " + formatPrix(selected.getPrixFinal())
                            + " au lieu de " + formatPrix(selected.getPrixInscription())
                            + " | " + selected.getCurrentParticipants()
                            + "/" + selected.getMaxParticipants()
            );
            lblPrixInscription.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
        } else if (statut.equals("EN_COURS")) {
            lblPrixInscription.setText(
                    "🟢 En cours | " + formatPrix(selected.getPrixInscription())
                            + " | " + selected.getCurrentParticipants()
                            + "/" + selected.getMaxParticipants()
            );
            lblPrixInscription.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
        } else {
            lblPrixInscription.setText(
                    "🟡 À venir | " + formatPrix(selected.getPrixInscription())
                            + " | " + selected.getCurrentParticipants()
                            + "/" + selected.getMaxParticipants()
            );
            lblPrixInscription.setStyle("-fx-text-fill: #facc15; -fx-font-weight: bold;");
        }
    }

    private String formatPrixListe(Tournoi tournoi) {
        String statut = getStatut(tournoi);

        if (tournoi.getCurrentParticipants() >= tournoi.getMaxParticipants()) {
            return "❌ COMPLET";
        }

        if (statut.equals("TERMINE")) {
            return "🔴 Terminé";
        }

        if (statut.equals("EN_COURS")) {
            return "🟢 En cours | " + formatPrix(tournoi.getPrixInscription());
        }

        if (tournoi.isDiscountAvailable()) {
            return "🟡 À venir | 🔥 " + formatPrix(tournoi.getPrixFinal());
        }

        return "🟡 À venir | " + formatPrix(tournoi.getPrixInscription());
    }

    private String getStatut(Tournoi tournoi) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate debut = LocalDate.parse(tournoi.getDateDebut());
            LocalDate fin = LocalDate.parse(tournoi.getDateFin());

            if (today.isBefore(debut)) return "A_VENIR";
            if (today.isAfter(fin)) return "TERMINE";
            return "EN_COURS";

        } catch (Exception e) {
            return "EN_COURS";
        }
    }

    private String formatPrix(BigDecimal prix) {
        if (prix == null) {
            return "0.00 €";
        }

        return prix.setScale(2, RoundingMode.HALF_UP) + " €";
    }

    private void loadData() {
        try {
            data.clear();
            data.add(ligneAjouterTournoi());

            List<Tournoi> tournois = service.afficherTournois();
            data.addAll(tournois);

            int total = tournois.size();

            long actifs = tournois.stream()
                    .filter(t -> getStatut(t).equals("EN_COURS"))
                    .count();

            long termines = tournois.stream()
                    .filter(t -> getStatut(t).equals("TERMINE"))
                    .count();

            if (lblTotalTournois != null) lblTotalTournois.setText(String.valueOf(total));
            if (lblTournoisActifs != null) lblTournoisActifs.setText(String.valueOf(actifs));
            if (lblTournoisTermines != null) lblTournoisTermines.setText(String.valueOf(termines));

            if (lblTotalInfo != null) lblTotalInfo.setText("Tournois enregistrés");
            if (lblActifsInfo != null) lblActifsInfo.setText("Tournois en cours");

            if (lblTerminesInfo != null) {
                int percent = total == 0 ? 0 : (int) ((termines * 100) / total);
                lblTerminesInfo.setText(percent + "% du total");
            }

            listTournois.setItems(data);

        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void rechercherTournoi() {
        String keyword = tfRecherche.getText().toLowerCase().trim();

        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        try {
            List<Tournoi> result = service.afficherTournois().stream()
                    .filter(t -> t.getNom().toLowerCase().contains(keyword)
                            || t.getLieu().toLowerCase().contains(keyword)
                            || t.getDateDebut().toLowerCase().contains(keyword)
                            || t.getDateFin().toLowerCase().contains(keyword)
                            || formatPrix(t.getPrixInscription()).toLowerCase().contains(keyword)
                            || formatPrix(t.getPrixFinal()).toLowerCase().contains(keyword)
                            || String.valueOf(t.getMaxParticipants()).contains(keyword)
                            || String.valueOf(t.getCurrentParticipants()).contains(keyword)
                            || getStatut(t).toLowerCase().contains(keyword))
                    .collect(Collectors.toList());

            ObservableList<Tournoi> filtered = FXCollections.observableArrayList();
            filtered.add(ligneAjouterTournoi());
            filtered.addAll(result);

            listTournois.setItems(filtered);

        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void actualiserListe() {
        tfRecherche.clear();
        clearDetails();
        loadData();
        chargerTeams();
    }

    @FXML
    void ouvrirAjouter(ActionEvent event) {
        afficherModal("/tournoi_ajouter.fxml", controller -> {
            TournoiAjouterController c = (TournoiAjouterController) controller;
            c.setOnClose(this::fermerModal);
            c.setOnDataChanged(this::actualiserListe);
        });
    }

    @FXML
    void ouvrirModifier(ActionEvent event) {
        Tournoi selected = listTournois.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getId() == 0) {
            showAlert("Attention", "Sélectionne un tournoi à modifier.");
            return;
        }

        afficherModal("/tournoi_modifier.fxml", controller -> {
            TournoiModifierController c = (TournoiModifierController) controller;
            c.setTournoi(selected);
            c.setOnClose(this::fermerModal);
            c.setOnDataChanged(this::actualiserListe);
        });
    }

    private void ouvrirConsultationDepuisDoubleClick() {
        Tournoi selected = listTournois.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getId() == 0) {
            return;
        }

        afficherModal("/tournoi_consulter.fxml", controller -> {
            TournoiConsulterController c = (TournoiConsulterController) controller;
            c.setTournoi(selected);
            c.setOnClose(this::fermerModal);
        });
    }

    @FXML
    void ouvrirSupprimer(ActionEvent event) {
        Tournoi selected = listTournois.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getId() == 0) {
            showAlert("Attention", "Sélectionne un tournoi à supprimer.");
            return;
        }

        afficherModal("/tournoi_supprimer.fxml", controller -> {
            TournoiSupprimerController c = (TournoiSupprimerController) controller;
            c.setTournoi(selected);
            c.setOnClose(this::fermerModal);
            c.setOnDataChanged(this::actualiserListe);
        });
    }

    @FXML
    void payerInscription(ActionEvent event) {
        try {
            Tournoi tournoi = listTournois.getSelectionModel().getSelectedItem();

            if (tournoi == null || tournoi.getId() == 0) {
                showAlert("Erreur", "Sélectionne un tournoi.");
                return;
            }

            Team team = cbTeams.getValue();

            if (team == null) {
                showAlert("Attention", "Choisis une team à inscrire.");
                return;
            }

            Integer userId = SessionUtilisateur.getUserId();

            if (userId == null) {
                showAlert("Erreur", "Vous devez être connecté.");
                return;
            }

            if (getStatut(tournoi).equals("TERMINE")) {
                showAlert("Info", "Ce tournoi est terminé.");
                return;
            }

            if (tournoi.getCurrentParticipants() >= tournoi.getMaxParticipants()) {
                showAlert("Info", "Ce tournoi est complet.");
                return;
            }

            if (service.teamDejaInscrite(team.getId(), tournoi.getId())) {
                showAlert("Info", "Cette team est déjà inscrite à ce tournoi.");
                return;
            }

            BigDecimal prix = service.getPrix(tournoi.getId());

            if (prix == null || prix.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Erreur", "Prix du tournoi invalide.");
                return;
            }

            var session = stripe.createSession(tournoi.getId(), userId, prix);
            BrowserUtils.open(session.getUrl());

            showAlert("Paiement", "La page Stripe est ouverte. Termine le paiement dans le navigateur.");

            new Thread(() -> {
                try {
                    for (int i = 0; i < 30; i++) {
                        Thread.sleep(5000);

                        if (stripe.isPaid(session.getId())) {
                            service.inscrireTeam(team.getId(), tournoi.getId());

                            Platform.runLater(() -> {
                                showAlert("Succès", "Paiement confirmé. Team inscrite avec succès !");
                                actualiserListe();
                            });

                            return;
                        }
                    }

                    Platform.runLater(() ->
                            showAlert("Info", "Paiement non confirmé pour le moment.")
                    );

                } catch (Exception e) {
                    Platform.runLater(() ->
                            showAlert("Erreur", e.getMessage())
                    );
                }
            }).start();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void goToMatch(ActionEvent event) {
        openPage(event, "/match.fxml", "Gestion des Matchs");
    }

    private void afficherModal(String fxml, Consumer<Object> setupController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent content = loader.load();

            if (setupController != null) {
                setupController.accept(loader.getController());
            }

            modalContent.getChildren().setAll(content);
            modalOverlay.setManaged(true);
            modalOverlay.setVisible(true);
            modalOverlay.toFront();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void fermerModal() {
        modalContent.getChildren().clear();
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
    }

    private void openPage(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void clearDetails() {
        lblNom.setText("-");
        lblLieu.setText("-");
        lblDateDebut.setText("-");
        lblDateFin.setText("-");
        lblPrixInscription.setText("-");
        lblPrixInscription.setStyle("-fx-text-fill: white;");
        cbTeams.getSelectionModel().clearSelection();
        listTournois.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}