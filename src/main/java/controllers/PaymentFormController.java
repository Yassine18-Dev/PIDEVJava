package controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import java.net.URL;

/**
 * Contrôleur pour le formulaire de paiement Stripe
 * Utilise WebView pour afficher le formulaire HTML avec Stripe Elements
 */
public class PaymentFormController {
    
    @FXML
    private WebView webView;
    
    private WebEngine webEngine;
    private String clientSecret;
    private String publishableKey;
    private double amountEUR;
    private PaymentCallback paymentCallback;
    
    public interface PaymentCallback {
        void onPaymentSuccess(String paymentIntentId);
        void onPaymentFailure(String error);
        void onWindowClosed();
    }
    
    @FXML
    public void initialize() {
        webEngine = webView.getEngine();
        
        // Enable JavaScript
        webEngine.setJavaScriptEnabled(true);
        
        // Set up JavaFX bridge for JavaScript communication
        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("JavaFX", this);
        
        // Load the HTML file
        loadPaymentForm();
    }
    
    private void loadPaymentForm() {
        try {
            URL htmlUrl = getClass().getResource("/html/payment-form.html");
            if (htmlUrl != null) {
                webEngine.load(htmlUrl.toExternalForm());
            } else {
                System.err.println("Could not find payment-form.html");
            }
        } catch (Exception e) {
            System.err.println("Error loading payment form: " + e.getMessage());
        }
    }
    
    /**
     * Initialize the payment form with Stripe parameters
     * @param publishableKey Stripe publishable key (pk_test_...)
     * @param clientSecret PaymentIntent client secret
     * @param amountEUR Amount in EUR
     * @param callback Callback for payment events
     */
    public void initializePayment(String publishableKey, String clientSecret, double amountEUR, PaymentCallback callback) {
        this.publishableKey = publishableKey;
        this.clientSecret = clientSecret;
        this.amountEUR = amountEUR;
        this.paymentCallback = callback;
        
        // Wait for the page to load, then initialize Stripe
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                initializeStripeInWebView();
            }
        });
    }
    
    private void initializeStripeInWebView() {
        String script = String.format(
            "window.postMessage({type: 'initialize', publishableKey: '%s', clientSecret: '%s', amountEUR: %f}, '*');",
            publishableKey, clientSecret, amountEUR
        );
        webEngine.executeScript(script);
    }
    
    /**
     * Called from JavaScript when payment succeeds
     */
    public void paymentSucceeded(String paymentIntentId) {
        System.out.println("=== PAYMENT SUCCESS CALLBACK RECEIVED ===");
        System.out.println("PaymentIntent ID: " + paymentIntentId);
        System.out.println("PaymentCallback: " + paymentCallback);
        
        if (paymentCallback != null) {
            System.out.println("Calling onPaymentSuccess...");
            paymentCallback.onPaymentSuccess(paymentIntentId);
            System.out.println("onPaymentSuccess called");
        } else {
            System.err.println("ERROR: paymentCallback is null!");
        }
    }
    
    /**
     * Called from JavaScript when payment fails
     */
    public void paymentFailed(String error) {
        System.err.println("Payment failed: " + error);
        if (paymentCallback != null) {
            paymentCallback.onPaymentFailure(error);
        }
    }
    
    /**
     * Called from JavaScript to close the window
     */
    public void closeWindow() {
        if (paymentCallback != null) {
            paymentCallback.onWindowClosed();
        }
    }
    
    /**
     * Get the WebView for display in a Stage
     */
    public WebView getWebView() {
        return webView;
    }
}
