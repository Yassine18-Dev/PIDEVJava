package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import entities.*;
import services.ProductService;
import java.io.File;

/**
 * Contrôleur pour la modification de produit
 */
public class ProductEditController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    
    // ==================== ÉTAT ====================
    private Product editingProduct;
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private TextField stockField;
    
    @FXML
    private RadioButton skinRadioButton;
    
    @FXML
    private RadioButton merchRadioButton;
    
    @FXML
    private VBox sizesSection;
    
    @FXML
    private CheckBox sizeSCheckBox;
    
    @FXML
    private CheckBox sizeMCheckBox;
    
    @FXML
    private CheckBox sizeLCheckBox;
    
    @FXML
    private CheckBox sizeXLCheckBox;
    
    @FXML
    private ImageView productImageView;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label statusLabel;
    
    // ==================== MÉTHODES STATIQUES ====================
    
    /**
     * Stocke le produit à modifier pour le récupérer plus tard
     */
    private static Product staticEditingProduct = null;
    
    /**
     * Stocke le produit à modifier pour le récupérer plus tard
     */
    public static void setEditingProduct(Product product) {
        staticEditingProduct = product;
    }
    
    /**
     * Récupère le produit stocké
     */
    public static Product getEditingProduct() {
        return staticEditingProduct;
    }
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        // Configurer les listeners pour la validation en temps réel
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        priceField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        stockField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        
        // Configurer les listeners pour les boutons radio
        skinRadioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateSizesVisibility();
            validateForm();
        });
        
        merchRadioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateSizesVisibility();
            validateForm();
        });
        
        // Charger les données du produit si disponible
        loadProductData();
    }
    
    // ==================== MÉTHODES PUBLIC ====================
    
    /**
     * Définit le produit à modifier
     */
    public void setProduct(Product product) {
        this.editingProduct = product;
        
        if (titleLabel != null) {
            titleLabel.setText("Edit Product: " + product.getName());
        }
        
        loadProductData();
    }
    
    /**
     * Charge les données du produit
     */
    private void loadProductData() {
        // Utiliser le produit statique d'abord, sinon le produit d'instance
        Product product = staticEditingProduct != null ? staticEditingProduct : editingProduct;
        
        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            stockField.setText(String.valueOf(product.getStock()));
            descriptionField.setText(product.getDescription());
            
            // Configurer le type de produit
            String productType = product.getType();
            if (productType != null && productType.equals("skin")) {
                skinRadioButton.setSelected(true);
            } else if (productType != null && productType.equals("merch")) {
                merchRadioButton.setSelected(true);
                
                // Configurer les tailles pour les merch
                if (product instanceof Merch merch) {
                    String sizes = merch.getSizes();
                    if (sizes != null && !sizes.isEmpty()) {
                        String[] sizeArray = sizes.split(",");
                        sizeSCheckBox.setSelected(containsSize(sizeArray, "S"));
                        sizeMCheckBox.setSelected(containsSize(sizeArray, "M"));
                        sizeLCheckBox.setSelected(containsSize(sizeArray, "L"));
                        sizeXLCheckBox.setSelected(containsSize(sizeArray, "XL"));
                    }
                }
            }
            
            updateSizesVisibility();
            loadProductImage();
            
            // Mettre à jour le produit d'instance si nécessaire
            if (editingProduct == null && staticEditingProduct != null) {
                editingProduct = staticEditingProduct;
            }
        }
    }
    
    // ==================== ACTIONS ====================
    
    /**
     * Sauvegarde les modifications
     */
    @FXML
    public void saveProduct() {
        if (!validateForm()) {
            updateStatus("Please correct errors before saving");
            return;
        }
        
        try {
            // Utiliser le produit statique d'abord, sinon le produit d'instance
            Product product = staticEditingProduct != null ? staticEditingProduct : editingProduct;
            
            if (product != null) {
                System.out.println("DEBUG: product = " + product.getName());
                
                String name = nameField.getText();
                String priceText = priceField.getText();
                String stockText = stockField.getText();
                String description = descriptionField.getText();
                
                System.out.println("DEBUG: name = " + name);
                System.out.println("DEBUG: priceText = " + priceText);
                System.out.println("DEBUG: stockText = " + stockText);
                System.out.println("DEBUG: description = " + description);
                
                // Validation simple
                if (name == null || name.trim().isEmpty()) {
                    updateStatus("Product name is required");
                    return;
                }
                
                if (priceText == null || priceText.trim().isEmpty()) {
                    updateStatus("Price is required");
                    return;
                }
                
                if (stockText == null || stockText.trim().isEmpty()) {
                    updateStatus("Stock is required");
                    return;
                }
                
                double price = Double.parseDouble(priceText);
                int stock = Integer.parseInt(stockText);
                
                System.out.println("DEBUG: parsed price = " + price);
                System.out.println("DEBUG: parsed stock = " + stock);
                
                // Mettre à jour les données
                product.setName(name);
                product.setPrice(price);
                product.setStock(stock);
                product.setDescription(description);
                
                System.out.println("DEBUG: Product updated in memory");
                
                // Configurer les tailles pour les merch
                if (merchRadioButton.isSelected() && product instanceof Merch merch) {
                    String sizes = "";
                    if (sizeSCheckBox.isSelected()) sizes += "S";
                    if (sizeMCheckBox.isSelected()) sizes += (sizes.isEmpty() ? "" : ",") + "M";
                    if (sizeLCheckBox.isSelected()) sizes += (sizes.isEmpty() ? "" : ",") + "L";
                    if (sizeXLCheckBox.isSelected()) sizes += (sizes.isEmpty() ? "" : ",") + "XL";
                    merch.setSizes(sizes);
                    System.out.println("DEBUG: Merch sizes set to: " + sizes);
                }
                
                // Sauvegarder
                System.out.println("DEBUG: Calling productService.update()");
                productService.update(product);
                System.out.println("DEBUG: productService.update() completed");
                
                updateStatus("Product updated successfully!");
                
                // Retour automatique
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1500);
                        goBack();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } else {
                System.out.println("DEBUG: product is null");
                updateStatus("Error: No product to save");
            }
        } catch (Exception e) {
            updateStatus("Error saving product: " + e.getMessage());
        }
    }
    
    /**
     * Supprime le produit
     */
    @FXML
    public void deleteProduct() {
        if (editingProduct == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + editingProduct.getName() + "?");
        alert.setContentText("This action cannot be undone. Are you sure?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.delete(editingProduct.getId());
                updateStatus("Product deleted successfully!");
                
                // Retour automatique
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1500);
                        goBack();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (Exception e) {
                updateStatus("Error deleting product: " + e.getMessage());
            }
        }
    }
    
    /**
     * Annule l'édition
     */
    @FXML
    public void cancelEdit() {
        goBack();
    }
    
    /**
     * Change l'image du produit
     */
    @FXML
    public void changeImage() {
        updateStatus("Use 'Choose File' to select an image from your computer");
    }
    
    /**
     * Permet de choisir un fichier image depuis le PC
     */
    @FXML
    public void chooseImageFile() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Choose Product Image");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
            );
            
            java.io.File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                // Copier le fichier vers le répertoire uploads
                String uploadsDir = "uploads/";
                java.io.File uploadsFolder = new java.io.File(uploadsDir);
                if (!uploadsFolder.exists()) {
                    uploadsFolder.mkdirs();
                }
                
                String fileName = selectedFile.getName();
                java.io.File destFile = new java.io.File(uploadsDir + fileName);
                
                // Copier le fichier
                java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                // Mettre à jour l'image du produit
                String imagePath = uploadsDir + fileName;
                if (editingProduct != null) {
                    editingProduct.setImage(imagePath);
                }
                
                // Mettre à jour l'aperçu
                updateImagePreview(imagePath);
                
                updateStatus("Image selected: " + fileName);
            }
        } catch (Exception e) {
            updateStatus("Error selecting image: " + e.getMessage());
        }
    }
    
    /**
     * Retour à la vue précédente
     */
    private void goBack() {
        NavigationController.showAllProducts();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Affiche la section des tailles pour les merch
     */
    private void updateSizesVisibility() {
        if (sizesSection != null) {
            boolean isMerch = merchRadioButton != null && merchRadioButton.isSelected();
            sizesSection.setVisible(isMerch);
            sizesSection.setManaged(isMerch);
        }
    }
    
    /**
     * Charge l'image du produit
     */
    private void loadProductImage() {
        if (productImageView != null && editingProduct != null) {
            String imagePath = editingProduct.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    if (imagePath.startsWith("http")) {
                        Image image = new Image(imagePath, true);
                        productImageView.setImage(image);
                    } else {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString(), true);
                            productImageView.setImage(image);
                        }
                    }
                } catch (Exception e) {
                    // Image par défaut si erreur
                    try {
                        Image defaultImage = new Image("/uploads/default_product.png", true);
                        productImageView.setImage(defaultImage);
                    } catch (Exception ex) {
                        productImageView.setImage(null);
                    }
                }
            }
        }
    }
    
    /**
     * Met à jour l'aperçu de l'image
     */
    private void updateImagePreview(String imagePath) {
        if (productImageView != null) {
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    if (imagePath.startsWith("http")) {
                        Image image = new Image(imagePath, true);
                        productImageView.setImage(image);
                    } else {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString(), true);
                            productImageView.setImage(image);
                        }
                    }
                } else {
                    // Image par défaut
                    try {
                        Image defaultImage = new Image("/uploads/default_product.png", true);
                        productImageView.setImage(defaultImage);
                    } catch (Exception e) {
                        productImageView.setImage(null);
                    }
                }
            } catch (Exception e) {
                // Image par défaut si erreur
                try {
                    Image defaultImage = new Image("/uploads/default_product.png", true);
                    productImageView.setImage(defaultImage);
                } catch (Exception ex) {
                    productImageView.setImage(null);
                }
            }
        }
    }
    
    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        String name = nameField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        
        // Validation simple
        if (name == null || name.trim().isEmpty()) {
            updateStatus("Product name is required");
            return false;
        }
        
        if (priceText == null || priceText.trim().isEmpty()) {
            updateStatus("Price is required");
            return false;
        }
        
        if (stockText == null || stockText.trim().isEmpty()) {
            updateStatus("Stock is required");
            return false;
        }
        
        // Validation des nombres
        try {
            Double.parseDouble(priceText);
            Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            updateStatus("Invalid numbers");
            return false;
        }
        
        updateStatus("");
        return true;
    }
    
    /**
     * Met à jour le statut
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Vérifie si une chaîne contient une valeur
     */
    private boolean containsSize(String[] array, String value) {
        for (String item : array) {
            if (item.trim().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
