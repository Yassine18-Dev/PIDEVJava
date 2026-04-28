package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service pour la conversion de devises via l'API Fixer
 * Utilise l'API Fixer pour obtenir les taux de change en temps réel
 */
public class CurrencyConversionService {
    private static CurrencyConversionService instance;
    
    // Clé API Fixer
    private static final String FIXER_API_KEY = "9f5e1c05e2920672b19a268529e861e8";
    
    // URL de l'API Fixer
    private static final String FIXER_API_URL = "http://data.fixer.io/api/latest";
    
    // Taux de change par défaut (fallback si l'API échoue)
    // 1 TND ≈ 0.30 EUR (taux approximatif)
    private static final double DEFAULT_TND_TO_EUR_RATE = 0.30;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private CurrencyConversionService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public static CurrencyConversionService getInstance() {
        if (instance == null) {
            instance = new CurrencyConversionService();
        }
        return instance;
    }
    
    /**
     * Convertit un montant de TND vers EUR
     * @param amountTND Montant en TND
     * @return Montant converti en EUR
     */
    public double convertTNDtoEUR(double amountTND) {
        try {
            double rate = getTNDtoEURRate();
            return amountTND * rate;
        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion TND→EUR: " + e.getMessage());
            System.err.println("Utilisation du taux de change par défaut: " + DEFAULT_TND_TO_EUR_RATE);
            return amountTND * DEFAULT_TND_TO_EUR_RATE;
        }
    }
    
    /**
     * Récupère le taux de change TND → EUR depuis l'API Fixer
     * @return Taux de change TND → EUR
     */
    private double getTNDtoEURRate() throws IOException, InterruptedException {
        // Construire l'URL de l'API avec base=EUR (par défaut) et symbols=TND
        String url = String.format("%s?access_key=%s&symbols=TND", FIXER_API_URL, FIXER_API_KEY);
        
        // Créer la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        // Envoyer la requête
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Parser la réponse JSON
        JsonNode rootNode = objectMapper.readTree(response.body());
        
        // Vérifier si la requête a réussi
        if (!rootNode.has("success") || !rootNode.get("success").asBoolean()) {
            JsonNode errorNode = rootNode.get("error");
            String error = (errorNode != null && errorNode.has("info")) ? errorNode.get("info").asText() : "Unknown error";
            throw new IOException("API Fixer error: " + error);
        }
        
        // Extraire le taux de change
        JsonNode ratesNode = rootNode.get("rates");
        if (ratesNode != null && ratesNode.has("TND")) {
            // Avec base=EUR, le taux TND est EUR → TND
            // Pour TND → EUR, on utilise 1 / taux_TND
            double tndRate = ratesNode.get("TND").asDouble();
            return 1.0 / tndRate;
        }
        
        throw new IOException("Taux de change non trouvé dans la réponse API");
    }
    
    /**
     * Convertit un montant de TND vers EUR et retourne le résultat en centimes pour Stripe
     * @param amountTND Montant en TND
     * @return Montant en centimes d'EUR
     */
    public long convertTNDtoEURCents(double amountTND) {
        double amountEUR = convertTNDtoEUR(amountTND);
        return (long) (amountEUR * 100);
    }
    
    /**
     * Définit la clé API Fixer
     * @param apiKey Clé API Fixer
     */
    public void setApiKey(String apiKey) {
        // Note: Comme la clé est static final, cette méthode ne peut pas modifier la clé
        // Pour utiliser une vraie clé, modifiez directement la constante FIXER_API_KEY
        System.out.println("Pour utiliser une clé API réelle, modifiez FIXER_API_KEY dans CurrencyConversionService.java");
    }
    
    /**
     * Vérifie si le service est configuré avec une clé API valide
     * @return true si une clé API est configurée
     */
    public boolean isConfigured() {
        return !FIXER_API_KEY.equals("your_fixer_api_key_here");
    }
}
