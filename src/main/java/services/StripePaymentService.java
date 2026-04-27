package services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

/**
 * Service pour la gestion des paiements via Stripe
 * Mode test : utilise des clés de test Stripe (pas d'argent réel)
 */
public class StripePaymentService {
    private static StripePaymentService instance;
    
    // Clés de test Stripe (à remplacer par vos clés de test réelles)
    private static final String STRIPE_API_KEY = "sk_test_your_stripe_test_key_here";
    
    private StripePaymentService() {
        // Initialiser Stripe avec la clé API
        // Stripe.apiKey = STRIPE_API_KEY;
    }
    
    public static StripePaymentService getInstance() {
        if (instance == null) {
            instance = new StripePaymentService();
        }
        return instance;
    }
    
    /**
     * Crée un PaymentIntent pour un paiement
     * @param amount Montant en centimes (ex: 1000 = 10.00 TND)
     * @param currency Devise (ex: "tnd")
     * @return PaymentIntent créé
     */
    public PaymentIntent createPaymentIntent(long amount, String currency) throws StripeException {
        // Configuration de la clé API (à décommenter en production)
        // Stripe.apiKey = STRIPE_API_KEY;
        
        // En mode test/simulation, on retourne un PaymentIntent simulé
        return createSimulatedPaymentIntent(amount, currency);
    }
    
    /**
     * Crée un PaymentIntent simulé pour le développement
     * À remplacer par un vrai appel Stripe en production
     */
    private PaymentIntent createSimulatedPaymentIntent(long amount, String currency) {
        // Simulation d'un paiement réussi
        // En production, utiliser : PaymentIntent.create(params);
        
        System.out.println("SIMULATION STRIPE:");
        System.out.println("Montant: " + amount + " centimes (" + (amount / 100.0) + " TND)");
        System.out.println("Devise: " + currency);
        System.out.println("Mode test activé - Pas de débit réel");
        
        // Retourner null pour indiquer que c'est une simulation
        // En production, retourner le vrai PaymentIntent
        return null;
    }
    
    /**
     * Confirme un paiement
     * @param paymentIntentId ID du PaymentIntent à confirmer
     * @return true si le paiement est confirmé avec succès
     */
    public boolean confirmPayment(String paymentIntentId) {
        // Simulation de confirmation
        System.out.println("SIMULATION: Confirmation du paiement " + paymentIntentId);
        return true;
    }
    
    /**
     * Annule un paiement
     * @param paymentIntentId ID du PaymentIntent à annuler
     * @return true si le paiement est annulé avec succès
     */
    public boolean cancelPayment(String paymentIntentId) {
        // Simulation d'annulation
        System.out.println("SIMULATION: Annulation du paiement " + paymentIntentId);
        return true;
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
