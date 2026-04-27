package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import entities.PromoCode;
import services.PromoCodeService;
import java.time.LocalDate;

/**
 * Contrôleur pour la gestion des codes promo
 */
public class PromoCodesController {

    // ==================== SERVICES ====================
    private final PromoCodeService promoCodeService = PromoCodeService.getInstance();

    // ==================== COMPOSANTS FXML ====================
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
    private VBox promoCodesVBox;

    @FXML
    private Label statusLabel;

    // ==================== MÉTHODES D'INITIALISATION ====================

    @FXML
    public void initialize() {
        setupPromoTypeCombo();
        loadPromoCodes();
    }

    // ==================== ACTIONS ====================

    @FXML
    public void goBack() {
        NavigationController.showAllProducts();
    }

    @FXML
    public void createPromoCode() {
        String code = promoCodeField != null ? promoCodeField.getText().trim() : "";
        String type = promoTypeCombo != null ? promoTypeCombo.getValue() : null;
        String valueText = promoValueField != null ? promoValueField.getText().trim() : "";
        String maxUsageText = promoMaxUsageField != null ? promoMaxUsageField.getText().trim() : "";
        LocalDate expirationDate = promoExpirationDatePicker != null ? promoExpirationDatePicker.getValue() : null;

        if (code.isEmpty() || type == null || valueText.isEmpty()) {
            updateStatus("Please fill in all required fields");
            return;
        }

        try {
            double value = Double.parseDouble(valueText);
            int maxUsage = maxUsageText.isEmpty() ? 0 : Integer.parseInt(maxUsageText);

            PromoCode.PromoType promoType = PromoCode.PromoType.valueOf(type);
            PromoCode promoCode = new PromoCode(code, promoType, value, expirationDate, maxUsage);

            PromoCode created = promoCodeService.create(promoCode);
            if (created != null) {
                updateStatus("Promo code created successfully!");
                loadPromoCodes();
                clearPromoForm();
            } else {
                updateStatus("Failed to create promo code");
            }
        } catch (NumberFormatException e) {
            updateStatus("Invalid value or max usage");
        } catch (Exception e) {
            updateStatus("Error: " + e.getMessage());
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Configure le combo box des types de promo
     */
    private void setupPromoTypeCombo() {
        if (promoTypeCombo != null) {
            promoTypeCombo.getItems().addAll("PERCENTAGE", "FIXED");
        }
    }

    /**
     * Charge les codes promo dans la liste
     */
    private void loadPromoCodes() {
        if (promoCodesVBox == null) return;

        promoCodesVBox.getChildren().clear();

        for (PromoCode promoCode : promoCodeService.getAll()) {
            HBox promoCard = createPromoCodeCard(promoCode);
            promoCodesVBox.getChildren().add(promoCard);
        }
    }

    /**
     * Crée une carte visuelle pour un code promo
     */
    private HBox createPromoCodeCard(PromoCode promoCode) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #312E81; -fx-border-color: #1E1B4B; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-border-width: 2;");
        card.setPrefWidth(1160);

        // Code
        VBox codeBox = new VBox(5);
        codeBox.setPrefWidth(150);
        Label codeLabel = new Label("Code");
        codeLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        Label codeValue = new Label(promoCode.getCode());
        codeValue.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px; -fx-font-weight: bold;");
        codeBox.getChildren().addAll(codeLabel, codeValue);

        // Type
        VBox typeBox = new VBox(5);
        typeBox.setPrefWidth(120);
        Label typeLabel = new Label("Type");
        typeLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        Label typeValue = new Label(promoCode.getType().name());
        typeValue.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-weight: bold;");
        typeBox.getChildren().addAll(typeLabel, typeValue);

        // Value
        VBox valueBox = new VBox(5);
        valueBox.setPrefWidth(100);
        Label valueLabel = new Label("Value");
        valueLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        Label valueValue = new Label(String.valueOf(promoCode.getValue()) + (promoCode.getType() == PromoCode.PromoType.PERCENTAGE ? "%" : " TND"));
        valueValue.setStyle("-fx-text-fill: #06B6D4; -fx-font-size: 14px; -fx-font-weight: bold;");
        valueBox.getChildren().addAll(valueLabel, valueValue);

        // Usage
        VBox usageBox = new VBox(5);
        usageBox.setPrefWidth(120);
        Label usageLabel = new Label("Usage");
        usageLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        Label usageValue = new Label(promoCode.getCurrentUsage() + " / " + (promoCode.getMaxUsage() == 0 ? "∞" : promoCode.getMaxUsage()));
        usageValue.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-weight: bold;");
        usageBox.getChildren().addAll(usageLabel, usageValue);

        // Expiration
        VBox expBox = new VBox(5);
        expBox.setPrefWidth(120);
        Label expLabel = new Label("Expiration");
        expLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        Label expValue = new Label(promoCode.getExpirationDate() != null ? promoCode.getExpirationDate().toString() : "No limit");
        expValue.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-weight: bold;");
        expBox.getChildren().addAll(expLabel, expValue);

        // Status
        VBox statusBox = new VBox(5);
        statusBox.setPrefWidth(80);
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        Label statusValue = new Label(promoCode.isActive() ? "Active" : "Inactive");
        statusValue.setStyle("-fx-text-fill: " + (promoCode.isActive() ? "#10B981" : "#EF4444") + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        // Actions
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button toggleButton = new Button(promoCode.isActive() ? "Disable" : "Enable");
        toggleButton.getStyleClass().add(promoCode.isActive() ? "button-danger" : "button-success");
        toggleButton.setPrefWidth(80);
        toggleButton.setOnAction(e -> togglePromoCode(promoCode));

        actionBox.getChildren().add(toggleButton);

        card.getChildren().addAll(codeBox, typeBox, valueBox, usageBox, expBox, statusBox, actionBox);
        return card;
    }

    /**
     * Active/désactive un code promo
     */
    private void togglePromoCode(PromoCode promoCode) {
        if (promoCode.isActive()) {
            promoCodeService.deactivate(promoCode.getId());
            updateStatus("Promo code disabled");
        } else {
            promoCodeService.activate(promoCode.getId());
            updateStatus("Promo code activated");
        }
        loadPromoCodes();
    }

    /**
     * Vide le formulaire de création de promo
     */
    private void clearPromoForm() {
        if (promoCodeField != null) promoCodeField.clear();
        if (promoTypeCombo != null) promoTypeCombo.setValue(null);
        if (promoValueField != null) promoValueField.clear();
        if (promoMaxUsageField != null) promoMaxUsageField.clear();
        if (promoExpirationDatePicker != null) promoExpirationDatePicker.setValue(null);
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
