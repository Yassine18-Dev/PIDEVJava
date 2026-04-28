package controllers;

import entities.Tournoi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.TournoiService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TournoiAjouterController {

    @FXML private TextField tfNom;
    @FXML private TextField tfLieu;
    @FXML private TextField tfDateDebut;
    @FXML private TextField tfDateFin;
    @FXML private TextField tfPrixInscription;

    private Runnable onClose;
    private Runnable onDataChanged;
    private final TournoiService service = new TournoiService();

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    @FXML
    void ajouterTournoi(ActionEvent event) {
        String nom = tfNom.getText().trim();
        String lieu = tfLieu.getText().trim();
        String dateDebut = tfDateDebut.getText().trim();
        String dateFin = tfDateFin.getText().trim();
        String prixText = tfPrixInscription.getText().trim().replace(",", ".");

        if (nom.isEmpty() || lieu.isEmpty() || dateDebut.isEmpty() || dateFin.isEmpty() || prixText.isEmpty()) {
            showAlert("Attention", "Tous les champs sont obligatoires.");
            return;
        }

        try {
            LocalDate d1 = LocalDate.parse(dateDebut);
            LocalDate d2 = LocalDate.parse(dateFin);

            if (d2.isBefore(d1)) {
                showAlert("Attention", "La date de fin doit être après la date de début.");
                return;
            }

            BigDecimal prix = new BigDecimal(prixText);

            if (prix.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Attention", "Le montant d'inscription ne peut pas être négatif.");
                return;
            }

            Tournoi tournoi = new Tournoi(nom, lieu, dateDebut, dateFin, prix);

            service.ajouterTournoi(tournoi);

            showAlert("Succès", "Tournoi ajouté avec succès.");

            if (onDataChanged != null) {
                onDataChanged.run();
            }

            retour(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide. Exemple : 20.00");
        } catch (Exception e) {
            showAlert("Erreur", "Vérifie les champs. Date demandée : yyyy-MM-dd et montant valide.");
        }
    }

    @FXML
    void retour(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
            return;
        }

        openPage(event, "/tournoi.fxml", "Gestion des Tournois");
    }

    private void openPage(ActionEvent event, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}