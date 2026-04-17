package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import entities.*;
import services.ProductService;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contrôleur pour la vue des Skins
 */
public class SkinsController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> priceFilterComboBox;
    
    @FXML
    private Label resultsLabel;
    
    @FXML
    private FlowPane skinsFlowPane;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        setupPriceFilter();
        loadSkins();
    }
    
    /**
     * Configure le filtre de prix
     */
    private void setupPriceFilter() {
        if (priceFilterComboBox != null) {
            priceFilterComboBox.getItems().addAll("All Prices", "0-25 TND", "25-50 TND", "50-100 TND", "100+ TND");
            priceFilterComboBox.setValue("All Prices");
        }
    }
    
    // ==================== ACTIONS ====================
    
    /**
     * Recherche les skins selon les critères
     */
    @FXML
    public void searchSkins() {
        loadSkins();
    }
    
    /**
     * Efface les filtres
     */
    @FXML
    public void clearFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (priceFilterComboBox != null) {
            priceFilterComboBox.setValue("All Prices");
        }
        loadSkins();
    }
    
    /**
     * Affiche le formulaire d'ajout de produit
     */
    @FXML
    public void showAddProduct() {
        NavigationController.showAddProduct();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Charge et affiche les skins
     */
    private void loadSkins() {
        if (skinsFlowPane == null) return;
        
        try {
            skinsFlowPane.getChildren().clear();
            
            List<Product> allSkins = productService.getByType("skin");
            List<Product> filteredSkins = filterSkins(allSkins);
            
            updateResultsLabel(filteredSkins.size());
            
            for (Product skin : filteredSkins) {
                VBox skinCard = createSkinCard((entities.Skin) skin);
                skinsFlowPane.getChildren().add(skinCard);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading skins: " + e.getMessage());
            if (resultsLabel != null) {
                resultsLabel.setText("Error loading skins");
            }
        }
    }
    
    /**
     * Filtre les skins selon les critères
     */
    private List<Product> filterSkins(List<Product> skins) {
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String priceFilter = priceFilterComboBox != null ? priceFilterComboBox.getValue() : "All Prices";
        
        return skins.stream()
                .filter(skin -> searchText.isEmpty() || 
                          skin.getName().toLowerCase().contains(searchText) ||
                          skin.getDescription().toLowerCase().contains(searchText))
                .filter(skin -> filterByPrice(skin, priceFilter))
                .toList();
    }
    
    /**
     * Filtre par plage de prix
     */
    private boolean filterByPrice(Product product, String priceFilter) {
        if ("All Prices".equals(priceFilter)) {
            return true;
        }
        
        double price = product.getPrice();
        return switch (priceFilter) {
            case "0-25 TND" -> price >= 0 && price <= 25;
            case "25-50 TND" -> price > 25 && price <= 50;
            case "50-100 TND" -> price > 50 && price <= 100;
            case "100+ TND" -> price > 100;
            default -> true;
        };
    }
    
    /**
     * Met à jour le label de résultats
     */
    private void updateResultsLabel(int count) {
        if (resultsLabel != null) {
            resultsLabel.setText("Found " + count + " skins");
        }
    }
    
    /**
     * Crée une carte skin pour l'affichage
     */
    private VBox createSkinCard(entities.Skin skin) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(250);
        card.setPrefHeight(220);
        
        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        
        // Charger l'image
        String imagePath = skin.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Essayer de charger l'image depuis le chemin
                if (imagePath.startsWith("http")) {
                    // URL
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath, true);
                    imageView.setImage(image);
                } else {
                    // Fichier local - essayer avec le chemin absolu d'abord
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString(), true);
                        imageView.setImage(image);
                    } else {
                        // Essayer avec chemin relatif au classpath
                        try {
                            javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath, true);
                            imageView.setImage(image);
                        } catch (Exception ex) {
                            // Image par défaut si le chemin relatif ne fonctionne pas
                            javafx.scene.image.Image defaultImage = new javafx.scene.image.Image("/uploads/default_product.png", true);
                            imageView.setImage(defaultImage);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image for product " + skin.getName() + ": " + e.getMessage());
                // Image par défaut en cas d'erreur
                try {
                    javafx.scene.image.Image defaultImage = new javafx.scene.image.Image("/uploads/default_product.png", true);
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    // Si même l'image par défaut ne charge pas, cacher l'ImageView
                    imageView.setVisible(false);
                }
            }
        } else {
            // Cacher l'ImageView si pas de chemin d'image
            imageView.setVisible(false);
        }
        
        // Nom du produit
        Label nameLabel = new Label(skin.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        
        // Description (tronquée)
        String description = skin.getDescription();
        if (description.length() > 50) {
            description = description.substring(0, 50) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        
        // Prix
        Label priceLabel = new Label(skin.getPrice() + " TND");
        priceLabel.getStyleClass().add("product-price");
        
        // Statut du stock
        Label stockLabel = new Label(skin.getStockStatus());
        stockLabel.getStyleClass().add("product-stock");
        
        // Appliquer le style de couleur selon le statut
        if (skin.getStock() == 0) {
            stockLabel.getStyleClass().add("stock-out");
        } else if (skin.getStock() < 5) {
            stockLabel.getStyleClass().add("stock-low");
        } else {
            stockLabel.getStyleClass().add("stock-available");
        }
        
        // Bouton d'action - seulement Add to Cart
        HBox buttonBox = new HBox(8);
        Button cartButton = new Button("Add to Cart");
        cartButton.getStyleClass().add("button-primary");
        cartButton.setPrefWidth(120);
        cartButton.setOnAction(e -> addToCart(skin));
        
        buttonBox.getChildren().add(cartButton);
        
        card.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, stockLabel, buttonBox);
        return card;
    }
    
    /**
     * Modifie un produit
     */
    private void editProduct(Product product) {
        // Naviguer vers la nouvelle interface de modification
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
                loadSkins(); // Recharger la liste
                
                // Message de succès
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
    
    /**
     * Ajoute au panier (réduit le stock)
     */
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
                loadSkins(); // Recharger la liste
                
                // Message de succès
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
    
    /**
     * Navigue vers une vue spécifique
     */
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
