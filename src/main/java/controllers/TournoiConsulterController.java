package controllers;

import entities.Tournoi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TournoiConsulterController {

    @FXML private Label lblNom;
    @FXML private Label lblLieu;
    @FXML private Label lblDateDebut;
    @FXML private Label lblDateFin;
    @FXML private Label lblPrixInscription;

    private Runnable onClose;

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setTournoi(Tournoi tournoi) {
        lblNom.setText(tournoi.getNom());
        lblLieu.setText(tournoi.getLieu());
        lblDateDebut.setText(tournoi.getDateDebut());
        lblDateFin.setText(tournoi.getDateFin());
        lblPrixInscription.setText(formatPrix(tournoi.getPrixInscription()));
    }

    private String formatPrix(BigDecimal prix) {
        if (prix == null) return "0.00 €";
        return prix.setScale(2, RoundingMode.HALF_UP) + " €";
    }

    @FXML
    void retour(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tournoi.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Tournois");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}