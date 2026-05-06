package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour l'analyse d'images via Gemini API
 */
public class GeminiAPIService {
    
    // Configuration Gemini API
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String API_KEY = "AIzaSyD7RWmM0qSLDUHLkOZPEMV0-_N2PX0MUAA"; // Votre clé API
    
    /**
     * Analyse une image via son URL et retourne les informations du produit
     */
    public CompletableFuture<ProductAnalysisResult> analyzeImageFromUrl(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Construire le prompt pour Gemini
                String prompt = buildPrompt();
                
                // Construire la requête JSON
                String requestBody = buildGeminiRequestBody(imageUrl, prompt);
                
                // Envoyer la requête à Gemini API
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + "?key=" + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Parser la réponse et extraire les informations
                return parseGeminiResponse(response.body());
                
            } catch (Exception e) {
                System.err.println("Gemini API error: " + e.getMessage());
                return createFallbackResult();
            }
        });
    }
    
    /**
     * Construit le prompt pour l'analyse d'image gaming
     */
    private String buildPrompt() {
        return "Analyse cette image de produit gaming et génère des informations structurées. " +
               "Détermine si c'est un skin (objet virtuel dans un jeu) ou du merch (vêtement/accessoire physique). " +
               "Génère un titre attractif, une description marketing pertinente, et une catégorie. " +
               "Réponds UNIQUEMENT au format JSON exact suivant: " +
               "{\"name\":\"Nom du produit\",\"description\":\"Description marketing détaillée\",\"category\":\"skin|merch\"}";
    }
    
    /**
     * Construit le corps de la requête pour Gemini API
     */
    private String buildGeminiRequestBody(String imageUrl, String prompt) {
        return "{"
            + "\"contents\":[{"
            + "\"parts\":["
            + "{\"text\":\"" + escapeJson(prompt) + "\"},"
            + "{\"inline_data\":{"
            + "\"mime_type\":\"image/jpeg\","
            + "\"data\":\"" + imageUrl + "\""
            + "}}"
            + "]"
            + "}]"
            + "}";
    }
    
    /**
     * Parse la réponse JSON de Gemini API
     */
    private ProductAnalysisResult parseGeminiResponse(String jsonResponse) {
        try {
            // Parser simple pour extraire le texte de la réponse
            if (jsonResponse.contains("\"text\"")) {
                int start = jsonResponse.indexOf("\"text\":\"") + 8;
                int end = jsonResponse.indexOf("\"", start);
                String text = jsonResponse.substring(start, end);
                
                // Extraire le JSON de la réponse
                if (text.contains("{") && text.contains("}")) {
                    int jsonStart = text.indexOf("{");
                    int jsonEnd = text.lastIndexOf("}") + 1;
                    String jsonPart = text.substring(jsonStart, jsonEnd);
                    
                    return parseProductJson(jsonPart);
                }
            }
            
            return createFallbackResult();
            
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            return createFallbackResult();
        }
    }
    
    /**
     * Parse le JSON du produit
     */
    private ProductAnalysisResult parseProductJson(String json) {
        ProductAnalysisResult result = new ProductAnalysisResult();
        
        try {
            // Parser simple du JSON
            if (json.contains("\"name\"")) {
                int start = json.indexOf("\"name\":\"") + 8;
                int end = json.indexOf("\"", start);
                result.setName(json.substring(start, end));
            }
            
            if (json.contains("\"description\"")) {
                int start = json.indexOf("\"description\":\"") + 14;
                int end = json.indexOf("\"", start);
                result.setDescription(json.substring(start, end));
            }
            
            if (json.contains("\"category\"")) {
                int start = json.indexOf("\"category\":\"") + 12;
                int end = json.indexOf("\"", start);
                String category = json.substring(start, end);
                result.setType(category);
                result.setPrice(category.equals("skin") ? 59.99 : 39.99);
            }
            
            // Validation
            if (result.getName() == null || result.getName().isEmpty()) {
                result.setName("Gaming Product");
            }
            if (result.getDescription() == null || result.getDescription().isEmpty()) {
                result.setDescription("High-quality gaming product for enthusiasts.");
            }
            if (result.getType() == null) {
                result.setType("merch");
                result.setPrice(39.99);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing product JSON: " + e.getMessage());
            return createFallbackResult();
        }
        
        return result;
    }
    
    /**
     * Crée un résultat par défaut
     */
    private ProductAnalysisResult createFallbackResult() {
        ProductAnalysisResult result = new ProductAnalysisResult();
        result.setName("Gaming Product");
        result.setDescription("High-quality gaming product analyzed by AI.");
        result.setType("merch");
        result.setPrice(39.99);
        return result;
    }
    
    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Classe de résultat pour l'analyse de produit
     */
    public static class ProductAnalysisResult {
        private String name;
        private String description;
        private String type;
        private double price;
        
        // Getters et setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        /**
         * Applique les résultats à un produit existant
         */
        public void applyToProduct(entities.Product product) {
            product.setName(name);
            product.setDescription(description);
            product.setType(type);
            product.setPrice(price);
            
            // Si c'est un merch, ajouter des tailles par défaut
            if ("merch".equals(type) && product instanceof entities.Merch) {
                ((entities.Merch) product).setSizes("S,M,L,XL");
            }
        }
    }
}
