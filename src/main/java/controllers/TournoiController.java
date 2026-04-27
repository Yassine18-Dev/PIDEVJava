package controllers;

import entities.Tournoi;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.TournoiService;

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

    @FXML private Label lblNom;
    @FXML private Label lblLieu;
    @FXML private Label lblDateDebut;
    @FXML private Label lblDateFin;

    private final TournoiService service = new TournoiService();
    private final ObservableList<Tournoi> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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

                    HBox row = new HBox(20);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("pro-row");

                    Label icon = new Label("🏆");
                    icon.setPrefSize(34, 34);
                    icon.getStyleClass().add("row-icon");

                    Label nom = new Label(tournoi.getNom());
                    nom.setPrefWidth(170);
                    nom.getStyleClass().add("row-title");

                    Label lieu = new Label(tournoi.getLieu());
                    lieu.setPrefWidth(100);
                    lieu.getStyleClass().add("row-text");

                    Label debut = new Label(tournoi.getDateDebut());
                    debut.setPrefWidth(110);
                    debut.getStyleClass().add("row-text");

                    Label fin = new Label(tournoi.getDateFin());
                    fin.setPrefWidth(110);
                    fin.getStyleClass().add("row-text");

                    Label statut = new Label(getStatut(tournoi));
                    statut.setPrefWidth(90);
                    statut.setAlignment(Pos.CENTER);
                    statut.getStyleClass().add(getStatutClass(tournoi));

                    Label menu = new Label("⋮");
                    menu.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

                    row.getChildren().addAll(icon, nom, lieu, debut, fin, statut, menu);
                    setText(null);
                    setGraphic(row);
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getClickCount() == 2) {
                    listTournois.getSelectionModel().select(cell.getItem());
                    ouvrirConsultationDepuisDoubleClick();
                }
            });

            return cell;
        });

        listTournois.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                lblNom.setText(selected.getNom());
                lblLieu.setText(selected.getLieu());
                lblDateDebut.setText(selected.getDateDebut());
                lblDateFin.setText(selected.getDateFin());
            }
        });

        loadData();
    }

    private String getStatut(Tournoi tournoi) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate debut = LocalDate.parse(tournoi.getDateDebut());
            LocalDate fin = LocalDate.parse(tournoi.getDateFin());

            if (today.isBefore(debut)) return "À venir";
            if (today.isAfter(fin)) return "Terminé";
            return "Actif";
        } catch (Exception e) {
            return "Actif";
        }
    }

    private String getStatutClass(Tournoi tournoi) {
        String statut = getStatut(tournoi);
        if ("À venir".equals(statut)) return "badge-coming";
        if ("Terminé".equals(statut)) return "badge-ended";
        return "badge-active";
    }

    private void loadData() {
        try {
            data.setAll(service.afficherTournois());
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

        List<Tournoi> result = data.stream()
                .filter(t -> t.getNom().toLowerCase().contains(keyword)
                        || t.getLieu().toLowerCase().contains(keyword)
                        || t.getDateDebut().toLowerCase().contains(keyword)
                        || t.getDateFin().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        listTournois.setItems(FXCollections.observableArrayList(result));
    }

    @FXML
    void actualiserListe() {
        tfRecherche.clear();
        clearDetails();
        loadData();
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
        if (selected == null) {
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
        if (selected == null) {
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
        if (selected == null) {
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