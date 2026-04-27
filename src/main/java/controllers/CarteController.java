package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import entities.CarteItem;
import entities.Product;
import entities.PromoCode;
import entities.LoyaltyAccount;
import services.CarteService;
import services.PromoCodeService;
import services.LoyaltyService;
import services.StripePaymentService;
import services.ProductService;
import java.util.Optional;
import java.io.File;

/**
 * Contrôleur pour la vue du panier
 */
public class CarteController {
    
    // ==================== SERVICES ====================
    private final CarteService carteService = CarteService.getInstance();
    private final PromoCodeService promoCodeService = PromoCodeService.getInstance();
    private final LoyaltyService loyaltyService = LoyaltyService.getInstance();
    private final StripePaymentService stripePaymentService = StripePaymentService.getInstance();
    private final ProductService productService = new ProductService();

    // Variables pour les réductions
    private PromoCode appliedPromoCode = null;
    private double promoDiscount = 0.0;
    private double loyaltyDiscount = 0.0;
    private int redeemedPoints = 0;

    // ID utilisateur simulé (à remplacer par l'ID réel de l'utilisateur connecté)
    private static final int CURRENT_USER_ID = 1;

    // ==================== COMPOSANTS FXML ====================
    @FXML
    private VBox cartItemsVBox;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label emptyCartLabel;

    @FXML
    private TextField promoCodeField;

    @FXML
    private Label promoMessageLabel;

    @FXML
    private Label loyaltyPointsLabel;

    @FXML
    private Label loyaltyTierLabel;

    @FXML
    private Label loyaltyDiscountLabel;

    @FXML
    private ProgressBar loyaltyProgressBar;

    @FXML
    private Label loyaltyProgressLabel;

    @FXML
    private TextField pointsToRedeemField;

    @FXML
    private Label redeemMessageLabel;
    
    // ==================== MÉTHODES D'INITIALISATION ====================

