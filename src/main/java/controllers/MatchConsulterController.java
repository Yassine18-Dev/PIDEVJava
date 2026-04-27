package controllers;

import entities.Match;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MatchConsulterController {

    @FXML private Label lblEquipe1;
    @FXML private Label lblEquipe2;
    @FXML private Label lblDateMatch;
    @FXML private Label lblScore;

    private Runnable onClose;

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setMatch(Match match) {
        lblEquipe1.setText(match.getEquipe1());
        lblEquipe2.setText(match.getEquipe2());
        lblDateMatch.setText(match.getDateMatch());
        lblScore.setText(match.getScore());
    }

    @FXML
    void retour(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/match.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Matchs");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}