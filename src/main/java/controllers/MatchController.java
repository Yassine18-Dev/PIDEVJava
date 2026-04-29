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
import services.PredictionService;

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

    @FXML private Label lblTotalMatchs;
    @FXML private Label lblMatchsActifs;
    @FXML private Label lblMatchsTermines;
    @FXML private Label lblTotalInfo;
    @FXML private Label lblActifsInfo;
    @FXML private Label lblTerminesInfo;

    @FXML private StackPane modalOverlay;
    @FXML private StackPane modalContent;

    private final MatchService service = new MatchService();
    private final PredictionService predictionService = new PredictionService();
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

                    if (match.getId() == 0) {
                        HBox row = new HBox(20);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.getStyleClass().add("pro-row");

                        Label icon = new Label("+");
                        icon.setPrefSize(34, 34);
                        icon.getStyleClass().add("row-icon");

                        Label text = new Label("Ajouter un match");
                        text.setPrefWidth(520);
                        text.getStyleClass().add("row-title");

                        row.getChildren().addAll(icon, text);
                        row.setOnMouseClicked(e -> ouvrirAjouter(null));

                        setText(null);
                        setGraphic(row);
                        return;
                    }

                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    HBox row = new HBox(18);
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

                    Label date = new Label(match.getDateHeureMatch());
                    date.setPrefWidth(150);
                    date.getStyleClass().add("row-text");

                    Label score = new Label(match.getScore());
                    score.setPrefWidth(80);
                    score.getStyleClass().add("badge-active");
                    score.setAlignment(Pos.CENTER);

                    String etatAuto = match.getEtatAuto();

                    Label etat = new Label(etatAuto);
                    etat.setPrefWidth(100);
                    etat.setAlignment(Pos.CENTER);

                    if ("A_VENIR".equals(etatAuto)) {
                        etat.getStyleClass().add("badge-coming");
                    } else if ("EN_COURS".equals(etatAuto)) {
                        etat.getStyleClass().add("badge-active");
                    } else {
                        etat.getStyleClass().add("badge-ended");
                    }

                    row.getChildren().addAll(icon, equipe1, equipe2, date, score, etat);

                    HBox actions = new HBox(8);
                    actions.setAlignment(Pos.CENTER_RIGHT);
                    actions.setVisible(false);
                    actions.setManaged(false);
                    actions.getStyleClass().add("row-actions");

                    Button btnEdit = new Button("✎");
                    btnEdit.getStyleClass().add("btn-edit");
                    btnEdit.setOnAction(e -> {
                        listMatchs.getSelectionModel().select(match);
                        remplirDetails(match);
                        ouvrirModifier(null);
                    });

                    Button btnDelete = new Button("🗑");
                    btnDelete.getStyleClass().add("btn-delete");
                    btnDelete.setOnAction(e -> {
                        listMatchs.getSelectionModel().select(match);
                        remplirDetails(match);
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
                Match item = cell.getItem();

                if (!cell.isEmpty() && item != null && item.getId() != 0) {
                    listMatchs.getSelectionModel().select(item);
                    remplirDetails(item);

                    if (event.getClickCount() == 2) {
                        ouvrirConsultationDepuisDoubleClick();
                    }
                }
            });

            return cell;
        });

        listMatchs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null && selected.getId() != 0) {
                remplirDetails(selected);
            }
        });

        loadData();
    }

    private Match ligneAjouterMatch() {
        Match m = new Match();
        m.setId(0);
        m.setEquipe1("Ajouter un match");
        return m;
    }

    private void remplirDetails(Match selected) {
        lblEquipe1.setText(selected.getEquipe1());
        lblEquipe2.setText(selected.getEquipe2());
        lblDateMatch.setText(selected.getDateHeureMatch());
        lblScore.setText(selected.getScore());
    }

    private void loadData() {
        try {
            data.clear();
            data.add(ligneAjouterMatch());

            List<Match> matchs = service.afficherMatchs();
            data.addAll(matchs);

            int total = matchs.size();

            long actifs = matchs.stream()
                    .filter(m -> "EN_COURS".equals(m.getEtatAuto()))
                    .count();

            long termines = matchs.stream()
                    .filter(m -> "TERMINE".equals(m.getEtatAuto()))
                    .count();

            if (lblTotalMatchs != null) lblTotalMatchs.setText(String.valueOf(total));
            if (lblMatchsActifs != null) lblMatchsActifs.setText(String.valueOf(actifs));
            if (lblMatchsTermines != null) lblMatchsTermines.setText(String.valueOf(termines));

            if (lblTotalInfo != null) lblTotalInfo.setText("Matchs enregistrés");
            if (lblActifsInfo != null) lblActifsInfo.setText("Matchs en cours");

            if (lblTerminesInfo != null) {
                int percent = total == 0 ? 0 : (int) ((termines * 100) / total);
                lblTerminesInfo.setText(percent + "% du total");
            }

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

        try {
            List<Match> result = service.afficherMatchs().stream()
                    .filter(m -> m.getEquipe1().toLowerCase().contains(keyword)
                            || m.getEquipe2().toLowerCase().contains(keyword)
                            || m.getDateMatch().toLowerCase().contains(keyword)
                            || (m.getHeureMatch() != null && m.getHeureMatch().toLowerCase().contains(keyword))
                            || m.getDateHeureMatch().toLowerCase().contains(keyword)
                            || m.getScore().toLowerCase().contains(keyword)
                            || m.getEtatAuto().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());

            ObservableList<Match> filtered = FXCollections.observableArrayList();
            filtered.add(ligneAjouterMatch());
            filtered.addAll(result);

            listMatchs.setItems(filtered);

        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
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

        if (selected == null || selected.getId() == 0) {
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

        if (selected == null || selected.getId() == 0) {
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

        if (selected == null || selected.getId() == 0) {
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

    @FXML
    void predireMatch(ActionEvent event) {
        Match selected = listMatchs.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getId() == 0) {
            showAlert("Attention", "Sélectionne un match à prédire.");
            return;
        }

        try {
            String resultat = predictionService.predire(selected);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/match_prediction.fxml"));
            Parent root = loader.load();

            MatchPredictionController controller = loader.getController();
            controller.setPrediction(resultat);
            controller.setOnClose(this::fermerModal);

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