    @FXML
    public void initialize() {
        loadCart();
        loadLoyaltyInfo();
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

        double subtotal = carteService.getTotalPrice();
        double tierDiscountAmount = subtotal * (loyaltyService.getOrCreateAccount(CURRENT_USER_ID).getDiscountPercentage() / 100.0);
        double finalTotal = subtotal - promoDiscount - loyaltyDiscount - tierDiscountAmount;

        // Simulation de paiement Stripe
        try {
            long amountInCents = StripePaymentService.convertToCents(finalTotal);
            stripePaymentService.createPaymentIntent(amountInCents, "tnd");

            // Payer avec succès (simulation)
            Alert paymentAlert = new Alert(Alert.AlertType.INFORMATION);
            paymentAlert.setTitle("Payment");
            paymentAlert.setHeaderText("Payment Processing");
            paymentAlert.setContentText("Processing payment of " + String.format("%.2f TND", finalTotal) + " via Stripe...\n\n(Test mode - No actual charge)");
            paymentAlert.showAndWait();

            // Ajouter les points fidélité (1 TND = 1 point)
            LoyaltyAccount.LoyaltyTier newTier = loyaltyService.addPoints(CURRENT_USER_ID, finalTotal);

            // Décrémenter le stock de chaque produit dans le panier
            for (CarteItem item : carteService.getAll()) {
                Product product = item.getProduct();
                int quantity = item.getQuantity();
                String size = item.getSize();
                if (size != null && !size.isEmpty()) {
                    productService.reduceStock(product.getId(), quantity, size);
                } else {
                    productService.reduceStock(product.getId(), quantity);
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout");
            alert.setHeaderText("Order Placed Successfully!");
            String message = "Thank you for your order. Total: " + String.format("%.2f TND", finalTotal);
            if (newTier != loyaltyService.getOrCreateAccount(CURRENT_USER_ID).getTier()) {
                message += "\n🎉 Congratulations! You reached " + newTier.name() + " tier!";
            }
            alert.setContentText(message);
            alert.show();

            // Appliquer le code promo (incrémenter l'utilisation)
            if (appliedPromoCode != null) {
                promoCodeService.applyCode(appliedPromoCode.getCode());
            }

            carteService.clear();
            appliedPromoCode = null;
            promoDiscount = 0.0;
            loyaltyDiscount = 0.0;
            redeemedPoints = 0;
            if (promoMessageLabel != null) {
                promoMessageLabel.setText("");
            }
            if (redeemMessageLabel != null) {
                redeemMessageLabel.setText("");
            }
            loadCart();
            loadLoyaltyInfo();
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Payment Error");
            errorAlert.setHeaderText("Payment Failed");
            errorAlert.setContentText("Payment processing failed: " + e.getMessage());
            errorAlert.show();
        }
    }

    @FXML
    public void applyPromoCode() {
        String code = promoCodeField.getText().trim();

        if (code.isEmpty()) {
            if (promoMessageLabel != null) {
                promoMessageLabel.setText("Please enter a promo code");
                promoMessageLabel.setStyle("-fx-text-fill: #EF4444;");
            }
            return;
        }

        Optional<PromoCode> promoCodeOpt = promoCodeService.validateCode(code);

        if (promoCodeOpt.isEmpty()) {
            if (promoMessageLabel != null) {
                promoMessageLabel.setText("Invalid or expired promo code");
                promoMessageLabel.setStyle("-fx-text-fill: #EF4444;");
            }
            return;
        }

        PromoCode promoCode = promoCodeOpt.get();
        double subtotal = carteService.getTotalPrice();
        promoDiscount = promoCode.calculateDiscount(subtotal);
        appliedPromoCode = promoCode;

        if (promoMessageLabel != null) {
            String discountText = promoCode.getType() == PromoCode.PromoType.PERCENTAGE
                    ? promoCode.getValue() + "%"
                    : String.format("%.2f TND", promoCode.getValue());
            promoMessageLabel.setText("Promo code applied! Discount: " + discountText);
            promoMessageLabel.setStyle("-fx-text-fill: #10B981;");
        }

        updateSummary();
    }

    @FXML
    public void redeemPoints() {
        String pointsText = pointsToRedeemField.getText().trim();

        if (pointsText.isEmpty()) {
            if (redeemMessageLabel != null) {
                redeemMessageLabel.setText("Please enter points to redeem");
                redeemMessageLabel.setStyle("-fx-text-fill: #EF4444;");
            }
            return;
        }

        try {
            int points = Integer.parseInt(pointsText);

            if (points < 100 || points % 100 != 0) {
                if (redeemMessageLabel != null) {
                    redeemMessageLabel.setText("Points must be multiples of 100 (100 = 1 TND)");
                    redeemMessageLabel.setStyle("-fx-text-fill: #EF4444;");
                }
                return;
            }

            if (!loyaltyService.hasEnoughPoints(CURRENT_USER_ID, points)) {
                if (redeemMessageLabel != null) {
                    redeemMessageLabel.setText("Insufficient points");
                    redeemMessageLabel.setStyle("-fx-text-fill: #EF4444;");
                }
                return;
            }

            if (loyaltyService.redeemPoints(CURRENT_USER_ID, points)) {
                redeemedPoints = points;
                loyaltyDiscount = loyaltyService.calculateDiscountFromPoints(points);

                if (redeemMessageLabel != null) {
                    redeemMessageLabel.setText("Redeemed " + points + " points for " + String.format("%.2f TND", loyaltyDiscount));
                    redeemMessageLabel.setStyle("-fx-text-fill: #10B981;");
                }

                loadLoyaltyInfo();
                updateSummary();
            }
        } catch (NumberFormatException e) {
            if (redeemMessageLabel != null) {
                redeemMessageLabel.setText("Invalid number");
                redeemMessageLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void loadLoyaltyInfo() {
        LoyaltyAccount account = loyaltyService.getOrCreateAccount(CURRENT_USER_ID);

        if (loyaltyPointsLabel != null) {
            loyaltyPointsLabel.setText(String.valueOf(account.getCurrentPoints()));
        }

        if (loyaltyTierLabel != null) {
            loyaltyTierLabel.setText(account.getTier().name());
            // Couleur selon le palier
            String tierColor = switch (account.getTier()) {
                case BRONZE -> "#CD7F32";
                case SILVER -> "#C0C0C0";
                case GOLD -> "#FFD700";
            };
            loyaltyTierLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + tierColor + ";");
        }

        if (loyaltyDiscountLabel != null) {
            loyaltyDiscountLabel.setText(account.getDiscountPercentage() + "%");
        }

        if (loyaltyProgressBar != null) {
            loyaltyProgressBar.setProgress(account.getProgressToNextTier() / 100.0);
        }

        if (loyaltyProgressLabel != null) {
            int pointsToNext = account.getPointsToNextTier();
            LoyaltyAccount.LoyaltyTier nextTier = account.getTier().getNextTier();
            if (nextTier != null) {
                loyaltyProgressLabel.setText(account.getCurrentPoints() + " / " + nextTier.getPointsThreshold() + " points to " + nextTier.name());
            } else {
                loyaltyProgressLabel.setText("Maximum tier reached!");
            }
        }
    }
    
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

        System.out.println("Loading cart items. Total items: " + carteService.getAll().size());
        for (CarteItem item : carteService.getAll()) {
            System.out.println("Adding item: " + item.getProduct().getName());
            HBox itemRow = createCartItemRow(item);
            cartItemsVBox.getChildren().add(itemRow);
        }
        System.out.println("Total children in VBox: " + cartItemsVBox.getChildren().size());

        updateSummary();
    }
    
    private HBox createCartItemRow(CarteItem item) {
        HBox row = new HBox(15);
        row.getStyleClass().add("product-card");
        row.setPrefWidth(860);
        row.setStyle("-fx-padding: 15; -fx-background-color: #1E293B; -fx-background-radius: 10;");
        
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
        infoBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        
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
            double subtotal = carteService.getTotalPrice();
            double tierDiscountAmount = subtotal * (loyaltyService.getOrCreateAccount(CURRENT_USER_ID).getDiscountPercentage() / 100.0);
            double finalTotal = subtotal - promoDiscount - loyaltyDiscount - tierDiscountAmount;
            totalPriceLabel.setText(String.format("%.2f TND", finalTotal));
        }
    }
}
