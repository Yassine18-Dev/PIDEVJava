package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import entities.*;
import services.ProductService;
import java.io.IOException;
import java.util.List;

/**
 * Contrôleur pour le tableau de bord
 */
public class DashboardController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private Label totalProductsLabel;
    
    @FXML
    private Label totalSkinsLabel;
    
    @FXML
    private Label totalMerchLabel;
    
    @FXML
    private FlowPane recentProductsPane;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        updateDashboard();
        loadRecentProducts();
    }
    
    // ==================== ACTIONS ====================
    
    /**
     * Affiche la vue de tous les produits
     */
    @FXML
    public void showAllProducts() {
        NavigationController.showAllProducts();
    }
    
    /**
     * Affiche la vue des skins
     */
    @FXML
    public void showSkins() {
        NavigationController.showSkins();
    }
    
    /**
     * Affiche la vue des merch
     */
    @FXML
    public void showMerch() {
        NavigationController.showMerch();
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
     * Rafraîchit le dashboard
     */
    @FXML
    public void refreshDashboard() {
        updateDashboard();
        loadRecentProducts();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Met à jour les statistiques du dashboard
     */
    private void updateDashboard() {
        try {
            List<Product> allProducts = productService.getAll();
            List<Product> skins = productService.getByType("skin");
            List<Product> merch = productService.getByType("merch");
            
            if (totalProductsLabel != null) {
                totalProductsLabel.setText(String.valueOf(allProducts.size()));
            }
            if (totalSkinsLabel != null) {
                totalSkinsLabel.setText(String.valueOf(skins.size()));
            }
            if (totalMerchLabel != null) {
                totalMerchLabel.setText(String.valueOf(merch.size()));
            }
            
        } catch (Exception e) {
            System.err.println("Error updating dashboard: " + e.getMessage());
            if (totalProductsLabel != null) totalProductsLabel.setText("Error");
            if (totalSkinsLabel != null) totalSkinsLabel.setText("Error");
            if (totalMerchLabel != null) totalMerchLabel.setText("Error");
        }
    }
    
    /**
     * Charge les produits récents
     */
    private void loadRecentProducts() {
        if (recentProductsPane == null) return;
        
        try {
            recentProductsPane.getChildren().clear();
            
            List<Product> allProducts = productService.getAll();
            int recentCount = Math.min(allProducts.size(), 6); // Afficher max 6 produits récents
            
            for (int i = allProducts.size() - recentCount; i < allProducts.size(); i++) {
                Product product = allProducts.get(i);
                VBox productCard = createProductCard(product);
                recentProductsPane.getChildren().add(productCard);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading recent products: " + e.getMessage());
        }
    }
    
    /**
     * Crée une carte produit pour l'affichage
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(200);
        
        // Nom du produit
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        
        // Prix
        Label priceLabel = new Label(product.getPrice() + " TND");
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
        
        // Type
        Label typeLabel = new Label(product.getType().toUpperCase());
        typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7F8C8D;");
        
        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, typeLabel);
        return card;
    }
    
    /**
     * Navigue vers une vue spécifique
     */
    private void navigateToView(String fxmlPath) {
        try {
            // Obtenir le contrôleur principal
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/gui/MainLayout.fxml"));
            mainLoader.load();
            MainController mainController = mainLoader.getController();
            
            // Utiliser la méthode de navigation du contrôleur principal
            if (mainController != null) {
                if (fxmlPath.contains("SkinsView")) {
                    mainController.showSkins();
                } else if (fxmlPath.contains("MerchView")) {
                    mainController.showMerch();
                } else if (fxmlPath.contains("ProductForm")) {
                    mainController.showAddProduct();
                } else if (fxmlPath.contains("AllProductsView")) {
                    mainController.showAllProducts();
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error navigating to view: " + e.getMessage());
        }
    }
}
