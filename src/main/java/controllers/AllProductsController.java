package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import entities.*;
import services.ProductService;
import services.CarteService;
import java.util.List;
import java.io.File;

/**
 * Contrôleur pour la vue de tous les produits
 */
public class AllProductsController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    private final CarteService carteService = CarteService.getInstance();
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> typeFilterComboBox;
    
    @FXML
    private ComboBox<String> priceFilterComboBox;
    
    @FXML
    private Label resultsLabel;
    
    @FXML
    private FlowPane productsFlowPane;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    @FXML
    public void initialize() {
        setupFilters();
        loadProducts();
    }
    
    /**
     * Force le rechargement des produits (utilisé après modification)
     */
    public void refreshProducts() {
        // Forcer le garbage collector pour libérer les ressources d'images
        System.gc();
        
        // Attendre un peu pour que le GC s'exécute
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            loadProducts();
        });
    }
    
    private void setupFilters() {
        if (typeFilterComboBox != null) {
            typeFilterComboBox.getItems().addAll("All Types", "Skin", "Merch");
            typeFilterComboBox.setValue("All Types");
        }
        
        if (priceFilterComboBox != null) {
            priceFilterComboBox.getItems().addAll("All Prices", "0-25 TND", "25-50 TND", "50-100 TND", "100+ TND");
            priceFilterComboBox.setValue("All Prices");
        }
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    public void searchProducts() {
        loadProducts();
    }
    
    @FXML
    public void clearFilters() {
        if (searchField != null) searchField.clear();
        if (typeFilterComboBox != null) typeFilterComboBox.setValue("All Types");
        if (priceFilterComboBox != null) priceFilterComboBox.setValue("All Prices");
        loadProducts();
    }
    
    @FXML
    public void showAddProduct() {
        NavigationController.showAddProduct();
    }

    @FXML
    public void showCarte() {
        NavigationController.showCarte();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    private void loadProducts() {
        if (productsFlowPane == null) return;
        
        try {
            productsFlowPane.getChildren().clear();
            
            List<Product> allProducts = productService.getAll();
            List<Product> filteredProducts = filterProducts(allProducts);
            
            updateResultsLabel(filteredProducts.size());
            
            for (Product product : filteredProducts) {
                VBox productCard = createProductCard(product);
                productsFlowPane.getChildren().add(productCard);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            if (resultsLabel != null) {
                resultsLabel.setText("Error loading products");
            }
        }
    }
    
    private List<Product> filterProducts(List<Product> products) {
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String typeFilter = typeFilterComboBox != null ? typeFilterComboBox.getValue() : "All Types";
        String priceFilter = priceFilterComboBox != null ? priceFilterComboBox.getValue() : "All Prices";
        
        return products.stream()
                .filter(p -> searchText.isEmpty() || 
                          p.getName().toLowerCase().contains(searchText) ||
                          p.getDescription().toLowerCase().contains(searchText))
                .filter(p -> filterByType(p, typeFilter))
                .filter(p -> filterByPrice(p, priceFilter))
                .toList();
    }
    
    private boolean filterByType(Product product, String typeFilter) {
        if ("All Types".equals(typeFilter)) return true;
        return product.getType().equalsIgnoreCase(typeFilter);
    }
    
    private boolean filterByPrice(Product product, String priceFilter) {
        if ("All Prices".equals(priceFilter)) return true;
        
        double price = product.getPrice();
        return switch (priceFilter) {
            case "0-25 TND" -> price >= 0 && price <= 25;
            case "25-50 TND" -> price > 25 && price <= 50;
            case "50-100 TND" -> price > 50 && price <= 100;
            case "100+ TND" -> price > 100;
            default -> true;
        };
    }
    
    private void updateResultsLabel(int count) {
        if (resultsLabel != null) {
            resultsLabel.setText("Found " + count + " products");
        }
    }
    
    private VBox createProductCard(Product product) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(250);
        card.setPrefHeight(280);

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

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        
        String description = product.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        
        Label priceLabel = new Label(product.getPrice() + " TND");
        priceLabel.getStyleClass().add("product-price");
        
        Label typeLabel = new Label(product.getType().toUpperCase());
        typeLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Ajouter les tailles pour les merch
        if (product instanceof Merch merch) {
            Label sizesLabel = new Label("Sizes: " + merch.getSizes());
            sizesLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");
            card.getChildren().add(sizesLabel);
        }
        
        Label stockLabel = new Label(product.getStockStatus());
        stockLabel.getStyleClass().add("product-stock");
        
        if (product.getStock() == 0) {
            stockLabel.getStyleClass().add("stock-out");
        } else if (product.getStock() < 5) {
            stockLabel.getStyleClass().add("stock-low");
        } else {
            stockLabel.getStyleClass().add("stock-available");
        }
        
        HBox buttonBox = new HBox(8);
        Button cartButton = new Button("Add to Cart");
        cartButton.getStyleClass().add("button-primary");
        cartButton.setPrefWidth(120);
        cartButton.setOnAction(e -> addToCart(product));
        
        buttonBox.getChildren().add(cartButton);

        card.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, typeLabel, stockLabel, buttonBox);
        return card;
    }
    
    private void editProduct(Product product) {
        NavigationController.showAddProduct();
    }
    
    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + product.getName() + "?");
        alert.setContentText("This action cannot be undone. Are you sure?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.delete(product.getId());
                loadProducts();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Product Deleted");
                successAlert.setContentText(product.getName() + " has been deleted successfully.");
                successAlert.show();
                
            } catch (Exception e) {
                System.err.println("Error deleting product: " + e.getMessage());
                
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Delete Failed");
                errorAlert.setContentText("Failed to delete product: " + e.getMessage());
                errorAlert.show();
            }
        }
    }
    
    private void addToCart(Product product) {
        try {
            // Recharger le produit depuis la base de données pour avoir le stock actuel
            Product currentProduct = productService.getById(product.getId());
            if (currentProduct == null || currentProduct.getStock() == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Out of Stock");
                alert.setHeaderText("Cannot Add to Cart");
                alert.setContentText(product.getName() + " is out of stock.");
                alert.show();
                return;
            }

            // Si c'est un merch, demander la taille
            if ("merch".equalsIgnoreCase(currentProduct.getType())) {
                Merch merch = (Merch) currentProduct;
                String sizes = merch.getSizes();

                ChoiceDialog<String> sizeDialog = new ChoiceDialog<>();
                sizeDialog.setTitle("Select Size");
                sizeDialog.setHeaderText("Select a size for " + merch.getName());
                sizeDialog.setContentText("Available sizes:");

                if (sizes != null && !sizes.isEmpty()) {
                    String[] sizeArray = sizes.split(",");
                    for (String size : sizeArray) {
                        sizeDialog.getItems().add(size.trim());
                    }
                    if (!sizeDialog.getItems().isEmpty()) {
                        sizeDialog.setSelectedItem(sizeDialog.getItems().get(0));
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No Sizes Available");
                    alert.setHeaderText("Cannot Add to Cart");
                    alert.setContentText("No sizes available for this product.");
                    alert.show();
                    return;
                }

                sizeDialog.showAndWait().ifPresent(selectedSize -> {
                    try {
                        carteService.add(product, 1, selectedSize);
                        loadProducts();

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Added to Cart");
                        successAlert.setContentText(merch.getName() + " (Size: " + selectedSize + ") has been added to cart.");
                        successAlert.show();
                    } catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Add to Cart Failed");
                        errorAlert.setContentText("Failed to add product to cart: " + e.getMessage());
                        errorAlert.show();
                    }
                });
            } else {
                // Pour les skins et autres produits sans taille
                carteService.add(product, 1);
                loadProducts();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Added to Cart");
                successAlert.setContentText(product.getName() + " has been added to cart.");
                successAlert.show();
            }
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Add to Cart Failed");
            errorAlert.setContentText("Failed to add product to cart: " + e.getMessage());
            errorAlert.show();
        }
    }
}
