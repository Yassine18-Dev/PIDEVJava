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
import java.math.RoundingMode;
import java.time.LocalDate;

public class TournoiModifierController {

    @FXML private TextField tfNom;
    @FXML private TextField tfLieu;
    @FXML private TextField tfDateDebut;
    @FXML private TextField tfDateFin;
    @FXML private TextField tfPrixInscription;
    @FXML private TextField tfMaxParticipants;
    @FXML private TextField tfDiscountPrice;

    private Tournoi tournoi;
    private Runnable onClose;
    private Runnable onDataChanged;
    private final TournoiService service = new TournoiService();

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void setTournoi(Tournoi tournoi) {
        this.tournoi = tournoi;

        tfNom.setText(tournoi.getNom());
        tfLieu.setText(tournoi.getLieu());
        tfDateDebut.setText(tournoi.getDateDebut());
        tfDateFin.setText(tournoi.getDateFin());

        BigDecimal prix = tournoi.getPrixInscription() != null ? tournoi.getPrixInscription() : BigDecimal.ZERO;
        tfPrixInscription.setText(prix.setScale(2, RoundingMode.HALF_UP).toString());

        tfMaxParticipants.setText(String.valueOf(tournoi.getMaxParticipants()));

        BigDecimal discount = tournoi.getDiscountPrice();
        tfDiscountPrice.setText(discount == null ? "" : discount.setScale(2, RoundingMode.HALF_UP).toString());
    }

    @FXML
    void modifierTournoi(ActionEvent event) {
        if (tournoi == null) {
            showAlert("Erreur", "Aucun tournoi sélectionné.");
            return;
        }

        String nom = tfNom.getText().trim();
        String lieu = tfLieu.getText().trim();
        String dateDebut = tfDateDebut.getText().trim();
        String dateFin = tfDateFin.getText().trim();
        String prixText = tfPrixInscription.getText().trim().replace(",", ".");
        String maxText = tfMaxParticipants.getText().trim();
        String discountText = tfDiscountPrice.getText().trim().replace(",", ".");

        if (nom.isEmpty() || lieu.isEmpty() || dateDebut.isEmpty() || dateFin.isEmpty()
                || prixText.isEmpty() || maxText.isEmpty()) {
            showAlert("Attention", "Tous les champs obligatoires doivent être remplis.");
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

            int maxParticipants = Integer.parseInt(maxText);

            if (maxParticipants <= 0) {
                showAlert("Attention", "Le nombre maximum de participants doit être supérieur à 0.");
                return;
            }

            if (tournoi.getCurrentParticipants() > maxParticipants) {
                showAlert("Attention", "Max participants ne peut pas être inférieur aux participants actuels.");
                return;
            }

            BigDecimal discountPrice = null;

            if (!discountText.isEmpty()) {
                discountPrice = new BigDecimal(discountText);

                if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                    showAlert("Attention", "Le prix remisé ne peut pas être négatif.");
                    return;
                }

                if (discountPrice.compareTo(prix) >= 0) {
                    showAlert("Attention", "Le prix remisé doit être inférieur au prix normal.");
                    return;
                }
            }

            tournoi.setNom(nom);
            tournoi.setLieu(lieu);
            tournoi.setDateDebut(dateDebut);
            tournoi.setDateFin(dateFin);
            tournoi.setPrixInscription(prix);
            tournoi.setMaxParticipants(maxParticipants);
            tournoi.setDiscountPrice(discountPrice);

            service.modifierTournoi(tournoi);

            showAlert("Succès", "Tournoi modifié avec succès.");

            if (onDataChanged != null) {
                onDataChanged.run();
            }

            retour(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix / remise / participants doivent être des valeurs valides.");
        } catch (Exception e) {
            showAlert("Erreur", "Vérifie les champs. Date demandée : yyyy-MM-dd.");
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