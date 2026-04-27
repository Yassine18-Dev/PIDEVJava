package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import entities.*;
import services.ProductService;
import java.io.IOException;
import java.util.List;

/**
 * Contrôleur principal pour la navigation et la gestion des vues
 */
public class MainController {
    
    // ==================== SERVICES ====================
    private final ProductService productService = new ProductService();
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private AnchorPane contentArea;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label totalProductsLabel;
    
    @FXML
    private Label totalSkinsLabel;
    
    @FXML
    private Label totalMerchLabel;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        // Enregistrer ce contrôleur pour la navigation centralisée
        NavigationController.setMainController(this);
        updateDashboard();
        showDashboard();
    }
    
    // ==================== NAVIGATION ====================
    
    /**
     * Affiche le tableau de bord
     */
    @FXML
    public void showDashboard() {
        loadView("/gui/DashboardView.fxml", "Dashboard loaded");
        updateDashboard();
    }
    
    /**
     * Affiche la vue des Skins
     */
    @FXML
    public void showSkins() {
        loadView("/gui/SkinsView.fxml", "Skins view loaded");
    }
    
    /**
     * Affiche la vue des Merch
     */
    @FXML
    public void showMerch() {
        loadView("/gui/MerchView.fxml", "Merch view loaded");
    }
    
    /**
     * Affiche le formulaire d'ajout de produit
     */
    @FXML
    public void showAddProduct() {
        loadView("/gui/ProductForm.fxml", "Add product form loaded");
    }
    
    /**
     * Affiche tous les produits
     */
    @FXML
    public void showAllProducts() {
        loadView("/gui/AllProductsView.fxml", "All products view loaded");
    }
    
    /**
     * Affiche la vue de gestion des produits
     */
    @FXML
    public void showProductManagement() {
        loadView("/gui/ProductManagementView.fxml", "Product management view loaded");
    }
    
    /**
     * Affiche la vue du panier
     */
    @FXML
    public void showCarte() {
        NavigationController.showCarte();
    }
    
    /**
     * Affiche la vue de gestion des codes promo
     */
    @FXML
    public void showPromoCodes() {
        NavigationController.showPromoCodes();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Charge une vue dans la zone de contenu
     */
    public void loadView(String fxmlPath, String statusMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
            // Configurer le view pour remplir tout l'espace
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            
            updateStatus(statusMessage);
            
        } catch (IOException e) {
            System.err.println("Error loading view " + fxmlPath + ": " + e.getMessage());
            updateStatus("Error loading view");
        }
    }
    
    /**
     * Met à jour le statut dans la barre supérieure
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
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
     * Rafraîchit les données du dashboard
     */
    public void refreshDashboard() {
        updateDashboard();
    }
}