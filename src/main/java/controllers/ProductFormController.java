package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import entities.*;
import services.ProductService;
import services.ImageAnalysisService;
import java.io.File;

/**
 * Contrôleur pour le formulaire d'ajout/modification de produits
 */
public class ProductFormController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    private final ImageAnalysisService imageAnalysisService = new ImageAnalysisService();
    
    // ==================== ÉTAT ====================
    private Product editingProduct = null;
    private boolean isEditMode = false;
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private Label formTitleLabel;
    
    @FXML
    private RadioButton skinRadioButton;
    
    @FXML
    private RadioButton merchRadioButton;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private TextField imageField;
    
    @FXML
    private TextField stockField;
    
    @FXML
    private VBox sizesVBox;
    
    @FXML
    private CheckBox sizeSCheckBox;
    
    @FXML
    private CheckBox sizeMCheckBox;
    
    @FXML
    private CheckBox sizeLCheckBox;
    
    @FXML
    private CheckBox sizeXLCheckBox;
    
    @FXML
    private VBox previewVBox;
    
    @FXML
    private ImageView previewImageView;
    
    @FXML
    private Label previewNameLabel;
    
    @FXML
    private Label previewPriceLabel;
    
    @FXML
    private Label previewStockLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Label statusLabel;
    
    // ToggleGroup créé par programmation pour éviter les erreurs FXML
    private ToggleGroup productTypeToggleGroup;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        setupToggleGroup();
        setupPreview();
        setupValidation();
    }
    
    /**
     * Configure le groupe de boutons radio
     */
    private void setupToggleGroup() {
        // Créer le ToggleGroup par programmation
        productTypeToggleGroup = new ToggleGroup();
        
        // Associer les RadioButton au ToggleGroup
        if (skinRadioButton != null && merchRadioButton != null) {
            skinRadioButton.setToggleGroup(productTypeToggleGroup);
            merchRadioButton.setToggleGroup(productTypeToggleGroup);
            
            productTypeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateSizesVisibility();
                    updatePreview();
                }
            });
        }
    }
    
    /**
     * Configure la prévisualisation
     */
    private void setupPreview() {
        if (nameField != null) {
            nameField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        }
        if (priceField != null) {
            priceField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        }
        if (stockField != null) {
            stockField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        }
    }
    
    /**
     * Configure la validation en temps réel
     */
    private void setupValidation() {
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        
        // Activer le bouton sauvegarder quand tous les champs requis sont remplis
        if (nameField != null && priceField != null && stockField != null) {
            nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
            priceField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
            stockField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        }
    }
    
    // ==================== ACTIONS ====================
    
    /**
     * Sauvegarde le produit
     */
    @FXML
    public void saveProduct() {
        if (!validateForm()) {
            updateStatus("Please fill all required fields correctly");
            return;
        }
        
        try {
            Product product = createProductFromForm();
            
            if (isEditMode && editingProduct != null) {
                // Mode édition
                product.setId(editingProduct.getId());
                productService.update(product);
                updateStatus("Product updated successfully!");
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Product Updated");
                alert.setContentText(product.getName() + " has been updated successfully.");
                alert.show();
                
            } else {
                // Mode ajout
                productService.add(product);
                updateStatus("Product added successfully!");
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Product Added");
                alert.setContentText(product.getName() + " has been added successfully.");
                alert.show();
                
                resetForm();
            }
            
        } catch (Exception e) {
            System.err.println("Error saving product: " + e.getMessage());
            updateStatus("Error saving product: " + e.getMessage());
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Save Failed");
            alert.setContentText("Failed to save product: " + e.getMessage());
            alert.show();
        }
    }
    
    /**
     * Réinitialise le formulaire
     */
    @FXML
    public void resetForm() {
        if (nameField != null) nameField.clear();
        if (priceField != null) priceField.clear();
        if (descriptionField != null) descriptionField.clear();
        if (imageField != null) imageField.clear();
        if (stockField != null) stockField.clear();
        
        if (sizeSCheckBox != null) sizeSCheckBox.setSelected(false);
        if (sizeMCheckBox != null) sizeMCheckBox.setSelected(false);
        if (sizeLCheckBox != null) sizeLCheckBox.setSelected(false);
        if (sizeXLCheckBox != null) sizeXLCheckBox.setSelected(false);
        
        if (skinRadioButton != null) skinRadioButton.setSelected(true);
        
        updatePreview();
        updateStatus("Form reset");
    }
    
    /**
     * Annule le formulaire
     */
    @FXML
    public void cancelForm() {
        navigateBack();
    }
    
    /**
     * Parcourt pour une image
     */
    @FXML
    public void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );
        
        // Ouvrir le dialogue dans le répertoire de l'utilisateur
        File initialDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialDirectory);
        
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null && imageField != null) {
            imageField.setText(selectedFile.getAbsolutePath());
            updatePreview();
            updateStatus("Image sélectionnée: " + selectedFile.getName());
        }
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Met le contrôleur en mode édition
     */
    public void setEditMode(Product product) {
        this.editingProduct = product;
        this.isEditMode = true;
        
        if (formTitleLabel != null) {
            formTitleLabel.setText("Edit Product");
        }
        
        // Remplir le formulaire avec les données du produit
        if (nameField != null) nameField.setText(product.getName());
        if (priceField != null) priceField.setText(String.valueOf(product.getPrice()));
        if (descriptionField != null) descriptionField.setText(product.getDescription());
        if (imageField != null) imageField.setText(product.getImage());
        if (stockField != null) stockField.setText(String.valueOf(product.getStock()));
        
        // Sélectionner le type de produit
        if ("skin".equals(product.getType()) && skinRadioButton != null) {
            skinRadioButton.setSelected(true);
        } else if ("merch".equals(product.getType()) && merchRadioButton != null) {
            merchRadioButton.setSelected(true);
        }
        
        // Pour les merch, cocher les tailles disponibles
        if (product instanceof Merch) {
            Merch merch = (Merch) product;
            String sizes = merch.getSizes();
            if (sizes != null) {
                if (sizeSCheckBox != null) sizeSCheckBox.setSelected(sizes.contains("S"));
                if (sizeMCheckBox != null) sizeMCheckBox.setSelected(sizes.contains("M"));
                if (sizeLCheckBox != null) sizeLCheckBox.setSelected(sizes.contains("L"));
                if (sizeXLCheckBox != null) sizeXLCheckBox.setSelected(sizes.contains("XL"));
            }
        }
        
        updateSizesVisibility();
        updatePreview();
    }
    
    /**
     * Met à jour la visibilité des tailles
     */
    private void updateSizesVisibility() {
        boolean isMerch = merchRadioButton != null && merchRadioButton.isSelected();
        if (sizesVBox != null) {
            sizesVBox.setVisible(isMerch);
            sizesVBox.setManaged(isMerch);
        }
    }
    
    /**
     * Met à jour la prévisualisation
     */
    private void updatePreview() {
        if (previewVBox == null) return;
        
        String name = nameField != null ? nameField.getText() : "";
        String priceText = priceField != null ? priceField.getText() : "";
        String stockText = stockField != null ? stockField.getText() : "";
        String imagePath = imageField != null ? imageField.getText() : "";
        
        // Toujours afficher le preview
        previewVBox.setVisible(true);
        previewVBox.setManaged(true);
        
        // Mettre à jour l'image du preview
        if (previewImageView != null) {
            if (!imagePath.isEmpty()) {
                try {
                    if (imagePath.startsWith("http")) {
                        // URL
                        javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath, true);
                        previewImageView.setImage(image);
                    } else {
                        // Fichier local
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString(), true);
                            previewImageView.setImage(image);
                        } else {
                            // Image par défaut
                            try {
                                javafx.scene.image.Image defaultImage = new javafx.scene.image.Image("/uploads/default_product.png", true);
                                previewImageView.setImage(defaultImage);
                            } catch (Exception e) {
                                // Image vide si erreur
                                previewImageView.setImage(null);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading preview image: " + e.getMessage());
                    // Image par défaut en cas d'erreur
                    try {
                        javafx.scene.image.Image defaultImage = new javafx.scene.image.Image("/uploads/default_product.png", true);
                        previewImageView.setImage(defaultImage);
                    } catch (Exception ex) {
                        previewImageView.setImage(null);
                    }
                }
            } else {
                // Image par défaut si pas de chemin
                try {
                    javafx.scene.image.Image defaultImage = new javafx.scene.image.Image("/uploads/default_product.png", true);
                    previewImageView.setImage(defaultImage);
                } catch (Exception e) {
                    previewImageView.setImage(null);
                }
            }
        }
        
        if (previewNameLabel != null) {
            previewNameLabel.setText(name.isEmpty() ? "Product Name" : name);
        }
        
        if (previewPriceLabel != null) {
            try {
                double price = Double.parseDouble(priceText.isEmpty() ? "0" : priceText);
                previewPriceLabel.setText(String.format("%.2f TND", price));
            } catch (NumberFormatException e) {
                previewPriceLabel.setText("0.00 TND");
            }
        }
        
        if (previewStockLabel != null) {
            try {
                int stock = Integer.parseInt(stockText.isEmpty() ? "0" : stockText);
                String stockStatus = stock == 0 ? "Out of Stock" : 
                                   stock < 5 ? "Low Stock (" + stock + ")" : 
                                   "In Stock (" + stock + ")";
                previewStockLabel.setText(stockStatus);
            } catch (NumberFormatException e) {
                previewStockLabel.setText("In Stock (0)");
            }
        }
    }
    
    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();
        
        // Validation du nom
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            isValid = false;
            errorMessage.append("Le nom est requis. ");
        } else if (nameField.getText().trim().length() < 3) {
            isValid = false;
            errorMessage.append("Le nom doit contenir au moins 3 caractères. ");
        }
        
        // Validation du prix
        if (priceField == null || priceField.getText().trim().isEmpty()) {
            isValid = false;
            errorMessage.append("Le prix est requis. ");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    isValid = false;
                    errorMessage.append("Le prix ne peut pas être négatif. ");
                } else if (price > 10000) {
                    isValid = false;
                    errorMessage.append("Le prix ne peut pas dépasser 10000 TND. ");
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage.append("Le prix doit être un nombre valide. ");
            }
        }
        
        // Validation du stock
        if (stockField == null || stockField.getText().trim().isEmpty()) {
            isValid = false;
            errorMessage.append("Le stock est requis. ");
        } else {
            try {
                int stock = Integer.parseInt(stockField.getText());
                if (stock < 0) {
                    isValid = false;
                    errorMessage.append("Le stock ne peut pas être négatif. ");
                } else if (stock > 10000) {
                    isValid = false;
                    errorMessage.append("Le stock ne peut pas dépasser 10000 unités. ");
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage.append("Le stock doit être un nombre entier valide. ");
            }
        }
        
        // Validation de la description
        if (descriptionField != null && !descriptionField.getText().trim().isEmpty()) {
            if (descriptionField.getText().trim().length() > 500) {
                isValid = false;
                errorMessage.append("La description ne peut pas dépasser 500 caractères. ");
            }
        }
        
        // Validation de l'image
        if (imageField != null && !imageField.getText().trim().isEmpty()) {
            String imagePath = imageField.getText().trim();
            if (!imagePath.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                isValid = false;
                errorMessage.append("L'image doit être un fichier valide (jpg, jpeg, png, gif, bmp). ");
            }
        }
        
        // Pour les merch, au moins une taille doit être sélectionnée
        if (merchRadioButton != null && merchRadioButton.isSelected()) {
            boolean hasSize = (sizeSCheckBox != null && sizeSCheckBox.isSelected()) ||
                             (sizeMCheckBox != null && sizeMCheckBox.isSelected()) ||
                             (sizeLCheckBox != null && sizeLCheckBox.isSelected()) ||
                             (sizeXLCheckBox != null && sizeXLCheckBox.isSelected());
            if (!hasSize) {
                isValid = false;
                errorMessage.append("Au moins une taille doit être sélectionnée pour les merch. ");
            }
        }
        
        // Mettre à jour le statut avec les erreurs
        if (!isValid) {
            updateStatus("Erreurs: " + errorMessage.toString());
        } else {
            updateStatus("Formulaire valide");
        }
        
        if (saveButton != null) {
            saveButton.setDisable(!isValid);
        }
        
        return isValid;
    }
    
    /**
     * Crée un produit à partir du formulaire
     */
    private Product createProductFromForm() {
        String name = nameField.getText().trim();
        double price = Double.parseDouble(priceField.getText());
        String description = descriptionField.getText().trim();
        String image = imageField.getText().trim();
        if (image.isEmpty()) {
            image = "uploads/default_product.png";
        }
        int stock = Integer.parseInt(stockField.getText());
        
        boolean isSkin = skinRadioButton.isSelected();
        
        if (isSkin) {
            return new entities.Skin(0, name, price, description, image, stock);
        } else {
            // Construire la chaîne de tailles
            StringBuilder sizes = new StringBuilder();
            if (sizeSCheckBox.isSelected()) sizes.append("S,");
            if (sizeMCheckBox.isSelected()) sizes.append("M,");
            if (sizeLCheckBox.isSelected()) sizes.append("L,");
            if (sizeXLCheckBox.isSelected()) sizes.append("XL,");
            
            String sizesStr = sizes.length() > 0 ? sizes.substring(0, sizes.length() - 1) : "";
            return new Merch(0, name, price, description, image, stock, sizesStr);
        }
    }
    
    /**
     * Met à jour le message de statut
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Retourne à la vue précédente
     */
    private void navigateBack() {
        if (isEditMode) {
            NavigationController.showAllProducts();
        } else {
            NavigationController.showDashboard();
        }
    }
    
    /**
     * Analyse l'image avec l'IA pour générer automatiquement les informations du produit
     */
    @FXML
    public void analyzeImageWithAI() {
        String imagePath = imageField.getText();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            showAlert("Erreur", "Veuillez d'abord sélectionner une image");
            return;
        }
        
        // Créer un produit temporaire pour l'analyse
        if (editingProduct == null) {
            editingProduct = new Product() {
                @Override
                public String getType() { 
                    return merchRadioButton.isSelected() ? "merch" : "skin"; 
                }
            };
        }
        
        showAlert("🚀 Analyse IA Moderne", "Upload Cloudinary + Analyse Gemini Vision en cours...");
        
        // Lancer l'analyse unifiée
        File imageFile = new File(imagePath);
        imageAnalysisService.analyzeImage(imageFile)
            .thenAccept(result -> {
                // Appliquer les résultats sur le thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    applyModernAIResults(result);
                    showAlert("✅ Succès", "Analyse terminée ! Informations générées par IA moderne.");
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("❌ Erreur", "Erreur lors de l'analyse: " + throwable.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Applique les résultats de l'IA moderne (Gemini) aux champs du formulaire
     */
    private void applyModernAIResults(services.ImageAnalysisService.ProductAnalysisResult result) {
        nameField.setText(result.getName());
        priceField.setText(String.valueOf(result.getPrice()));
        descriptionField.setText(result.getDescription());
        
        // Mettre à jour le type de produit (category)
        if ("skin".equals(result.getType())) {
            skinRadioButton.setSelected(true);
        } else {
            merchRadioButton.setSelected(true);
        }
        
        updateSizesVisibility();
        updatePreview();
        
        // Mettre à jour l'image du produit avec l'URL Cloudinary (si disponible)
        if (editingProduct != null) {
            // L'image sera mise à jour automatiquement par le service Cloudinary
            System.out.println("✅ Product updated with Gemini AI analysis results");
        }
    }
    
    /**
     * Affiche une alerte simple
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Retour à la vue précédente
     */
    @FXML
    public void goBack() {
        NavigationController.showAllProducts();
    }
}
