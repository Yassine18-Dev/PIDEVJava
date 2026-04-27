package controllers;

import entities.Match;
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
import services.MatchService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MatchController {

    @FXML private ListView<Match> listMatchs;
    @FXML private TextField tfRecherche;

    @FXML private Label lblEquipe1;
    @FXML private Label lblEquipe2;
    @FXML private Label lblDateMatch;
    @FXML private Label lblScore;

    @FXML private StackPane modalOverlay;
    @FXML private StackPane modalContent;

    private final MatchService service = new MatchService();
    private final ObservableList<Match> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        listMatchs.setItems(data);

        listMatchs.setCellFactory(param -> {
            ListCell<Match> cell = new ListCell<>() {
                @Override
                protected void updateItem(Match match, boolean empty) {
                    super.updateItem(match, empty);

                    if (empty || match == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox row = new HBox(20);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("pro-row");

                    Label icon = new Label("🎮");
                    icon.setPrefSize(34, 34);
                    icon.getStyleClass().add("row-icon");

                    Label equipe1 = new Label(match.getEquipe1());
                    equipe1.setPrefWidth(140);
                    equipe1.getStyleClass().add("row-title");

                    Label equipe2 = new Label(match.getEquipe2());
                    equipe2.setPrefWidth(140);
                    equipe2.getStyleClass().add("row-title");

                    Label date = new Label(match.getDateMatch());
                    date.setPrefWidth(120);
                    date.getStyleClass().add("row-text");

                    Label score = new Label(match.getScore());
                    score.setPrefWidth(100);
                    score.getStyleClass().add("badge-active");
                    score.setAlignment(Pos.CENTER);

                    Label menu = new Label("⋮");
                    menu.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

                    row.getChildren().addAll(icon, equipe1, equipe2, date, score, menu);

                    setText(null);
                    setGraphic(row);
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getClickCount() == 2) {
                    listMatchs.getSelectionModel().select(cell.getItem());
                    ouvrirConsultationDepuisDoubleClick();
                }
            });

            return cell;
        });

        listMatchs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                lblEquipe1.setText(selected.getEquipe1());
                lblEquipe2.setText(selected.getEquipe2());
                lblDateMatch.setText(selected.getDateMatch());
                lblScore.setText(selected.getScore());
            }
        });

        loadData();
    }

    private void loadData() {
        try {
            data.setAll(service.afficherMatchs());
            listMatchs.setItems(data);
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void rechercherMatch() {
        String keyword = tfRecherche.getText().toLowerCase().trim();

        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        List<Match> result = data.stream()
                .filter(m -> m.getEquipe1().toLowerCase().contains(keyword)
                        || m.getEquipe2().toLowerCase().contains(keyword)
                        || m.getDateMatch().toLowerCase().contains(keyword)
                        || m.getScore().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        listMatchs.setItems(FXCollections.observableArrayList(result));
    }

    @FXML
    void actualiserListe() {
        tfRecherche.clear();
        clearDetails();
        loadData();
    }

    @FXML
    void ouvrirAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/match_ajouter.fxml"));
            Parent root = loader.load();

            MatchAjouterController controller = loader.getController();
            controller.setOnClose(this::fermerModal);
            controller.setOnDataChanged(this::actualiserApresModification);

            afficherModal(root);

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void ouvrirModifier(ActionEvent event) {
        Match selected = listMatchs.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Attention", "Sélectionne un match à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/match_modifier.fxml"));
            Parent root = loader.load();

            MatchModifierController controller = loader.getController();
            controller.setMatch(selected);
            controller.setOnClose(this::fermerModal);
            controller.setOnDataChanged(this::actualiserApresModification);

            afficherModal(root);

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void ouvrirConsultationDepuisDoubleClick() {
        Match selected = listMatchs.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/match_consulter.fxml"));
            Parent root = loader.load();

            MatchConsulterController controller = loader.getController();
            controller.setMatch(selected);
            controller.setOnClose(this::fermerModal);

            afficherModal(root);

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    void ouvrirSupprimer(ActionEvent event) {
        Match selected = listMatchs.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Attention", "Sélectionne un match à supprimer.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/match_supprimer.fxml"));
            Parent root = loader.load();

            MatchSupprimerController controller = loader.getController();
            controller.setMatch(selected);
            controller.setOnClose(this::fermerModal);
            controller.setOnDataChanged(this::actualiserApresModification);

            afficherModal(root);

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void afficherModal(Parent root) {
        modalContent.getChildren().setAll(root);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        modalOverlay.toFront();
    }

    private void fermerModal() {
        modalContent.getChildren().clear();
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
    }

    private void actualiserApresModification() {
        loadData();
        clearDetails();
    }

    @FXML
    void goToTournoi(ActionEvent event) {
        openPage(event, "/tournoi.fxml", "Gestion des Tournois");
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
        lblEquipe1.setText("-");
        lblEquipe2.setText("-");
        lblDateMatch.setText("-");
        lblScore.setText("-");
        listMatchs.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}