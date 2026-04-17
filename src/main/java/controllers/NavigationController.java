package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import services.ProductService;
import entities.Product;

/**
 * Gestionnaire centralisé de la navigation
 */
public class NavigationController {
    
    private static MainController mainController;
    
    /**
     * Définit l'instance du contrôleur principal
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
    }
    
    /**
     * Obtient le contrôleur principal
     */
    public static MainController getMainController() {
        return mainController;
    }
    
    /**
     * Affiche le dashboard
     */
    public static void showDashboard() {
        if (mainController != null) {
            mainController.showDashboard();
        }
    }
    
    /**
     * Affiche les skins
     */
    public static void showSkins() {
        if (mainController != null) {
            mainController.showSkins();
        }
    }
    
    /**
     * Affiche les merch
     */
    public static void showMerch() {
        if (mainController != null) {
            mainController.showMerch();
        }
    }
    
    /**
     * Affiche le formulaire d'ajout de produit
     */
    public static void showAddProduct() {
        if (mainController != null) {
            mainController.showAddProduct();
        }
    }
    
    /**
     * Affiche tous les produits
     */
    public static void showAllProducts() {
        if (mainController != null) {
            mainController.showAllProducts();
        }
    }
    
    /**
     * Affiche la vue de modification de produit
     */
    public static void showProductEdit(Product product) {
        if (mainController != null) {
            mainController.loadView("/gui/ProductEditView.fxml", "Edit product: " + product.getName());
            
            // Stocker le produit pour le récupérer plus tard
            ProductEditController.setEditingProduct(product);
            System.out.println("DEBUG: Product stored for ProductEditController: " + product.getName());
        }
    }
    
    /**
     * Affiche la vue de gestion des produits
     */
    public static void showProductManagement() {
        if (mainController != null) {
            mainController.loadView("/gui/ProductManagementView.fxml", "Product management view loaded");
        }
    }
}
