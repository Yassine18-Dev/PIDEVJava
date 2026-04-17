package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import entities.*;
import services.ProductService;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contrôleur pour la vue des Merch
 */
public class MerchController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> priceFilterComboBox;
    
    @FXML
    private ComboBox<String> sizeFilterComboBox;
    
    @FXML
    private Label resultsLabel;
    
    @FXML
    private FlowPane merchFlowPane;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    @FXML
    public void initialize() {
        setupFilters();
        loadMerch();
    }
    
    private void setupFilters() {
        if (priceFilterComboBox != null) {
            priceFilterComboBox.getItems().addAll("All Prices", "0-25 TND", "25-50 TND", "50-100 TND", "100+ TND");
            priceFilterComboBox.setValue("All Prices");
        }
        
        if (sizeFilterComboBox != null) {
            sizeFilterComboBox.getItems().addAll("All Sizes", "S", "M", "L", "XL");
            sizeFilterComboBox.setValue("All Sizes");
        }
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    public void searchMerch() {
        loadMerch();
    }
    
    @FXML
    public void clearFilters() {
        if (searchField != null) searchField.clear();
        if (priceFilterComboBox != null) priceFilterComboBox.setValue("All Prices");
        if (sizeFilterComboBox != null) sizeFilterComboBox.setValue("All Sizes");
        loadMerch();
    }
    
    @FXML
    public void showAddProduct() {
        NavigationController.showAddProduct();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    private void loadMerch() {
        if (merchFlowPane == null) return;
        
        try {
            merchFlowPane.getChildren().clear();
            
            List<Product> allMerch = productService.getByType("merch");
            List<Product> filteredMerch = filterMerch(allMerch);
            
            updateResultsLabel(filteredMerch.size());
            
            for (Product merch : filteredMerch) {
                VBox merchCard = createProductCard(merch);
                merchFlowPane.getChildren().add(merchCard);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading merch: " + e.getMessage());
            if (resultsLabel != null) {
                resultsLabel.setText("Error loading merch");
            }
        }
    }
    
    private List<Product> filterMerch(List<Product> merch) {
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String priceFilter = priceFilterComboBox != null ? priceFilterComboBox.getValue() : "All Prices";
        String sizeFilter = sizeFilterComboBox != null ? sizeFilterComboBox.getValue() : "All Sizes";
        
        return merch.stream()
                .filter(m -> searchText.isEmpty() || 
                          m.getName().toLowerCase().contains(searchText) ||
                          m.getDescription().toLowerCase().contains(searchText))
                .filter(m -> filterByPrice(m, priceFilter))
                .filter(m -> filterBySize((Merch) m, sizeFilter))
                .toList();
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
    
    private boolean filterBySize(Merch merch, String sizeFilter) {
        if ("All Sizes".equals(sizeFilter)) return true;
        
        String sizes = merch.getSizes();
        return sizes != null && sizes.contains(sizeFilter);
    }
    
    private void updateResultsLabel(int count) {
        if (resultsLabel != null) {
            resultsLabel.setText("Found " + count + " merch items");
        }
    }
    
    private VBox createProductCard(Product product) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(250);
        card.setPrefHeight(240);
        
        Merch merch = (Merch) product;
        
        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        
        // Charger l'image
        String imagePath = merch.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Essayer de charger l'image depuis le chemin
                if (imagePath.startsWith("http")) {
                    // URL
                    Image image = new Image(imagePath, true);
                    imageView.setImage(image);
                } else {
                    // Fichier local - essayer avec le chemin absolu d'abord
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString(), true);
                        imageView.setImage(image);
                    } else {
                        // Essayer avec chemin relatif au classpath
                        try {
                            Image image = new Image(imagePath, true);
                            imageView.setImage(image);
                        } catch (Exception ex) {
                            // Image par défaut si le chemin relatif ne fonctionne pas
                            try {
                                Image defaultImage = new Image("/uploads/default_product.png", true);
                                imageView.setImage(defaultImage);
                            } catch (Exception e) {
                                // Cacher l'ImageView si erreur
                                imageView.setVisible(false);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image for product " + merch.getName() + ": " + e.getMessage());
                // Image par défaut en cas d'erreur
                try {
                    Image defaultImage = new Image("/uploads/default_product.png", true);
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    // Cacher l'ImageView si erreur
                    imageView.setVisible(false);
                }
            }
        } else {
            // Cacher l'ImageView si pas de chemin d'image
            imageView.setVisible(false);
        }
        
        // Nom du produit
        Label nameLabel = new Label(merch.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        
        // Description (tronquée)
        String description = merch.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        
        // Prix
        Label priceLabel = new Label(merch.getPrice() + " TND");
        priceLabel.getStyleClass().add("product-price");
        
        // Tailles
        Label sizesLabel = new Label("Sizes: " + merch.getSizes());
        sizesLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Statut du stock
        Label stockLabel = new Label(merch.getStockStatus());
        stockLabel.getStyleClass().add("product-stock");
        
        // Appliquer le style de couleur selon le statut
        if (merch.getStock() == 0) {
            stockLabel.getStyleClass().add("stock-out");
        } else if (merch.getStock() < 5) {
            stockLabel.getStyleClass().add("stock-low");
        } else {
            stockLabel.getStyleClass().add("stock-available");
        }
        
        // Bouton d'action - seulement Add to Cart
        HBox buttonBox = new HBox(8);
        Button cartButton = new Button("Add to Cart");
        cartButton.getStyleClass().add("button-primary");
        cartButton.setPrefWidth(120);
        cartButton.setOnAction(e -> addToCart(merch));
        
        buttonBox.getChildren().add(cartButton);
        
        card.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, sizesLabel, stockLabel, buttonBox);
        return card;
    }
    
    private void editProduct(Product product) {
        navigateToView("/gui/ProductForm.fxml");
    }
    
    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + product.getName() + "?");
        alert.setContentText("This action cannot be undone. Are you sure?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.delete(product.getId());
                loadMerch();
                
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
        if (product.getStock() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Out of Stock");
            alert.setHeaderText("Cannot Add to Cart");
            alert.setContentText(product.getName() + " is out of stock.");
            alert.show();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add to Cart");
        alert.setHeaderText("Add " + product.getName() + " to cart?");
        alert.setContentText("This will reduce stock by 1 unit.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.reduceStock(product.getId(), 1);
                loadMerch();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Added to Cart");
                successAlert.setContentText(product.getName() + " has been added to cart.");
                successAlert.show();
                
            } catch (Exception e) {
                System.err.println("Error adding to cart: " + e.getMessage());
                
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Add to Cart Failed");
                errorAlert.setContentText("Failed to add product to cart: " + e.getMessage());
                errorAlert.show();
            }
        }
    }
    
    private void navigateToView(String fxmlPath) {
        try {
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/gui/MainLayout.fxml"));
            mainLoader.load();
            MainController mainController = mainLoader.getController();
            
            if (mainController != null) {
                mainController.loadView(fxmlPath, "Product form loaded");
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to view: " + e.getMessage());
        }
    }
}
