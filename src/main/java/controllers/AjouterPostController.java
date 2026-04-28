package controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.CloudinaryService;

public class AjouterPostController {

    @FXML
    private TextField titreInput;

    @FXML
    private TextArea contenuInput;

    @FXML
    private TextField imageInput;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnSelectImage;

    private String selectedImagePath = "";

    // ⚠️ À DÉCOMMENTER QUAND TON SERVEUR MYSQL SERA ALLUMÉ
    // private PostService postService = new PostService();

    @FXML
    private void onSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "Images (*.png, *.jpg, *.jpeg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) imageInput.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Fichier sélectionné: " + selectedFile.getAbsolutePath());
            
            // Upload to Cloudinary
            afficherAlerte("Upload", "Upload de l'image sur Cloudinary en cours...");
            String cloudinaryUrl = CloudinaryService.uploadImage(selectedFile);

            System.out.println("Résultat upload: " + cloudinaryUrl);

            if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
                imageInput.setText(cloudinaryUrl);
                afficherAlerte("Succès", "Image uploadée sur Cloudinary !");
            } else {
                imageInput.setText(selectedFile.getAbsolutePath());
                afficherAlerte("Erreur", "Échec de l'upload sur Cloudinary. Utilisation du chemin local.\nVérifiez la console pour plus de détails.");
            }
        }
    }

    @FXML
    void onAjouterPost(ActionEvent event) {
        String titre = titreInput.getText();
        String contenu = contenuInput.getText();
        String image = imageInput.getText();

        if (titre.isEmpty() || contenu.isEmpty()) {
            afficherAlerte("Erreur", "Le titre et le contenu sont obligatoires !");
            return;
        }

        Post nouveauPost = new Post(titre, contenu, image, new Timestamp(System.currentTimeMillis()), 1);

        /* =========================================================
           🛑 MODE BASE DE DONNÉES (À décommenter plus tard)
        ========================================================= */
        /*
        try {
            postService.ajouter(nouveauPost);
            afficherAlerte("Succès", "Ton post a été ajouté ! Redirection vers la liste...");
            redirigerVersListe(event); // On change de page après le succès
        } catch (SQLException e) {
            afficherAlerte("Erreur Base de données", e.getMessage());
        }
        */

        // =========================================================
        // ✅ MODE SIMULATION (Actif pour tes tests aujourd'hui)
        // =========================================================
        Post.addPost(nouveauPost);
        afficherAlerte("Succès (Mode Test)", "Post ajouté avec succès ! Redirection vers la liste...");

        // C'est ici que la magie opère :
        redirigerVersListe(event);
    }

    // Méthode pour revenir à la liste sans ajouter (si tu as un bouton "Annuler")
    @FXML
    void onNavListe(ActionEvent event) {
        redirigerVersListe(event);
    }

    // --- MÉTHODE DE NAVIGATION PARTAGÉE ---
    private void redirigerVersListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherPosts.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de redirection : " + e.getMessage());
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}