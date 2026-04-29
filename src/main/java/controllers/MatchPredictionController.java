package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MatchPredictionController {

    @FXML private Label lblPrediction;

    private Runnable onClose;

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setPrediction(String prediction) {
        lblPrediction.setText(prediction);
    }

    @FXML
    void retour(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
        }
    }
}