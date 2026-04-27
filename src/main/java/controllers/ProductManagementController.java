package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import entities.*;
import services.ProductService;
import java.io.File;
import java.util.List;

/**
 * Contrôleur pour la gestion complète des produits
 */
public class ProductManagementController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();

    // ==================== COMPOSANTS FXML ====================
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> priceFilter;

    @FXML
    private GridPane productsGrid;

    @FXML
    private Label resultsLabel;

    @FXML
    private Label statusLabel;

    // Promo Codes Section
    @FXML
    private VBox promoCodesSection;

    @FXML
    private TextField promoCodeField;

    @FXML
    private ComboBox<String> promoTypeCombo;

    @FXML
    private TextField promoValueField;

    @FXML
    private TextField promoMaxUsageField;

    @FXML
    private DatePicker promoExpirationDatePicker;

    @FXML
    private TableView<PromoCode> promoCodesTable;

    @FXML
    private TableColumn<PromoCode, String> codeColumn;

    @FXML
    private TableColumn<PromoCode, String> typeColumn;

    @FXML
    private TableColumn<PromoCode, Double> valueColumn;

    @FXML
    private TableColumn<PromoCode, Integer> maxUsageColumn;

    @FXML
    private TableColumn<PromoCode, Integer> currentUsageColumn;

    @FXML
    private TableColumn<PromoCode, String> expirationColumn;

    @FXML
    private TableColumn<PromoCode, Boolean> activeColumn;

    @FXML
    private TableColumn<PromoCode, Void> actionsColumn;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        loadProducts();
    }
    
    // ==================== ACTIONS ====================
    
    /**
     * Charge les produits
     */
    private void loadProducts() {
        try {
            List<Product> allProducts = productService.getAll();
            updateProductsGrid(allProducts);
            updateResultsLabel(allProducts.size());
        } catch (Exception e) {
            updateStatus("Error loading products: " + e.getMessage());
        }
    }
    
    /**
     * Recherche les produits
     */
    @FXML
    public void searchProducts() {
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String typeFilterValue = this.typeFilter != null ? this.typeFilter.getValue() : "All Types";
        String priceFilterValue = this.priceFilter != null ? this.priceFilter.getValue() : "All Prices";
        
        try {
            List<Product> allProducts = productService.getAll();
            List<Product> filteredProducts = allProducts.stream()
                .filter(product -> {
                    // Filtre par nom
                    if (!searchText.isEmpty() && 
                        !product.getName().toLowerCase().contains(searchText)) {
                        return false;
                    }
                    
                    // Filtre par type
                    if (!typeFilterValue.equals("All Types") && 
                        !product.getType().equals(typeFilterValue.toLowerCase())) {
                        return false;
                    }
                    
                    // Filtre par prix
                    if (!priceFilterValue.equals("All Prices")) {
                        double price = product.getPrice();
                        if (priceFilterValue.equals("0-25 TND") && (price < 0 || price > 25)) return false;
                        if (priceFilterValue.equals("25-50 TND") && (price < 25 || price > 50)) return false;
                        if (priceFilterValue.equals("50-100 TND") && (price < 50 || price > 100)) return false;
                        if (priceFilterValue.equals("100+ TND") && price <= 100) return false;
                    }
                    
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
            
            updateProductsGrid(filteredProducts);
            updateResultsLabel(filteredProducts.size());
        } catch (Exception e) {
            updateStatus("Error searching products: " + e.getMessage());
        }
    }
    
    /**
     * Efface les filtres
     */
    @FXML
    public void clearFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (typeFilter != null) {
            typeFilter.setValue("All Types");
        }
        if (priceFilter != null) {
            priceFilter.setValue("All Prices");
        }
        
        loadProducts();
    }
    
    /**
     * Rafraîchit les produits
     */
    @FXML
    public void refreshProducts() {
        loadProducts();
    }
    
    /**
     * Affiche le formulaire d'ajout de produit
     */
    @FXML
    public void showAddProduct() {
        NavigationController.showAddProduct();
    }

    @FXML
    public void showCarte() {
        NavigationController.showCarte();
    }
    
    /**
     * Modifie un produit
     */
    private void editProduct(Product product) {
        NavigationController.showProductEdit(product);
    }
    
    /**
     * Supprime un produit
     */
    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + product.getName() + "?");
        alert.setContentText("This action cannot be undone. Are you sure?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.delete(product.getId());
                updateStatus("Product deleted successfully!");
                loadProducts();
            } catch (Exception e) {
                updateStatus("Error deleting product: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ajoute au panier
     */
    private void addToCart(Product product) {
        updateStatus("Added " + product.getName() + " to cart");
    }
    
    /**
     * Retour à la vue précédente
     */
    @FXML
    public void goBack() {
        NavigationController.showAllProducts();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Crée une carte produit pour la Grid View
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(280);
        card.setPrefHeight(200);
        
        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        // Charger l'image
        String imagePath = product.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (imagePath.startsWith("http")) {
                    Image image = new Image(imagePath, true);
                    imageView.setImage(image);
                } else {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString(), true);
                        imageView.setImage(image);
                    } else {
                        try {
                            Image defaultImage = new Image("/uploads/default_product.png", true);
                            imageView.setImage(defaultImage);
                        } catch (Exception e) {
                            imageView.setImage(null);
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    Image defaultImage = new Image("/uploads/default_product.png", true);
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    imageView.setImage(null);
                }
            }
        } else {
            try {
                Image defaultImage = new Image("/uploads/default_product.png", true);
                imageView.setImage(defaultImage);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }
        
        // Nom du produit
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        
        // Description (tronquée)
        String description = product.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        
        // Prix
        Label priceLabel = new Label(String.format("%.2f TND", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        
        // Statut du stock
        Label stockLabel = new Label(product.getStockStatus());
        stockLabel.getStyleClass().add("product-stock");
        
        // Appliquer le style de couleur selon le statut
        if (product.getStock() == 0) {
            stockLabel.getStyleClass().add("stock-out");
        } else if (product.getStock() < 5) {
            stockLabel.getStyleClass().add("stock-low");
        } else {
            stockLabel.getStyleClass().add("stock-available");
        }
        
        // Type de produit
        Label typeLabel = new Label(product.getType());
        typeLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Boutons d'action
        HBox buttonBox = new HBox(8);
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("button-secondary");
        editButton.setPrefWidth(80);
        editButton.setOnAction(e -> editProduct(product));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setPrefWidth(80);
        deleteButton.setOnAction(e -> deleteProduct(product));

        buttonBox.getChildren().addAll(editButton, deleteButton);
        
        card.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, stockLabel, typeLabel, buttonBox);
        return card;
    }
    
    /**
     * Met à jour la Grid View avec les produits
     */
    private void updateProductsGrid(List<Product> products) {
        if (productsGrid != null) {
            productsGrid.getChildren().clear();
            
            int row = 0;
            int col = 0;
            int maxCols = 3;
            
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                productsGrid.add(productCard, col, row);
                
                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }
        }
    }
    
    /**
     * Met à jour le label de résultats
     */
    private void updateResultsLabel(int count) {
        if (resultsLabel != null) {
            resultsLabel.setText("Found " + count + " products");
        }
    }
    
    /**
     * Met à jour le statut
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
