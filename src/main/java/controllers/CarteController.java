package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import entities.CarteItem;
import entities.Product;
import services.CarteService;
import java.io.File;

/**
 * Contrôleur pour la vue du panier
 */
public class CarteController {
    
    // ==================== SERVICES ====================
    private final CarteService carteService = CarteService.getInstance();
    
    // ==================== COMPOSANTS FXML ====================
    @FXML
    private VBox cartItemsVBox;
    
    @FXML
    private Label totalItemsLabel;
    
    @FXML
    private Label totalPriceLabel;
    
    @FXML
    private Label emptyCartLabel;
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    
    @FXML
    public void initialize() {
        loadCart();
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    public void goBack() {
        NavigationController.showAllProducts();
    }
    
    @FXML
    public void clearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear all items?");
        alert.setContentText("This will remove all items from your cart. Are you sure?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            carteService.clear();
            loadCart();
        }
    }
    
    @FXML
    public void checkout() {
        if (carteService.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Cart");
            alert.setHeaderText("Cannot Checkout");
            alert.setContentText("Your cart is empty. Add items before checkout.");
            alert.show();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Checkout");
        alert.setHeaderText("Order Placed Successfully!");
        alert.setContentText("Thank you for your order. Total: " + String.format("%.2f TND", carteService.getTotalPrice()));
        alert.show();
        
        carteService.clear();
        loadCart();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    private void loadCart() {
        if (cartItemsVBox == null) return;
        
        cartItemsVBox.getChildren().clear();
        
        if (carteService.isEmpty()) {
            if (emptyCartLabel != null) {
                emptyCartLabel.setVisible(true);
                emptyCartLabel.setManaged(true);
            }
            updateSummary();
            return;
        }
        
        if (emptyCartLabel != null) {
            emptyCartLabel.setVisible(false);
            emptyCartLabel.setManaged(false);
        }
        
        for (CarteItem item : carteService.getAll()) {
            HBox itemRow = createCartItemRow(item);
            cartItemsVBox.getChildren().add(itemRow);
        }
        
        updateSummary();
    }
    
    private HBox createCartItemRow(CarteItem item) {
        HBox row = new HBox(15);
        row.getStyleClass().add("product-card");
        row.setPrefWidth(860);
        row.setStyle("-fx-padding: 15;");
        
        Product product = item.getProduct();
        
        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
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
                    }
                }
            } catch (Exception e) {
                // Image par défaut
            }
        }
        
        // Informations du produit
        VBox infoBox = new VBox(5);
        infoBox.setPrefWidth(300);
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%.2f TND", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #06B6D4;");

        // Afficher la taille si c'est un merch
        if (item.getSize() != null && !item.getSize().isEmpty()) {
            Label sizeLabel = new Label("Size: " + item.getSize());
            sizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
            infoBox.getChildren().addAll(nameLabel, priceLabel, sizeLabel);
        } else {
            infoBox.getChildren().addAll(nameLabel, priceLabel);
        }
        
        // Contrôles de quantité
        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button minusButton = new Button("-");
        minusButton.getStyleClass().add("button-secondary");
        minusButton.setPrefWidth(40);
        minusButton.setOnAction(e -> {
            item.decrementQuantity();
            carteService.updateQuantity(item, item.getQuantity());
            loadCart();
        });
        
        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        
        Button plusButton = new Button("+");
        plusButton.getStyleClass().add("button-secondary");
        plusButton.setPrefWidth(40);
        plusButton.setOnAction(e -> {
            item.incrementQuantity();
            carteService.updateQuantity(item, item.getQuantity());
            loadCart();
        });
        
        quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);
        
        // Prix total
        Label totalLabel = new Label(String.format("%.2f TND", item.getTotalPrice()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #06B6D4;");
        
        // Bouton supprimer
        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("button-danger");
        removeButton.setPrefWidth(80);
        removeButton.setOnAction(e -> {
            carteService.remove(item);
            loadCart();
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        row.getChildren().addAll(imageView, infoBox, spacer, quantityBox, totalLabel, removeButton);
        return row;
    }
    
    private void updateSummary() {
        if (totalItemsLabel != null) {
            totalItemsLabel.setText(String.valueOf(carteService.getTotalItems()));
        }
        
        if (totalPriceLabel != null) {
            totalPriceLabel.setText(String.format("%.2f TND", carteService.getTotalPrice()));
        }
    }
}
