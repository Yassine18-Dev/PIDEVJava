package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import entities.CarteItem;
import entities.Product;
import entities.PromoCode;
import entities.LoyaltyAccount;
import services.CarteService;
import services.PromoCodeService;
import services.LoyaltyService;
import services.StripePaymentService;
import services.ProductService;
import services.CurrencyConversionService;
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
    private final CurrencyConversionService currencyConversionService = CurrencyConversionService.getInstance();

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

        // Paiement Stripe avec conversion automatique TND → EUR et formulaire complet
        try {
            // Convertir TND → EUR
            double amountEUR = currencyConversionService.convertTNDtoEUR(finalTotal);
            
            // Créer le PaymentIntent et obtenir le client secret
            String clientSecret = stripePaymentService.createPaymentIntentAndGetClientSecret(finalTotal);
            String publishableKey = stripePaymentService.getPublishableKey();
            
            System.out.println("Ouverture du formulaire de paiement Stripe");
            System.out.println("Montant: " + finalTotal + " TND → " + amountEUR + " EUR");
            
            // Ouvrir le formulaire de paiement dans une nouvelle fenêtre
            openPaymentForm(publishableKey, clientSecret, amountEUR, finalTotal);
            
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Payment Error");
            errorAlert.setHeaderText("Payment Initialization Failed");
            errorAlert.setContentText("Could not initialize payment: " + e.getMessage());
            errorAlert.show();
        }
    }
    
    /**
     * Ouvre le formulaire de paiement Stripe dans une nouvelle fenêtre
     */
    private void openPaymentForm(String publishableKey, String clientSecret, double amountEUR, double amountTND) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/PaymentFormView.fxml"));
            Parent root = loader.load();
            
            PaymentFormController paymentController = loader.getController();
            
            // Créer une nouvelle fenêtre pour le paiement
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Complete Payment - " + String.format("%.2f TND", amountTND));
            paymentStage.setScene(new Scene(root, 800, 600));
            paymentStage.setResizable(false);
            
            // Initialiser le formulaire avec les paramètres Stripe
            paymentController.initializePayment(publishableKey, clientSecret, amountEUR, new PaymentFormController.PaymentCallback() {
                @Override
                public void onPaymentSuccess(String paymentIntentId) {
                    System.out.println("=== PAYMENT SUCCESS CALLBACK RECEIVED ===");
                    System.out.println("PaymentIntent ID: " + paymentIntentId);
                    System.out.println("Closing payment window...");
                    
                    // Fermer la fenêtre immédiatement depuis JavaFX
                    paymentStage.close();
                    completeOrder(amountTND);
                }
                
                @Override
                public void onPaymentFailure(String error) {
                    System.err.println("Paiement échoué: " + error);
                    paymentStage.close();
                    
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Payment Failed");
                    errorAlert.setHeaderText("Payment Could Not Be Processed");
                    errorAlert.setContentText("Payment failed: " + error);
                    errorAlert.show();
                }
                
                @Override
                public void onWindowClosed() {
                    System.out.println("Fenêtre de paiement fermée");
                }
            });
            
            // Démarrer un timer pour vérifier le statut du paiement
            // C'est un fallback si le callback JavaScript ne fonctionne pas
            java.util.Timer paymentTimer = new java.util.Timer();
            paymentTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        if (paymentStage.isShowing()) {
                            System.out.println("Payment window still open - checking payment status...");
                            try {
                                // Récupérer le PaymentIntent et vérifier son statut
                                String paymentIntentId = clientSecret.substring(0, clientSecret.indexOf("_secret_"));
                                com.stripe.model.PaymentIntent paymentIntent = 
                                    com.stripe.model.PaymentIntent.retrieve(paymentIntentId);
                                
                                System.out.println("PaymentIntent status: " + paymentIntent.getStatus());
                                
                                if ("succeeded".equals(paymentIntent.getStatus())) {
                                    System.out.println("Payment succeeded via polling - closing window");
                                    paymentStage.close();
                                    completeOrder(amountTND);
                                    paymentTimer.cancel();
                                } else if ("canceled".equals(paymentIntent.getStatus())) {
                                    System.out.println("Payment canceled - closing window");
                                    paymentStage.close();
                                    paymentTimer.cancel();
                                }
                            } catch (Exception e) {
                                System.err.println("Error checking payment status: " + e.getMessage());
                            }
                        } else {
                            paymentTimer.cancel();
                        }
                    });
                }
            }, 3000, 2000); // Commencer après 3 secondes, vérifier toutes les 2 secondes
            
            paymentStage.showAndWait();
            paymentTimer.cancel();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du formulaire de paiement: " + e.getMessage());
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could Not Open Payment Form");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.show();
        }
    }
    
    /**
     * Complète la commande après un paiement réussi
     */
    private void completeOrder(double finalTotal) {
        try {
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
            System.err.println("Erreur lors de la completion de la commande: " + e.getMessage());
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Order Completion Failed");
            errorAlert.setContentText("An error occurred while completing your order: " + e.getMessage());
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
