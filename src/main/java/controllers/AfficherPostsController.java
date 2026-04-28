package controllers;

import java.io.IOException;
import java.sql.Timestamp;

import entities.Post;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AfficherPostsController {

    @FXML
    private ListView<Post> postListView;

    @FXML
    private ListView<Post> editListView;

    @FXML
    private ListView<Post> deleteListView;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private ComboBox<String> popularityComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnPrevious;

    @FXML
    private Button btnNext;

    @FXML
    private Label pageLabel;
    
    @FXML
    private Label countLabel;

    private ObservableList<Post> postsData = FXCollections.observableArrayList();
    private ObservableList<Post> filteredPosts = FXCollections.observableArrayList();
    private ObservableList<Post> paginatedPosts = FXCollections.observableArrayList();

    private static final int POSTS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    // ⚠️ À DÉCOMMENTER QUAND MYSQL SERA PRÊT
    // private PostService postService = new PostService();

    @FXML
    public void initialize() {
        // Initialize filter combo box
        filterComboBox.getItems().addAll("Plus récent", "Plus ancien", "Sans filtre");
        filterComboBox.setValue("Sans filtre");

        // Initialize popularity combo box
        popularityComboBox.getItems().addAll("Plus likés", "Moins likés", "Plus commentés", "Sans filtre");
        popularityComboBox.setValue("Sans filtre");

        // Event listeners
        filterComboBox.setOnAction(event -> applyFilters());
        popularityComboBox.setOnAction(event -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Pagination buttons
        btnPrevious.setOnAction(event -> goToPreviousPage());
        btnNext.setOnAction(event -> goToNextPage());

        // --- CONFIGURATION DU MOULE DES CELLULES (DESIGN + BOUTONS) ---
        postListView.setCellFactory(param -> createPostCell(false));
        editListView.setCellFactory(param -> createPostCell(true));
        deleteListView.setCellFactory(param -> createPostCell(false));

        chargerDonnees();
    }

    private ListCell<Post> createPostCell(boolean editOnly) {
        return new ListCell<Post>() {
            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);

                if (empty || post == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 1. Infos du Post (Image + Texte)
                    ImageView imageView = new ImageView();
                    try {
                        String url = (post.getImageUrl() == null || post.getImageUrl().isEmpty())
                                     ? "https://via.placeholder.com/60"
                                     : post.getImageUrl();

                        // Convertir les chemins locaux en URLs de fichiers
                        if (url.contains(":") && !url.startsWith("http") && !url.startsWith("file:")) {
                            url = "file:/" + url.replace("\\", "/");
                        }

                        imageView.setImage(new Image(url, 60, 60, true, true));
                    } catch (Exception e) {
                        imageView.setImage(new Image("https://via.placeholder.com/60"));
                    }

                    Label labelTitre = new Label(post.getTitre());
                    labelTitre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label labelContenu = new Label(post.getContenu());
                    labelContenu.setWrapText(true);
                    labelContenu.setMaxWidth(200);

                    VBox textContainer = new VBox(labelTitre, labelContenu);
                    textContainer.setSpacing(5);

                    // 2. Boutons d'Action (Modifier / Supprimer / Like / Commentaire / Traduire)
                    VBox actionButtons = new VBox();
                    actionButtons.setSpacing(5);

                    // Bouton Like
                    Button btnLike = new Button("❤️ " + post.getLikes());
                    btnLike.setStyle("-fx-background-color: #EC4899; -fx-text-fill: white;");
                    btnLike.setOnAction(event -> handleLike(post));
                    actionButtons.getChildren().add(btnLike);

                    // Bouton Commentaire
                    Button btnComment = new Button("💬 Commenter");
                    btnComment.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white;");
                    btnComment.setOnAction(event -> handleComment(post));
                    actionButtons.getChildren().add(btnComment);

                    // Bouton Traduire
                    Button btnTranslate = new Button("🌐 Traduire");
                    btnTranslate.setStyle("-fx-background-color: #10B981; -fx-text-fill: white;");
                    btnTranslate.setOnAction(event -> handleTranslate(post, labelTitre, labelContenu));
                    actionButtons.getChildren().add(btnTranslate);

                    if (editOnly) {
                        Button btnEdit = new Button("✏️ Modifier");
                        btnEdit.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white;");
                        btnEdit.setOnAction(event -> handleModifier(post));
                        actionButtons.getChildren().add(btnEdit);
                    }
                    // Dans l'onglet "Tous les Posts", on n'affiche que Like, Commentaire et Traduction

                    // 3. Mise en page de la ligne
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    HBox root = new HBox(imageView, textContainer, spacer, actionButtons);
                    root.setSpacing(15);
                    root.setStyle("-fx-padding: 10; -fx-border-color: #374151; -fx-border-width: 0 0 1 0; -fx-background-color: #1F2937; -fx-background-radius: 5;");
                    
                    setGraphic(root);
                }
            }
        };
    }

    // --- R : READ (Affichage) ---
    private void chargerDonnees() {
        postsData.clear();
        /* --- CODE BDD (À décommenter plus tard) ---
        try {
            List<Post> listeBD = postService.recuperer();
            postsData.addAll(listeBD);
        } catch (SQLException e) { e.printStackTrace(); }
        */

        // Mode Simulation pour test - utilise la liste statique partagée
        postsData.addAll(Post.getPostsData());

        // Ajouter des posts de simulation si la liste est vide
        if (postsData.isEmpty()) {
            Post.addPost(new Post("Bienvenue !", "Ceci est un post de simulation.", "https://via.placeholder.com/60", new Timestamp(System.currentTimeMillis()), 1));
            Post.addPost(new Post("Post 2", "Contenu du post 2", "https://via.placeholder.com/60", new Timestamp(System.currentTimeMillis() - 86400000), 1));
            Post.addPost(new Post("Post 3", "Contenu du post 3", "https://via.placeholder.com/60", new Timestamp(System.currentTimeMillis() - 172800000), 1));
            postsData.addAll(Post.getPostsData());
        }

        applyFilters();
    }

    // --- FILTRAGE COMBINÉ (Recherche + Date + Popularité) ---
    private void applyFilters() {
        filteredPosts.clear();
        filteredPosts.addAll(postsData);

        // 1. Filtrage par recherche (titre + contenu)
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filteredPosts.removeIf(post ->
                !post.getTitre().toLowerCase().contains(searchText) &&
                !post.getContenu().toLowerCase().contains(searchText)
            );
        }

        // 2. Filtrage par date
        String dateFilter = filterComboBox.getValue();
        if (dateFilter != null) {
            switch (dateFilter) {
                case "Plus récent":
                    filteredPosts.sort(java.util.Comparator.comparing(Post::getDateCreation).reversed());
                    break;
                case "Plus ancien":
                    filteredPosts.sort(java.util.Comparator.comparing(Post::getDateCreation));
                    break;
                case "Sans filtre":
                default:
                    // No sorting
                    break;
            }
        }

        // 3. Filtrage par popularité
        String popularityFilter = popularityComboBox.getValue();
        if (popularityFilter != null) {
            switch (popularityFilter) {
                case "Plus likés":
                    filteredPosts.sort(java.util.Comparator.comparing(Post::getLikes).reversed());
                    break;
                case "Moins likés":
                    filteredPosts.sort(java.util.Comparator.comparing(Post::getLikes));
                    break;
                case "Plus commentés":
                    filteredPosts.sort((p1, p2) -> Integer.compare(p2.getComments().size(), p1.getComments().size()));
                    break;
                case "Sans filtre":
                default:
                    // No sorting
                    break;
            }
        }

        // Reset to page 1 when filters change
        currentPage = 1;
        updatePagination();
    }

    // --- PAGINATION ---
    private void updatePagination() {
        int totalPosts = filteredPosts.size();
        totalPages = (int) Math.ceil((double) totalPosts / POSTS_PER_PAGE);
        
        // Ensure current page is valid
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }
        
        // Update page label
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
        
        // Update count label
        countLabel.setText("Total: " + totalPosts + " posts");
        
        // Update button states
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
        
        // Update paginated posts
        updatePaginatedPosts();
    }

    private void updatePaginatedPosts() {
        paginatedPosts.clear();

        int startIndex = (currentPage - 1) * POSTS_PER_PAGE;
        int endIndex = Math.min(startIndex + POSTS_PER_PAGE, filteredPosts.size());

        if (startIndex < filteredPosts.size()) {
            paginatedPosts.addAll(filteredPosts.subList(startIndex, endIndex));
        }

        // Set data to list views
        postListView.setItems(paginatedPosts);
        editListView.setItems(filteredPosts); // Edit and delete tabs show all filtered posts
        deleteListView.setItems(filteredPosts);
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    // --- D : DELETE (Suppression) ---
    private void handleSupprimer(Post post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer définitivement ce post ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // postService.supprimer(post.getId()); // À décommenter plus tard
                Post.removePost(post);
                postsData.remove(post);
                applyFilters(); // Refresh all list views
            }
        });
    }

    // --- LIKE ---
    private void handleLike(Post post) {
        post.addLike();
        
        // Rafraîchir toutes les listes pour voir le nouveau compteur
        postListView.refresh();
        editListView.refresh();
        deleteListView.refresh();
        
        // Mettre à jour la liste partagée pour persistance
        applyFilters();
    }

    // --- COMMENTAIRE ---
    private void handleComment(Post post) {
        showCommentsDialog(post);
    }

    private void showCommentsDialog(Post post) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Commentaires du post");
        dialog.setHeaderText("Post : " + post.getTitre());

        ButtonType addCommentButtonType = new ButtonType("Ajouter un commentaire", ButtonBar.ButtonData.LEFT);
        ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addCommentButtonType, closeButtonType);

        // Affichage des commentaires existants
        VBox commentsContainer = new VBox(10);
        commentsContainer.setStyle("-fx-padding: 10; -fx-background-color: #1F2937; -fx-border-radius: 5;");

        if (post.getComments().isEmpty()) {
            commentsContainer.getChildren().add(new Label("Aucun commentaire pour ce moment."));
        } else {
            for (int i = 0; i < post.getComments().size(); i++) {
                String comment = post.getComments().get(i);
                Label commentLabel = new Label((i + 1) + ". " + comment);
                commentLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-wrap-text: true; -fx-padding: 5;");
                commentLabel.setMaxWidth(400);
                commentsContainer.getChildren().add(commentLabel);
            }
        }

        ScrollPane scrollPane = new ScrollPane(commentsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: #1F2937; -fx-border-color: #374151;");

        VBox layout = new VBox(10, scrollPane);
        dialog.getDialogPane().setContent(layout);

        // Gestion du bouton "Ajouter un commentaire"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addCommentButtonType) {
                showAddCommentDialog(post, dialog);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAddCommentDialog(Post post, Dialog<Void> parentDialog) {
        Dialog<String> addDialog = new Dialog<>();
        addDialog.setTitle("Nouveau commentaire");
        addDialog.setHeaderText("Ajouter un commentaire au post : " + post.getTitre());

        ButtonType submitButtonType = new ButtonType("Publier", ButtonBar.ButtonData.OK_DONE);
        addDialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Écrivez votre commentaire...");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(4);

        VBox layout = new VBox(10, new Label("Votre commentaire :"), commentArea);
        addDialog.getDialogPane().setContent(layout);

        addDialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return commentArea.getText();
            }
            return null;
        });

        addDialog.showAndWait().ifPresent(commentText -> {
            if (!commentText.trim().isEmpty()) {
                post.addComment(commentText);
                
                // Rafraîchir toutes les listes
                postListView.refresh();
                editListView.refresh();
                deleteListView.refresh();
                
                // Mettre à jour la liste partagée pour persistance
                applyFilters();
                
                afficherAlerte("Succès", "Commentaire ajouté !");
                
                // Fermer le dialogue parent et le rouvrir pour voir les commentaires mis à jour
                parentDialog.close();
                showCommentsDialog(post);
            }
        });
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- TRADUCTION ---
    private void handleTranslate(Post post, Label titreLabel, Label contenuLabel) {
        String originalTitre = post.getTitre();
        String originalContenu = post.getContenu();

        // Toggle entre original et traduit
        if (titreLabel.getText().startsWith("[TR] ")) {
            titreLabel.setText(originalTitre);
            contenuLabel.setText(originalContenu);
        } else {
            // Traduction intelligente selon la langue détectée
            afficherAlerte("Traduction", "Détection de la langue et traduction en cours...");
            String translatedTitre = utils.TranslateService.translateIntelligently(originalTitre);
            String translatedContenu = utils.TranslateService.translateIntelligently(originalContenu);

            titreLabel.setText("[TR] " + translatedTitre);
            contenuLabel.setText("[TR] " + translatedContenu);
            afficherAlerte("Traduction", "Traduction intelligente terminée !");
        }
    }

    // --- U : UPDATE (Modification Complète) ---
    private void handleModifier(Post post) {
        // Création de la boîte de dialogue
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Modifier le Post");
        dialog.setHeaderText("Édition de l'article : " + post.getTitre());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Champs de saisie pré-remplis
        TextField editTitre = new TextField(post.getTitre());
        TextArea editContenu = new TextArea(post.getContenu());
        editContenu.setPrefRowCount(3);
        TextField editUrl = new TextField(post.getImageUrl());

        // Bouton pour sélectionner une image
        Button btnSelectImage = new Button("📁 Sélectionner image");
        btnSelectImage.setStyle("-fx-background-color: #06B6D4; -fx-text-fill: white;");
        btnSelectImage.setOnAction(event -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            javafx.stage.FileChooser.ExtensionFilter extFilter = new javafx.stage.FileChooser.ExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
            fileChooser.getExtensionFilters().add(extFilter);

            javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
            java.io.File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                // Upload to Cloudinary
                afficherAlerte("Upload", "Upload de l'image sur Cloudinary en cours...");
                String cloudinaryUrl = utils.CloudinaryService.uploadImage(selectedFile);

                if (cloudinaryUrl != null) {
                    editUrl.setText(cloudinaryUrl);
                    afficherAlerte("Succès", "Image uploadée sur Cloudinary !");
                } else {
                    editUrl.setText(selectedFile.getAbsolutePath());
                    afficherAlerte("Erreur", "Échec de l'upload sur Cloudinary. Utilisation du chemin local.");
                }
            }
        });

        HBox imageContainer = new HBox(10, editUrl, btnSelectImage);

        VBox layout = new VBox(10, new Label("Titre :"), editTitre, new Label("Contenu :"), editContenu, new Label("URL Image :"), imageContainer);
        dialog.getDialogPane().setContent(layout);

        // Récupération des données au clic sur Enregistrer
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                post.setTitre(editTitre.getText());
                post.setContenu(editContenu.getText());
                post.setImageUrl(editUrl.getText());
                Post.updatePost(post);
                return post;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedPost -> {
            // postService.modifier(updatedPost); // À décommenter plus tard
            postListView.refresh(); // Rafraîchit l'affichage
        });
    }

    // --- NAVIGATION ---
    @FXML
    void onNavAjout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterPost.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void onActualiser(ActionEvent event) {
        chargerDonnees();
    }
}