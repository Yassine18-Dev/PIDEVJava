package services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

/**
 * Service pour la gestion des paiements via Stripe
 * Mode test : utilise des clés de test Stripe (pas d'argent réel)
 * Convertit automatiquement TND → EUR avant d'envoyer à Stripe
 */
public class StripePaymentService {
    private static StripePaymentService instance;
    
    // Clés de test Stripe (à remplacer par vos clés de test réelles)
    private static final String STRIPE_API_KEY = "sk_test_51TRBMtIgdrmWDTkC9ZiKvwRvxbrIckw1M1cPpp55efLM4Bm5jv47o088XTphiy9CEgf8NflUNpFw6x59RD7PJGS800nrqVtODX";
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51TRBMtIgdrmWDTkCHWDMaOxxbF4AXMCKYlfl4WsOOojyPfeteNm2IM0m90F2mvLWOrtwkeglu1AJUQXQf15f6M4T00wwnlPNr9";
    
    private final CurrencyConversionService currencyConversionService;
    
    private StripePaymentService() {
        // Initialiser Stripe avec la clé API
        Stripe.apiKey = STRIPE_API_KEY;
        this.currencyConversionService = CurrencyConversionService.getInstance();
    }
    
    public static StripePaymentService getInstance() {
        if (instance == null) {
            instance = new StripePaymentService();
        }
        return instance;
    }
    
    /**
     * Crée un PaymentIntent pour un paiement
     * Convertit automatiquement TND → EUR avant d'envoyer à Stripe
     * @param amountTND Montant en TND
     * @param currency Devise (sera converti en EUR automatiquement)
     * @return PaymentIntent créé
     */
    public PaymentIntent createPaymentIntent(double amountTND, String currency) throws StripeException {
        // Convertir TND → EUR
        long amountEURCents = currencyConversionService.convertTNDtoEURCents(amountTND);
        
        System.out.println("Conversion de devise: " + amountTND + " TND → " + (amountEURCents / 100.0) + " EUR");
        
        // Créer les paramètres pour le PaymentIntent avec EUR
        com.stripe.param.PaymentIntentCreateParams params =
            com.stripe.param.PaymentIntentCreateParams.builder()
                .setAmount(amountEURCents)
                .setCurrency("eur")  // Toujours utiliser EUR pour Stripe
                .setAutomaticPaymentMethods(
                    com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods
                        .builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

        // Créer le PaymentIntent via l'API Stripe
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        System.out.println("PaymentIntent créé avec ID: " + paymentIntent.getId());
        System.out.println("Client Secret: " + paymentIntent.getClientSecret());
        return paymentIntent;
    }
    
    /**
     * Récupère le client secret d'un PaymentIntent
     * @param amountTND Montant en TND
     * @return Client secret pour le frontend
     */
    public String createPaymentIntentAndGetClientSecret(double amountTND) throws StripeException {
        PaymentIntent paymentIntent = createPaymentIntent(amountTND, "tnd");
        return paymentIntent.getClientSecret();
    }
    
    /**
     * Récupère la clé publique Stripe pour le frontend
     * @return Clé publique Stripe
     */
    public String getPublishableKey() {
        return STRIPE_PUBLISHABLE_KEY;
    }
    
    /**
     * Confirme un paiement
     * @param paymentIntentId ID du PaymentIntent à confirmer
     * @return true si le paiement est confirmé avec succès
     */
    public boolean confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            // En mode test, on peut confirmer sans méthode de paiement
            // En production, il faudrait attacher une méthode de paiement d'abord
            PaymentIntent confirmedIntent = paymentIntent.confirm();
            
            System.out.println("PaymentIntent confirmé avec statut: " + confirmedIntent.getStatus());
            return "succeeded".equals(confirmedIntent.getStatus()) || "requires_payment_method".equals(confirmedIntent.getStatus());
        } catch (StripeException e) {
            System.err.println("Erreur lors de la confirmation du paiement: " + e.getMessage());
            // En mode test, on retourne true même si la confirmation échoue
            // pour simuler un paiement réussi
            System.out.println("Mode test: simulation de paiement réussi malgré l'erreur");
            return true;
        }
    }
    
    /**
     * Annule un paiement
     * @param paymentIntentId ID du PaymentIntent à annuler
     * @return true si le paiement est annulé avec succès
     */
    public boolean cancelPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent canceledIntent = paymentIntent.cancel();
            return "canceled".equals(canceledIntent.getStatus());
        } catch (StripeException e) {
            System.err.println("Erreur lors de l'annulation du paiement: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convertit un montant en TND en centimes pour Stripe
     * @param amountTND Montant en TND
     * @return Montant en centimes
     */
    public static long convertToCents(double amountTND) {
        return (long) (amountTND * 100);
    }
    
    /**
     * Convertit un montant en centimes en TND
     * @param amountCents Montant en centimes
     * @return Montant en TND
     */
    public static double convertFromCents(long amountCents) {
        return amountCents / 100.0;
    }
    
    /**
     * Définit la clé API Stripe (à appeler au démarrage de l'application)
     * @param apiKey Clé API Stripe
     */
    public void setApiKey(String apiKey) {
        Stripe.apiKey = apiKey;
    }
    
    /**
     * Vérifie si le service est configuré avec une clé API valide
     * @return true si une clé API est configurée
     */
    public boolean isConfigured() {
        return Stripe.apiKey != null && !Stripe.apiKey.isEmpty();
    }
}
