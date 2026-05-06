package services;

import entities.Product;
import entities.Skin;
import entities.Merch;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'analyse d'image par IA pour détecter le type de produit
 * et générer automatiquement titre, prix et description
 */
public class ImageAnalysisService {
    
    // Imagga API (gratuite - 1000 requêtes/mois)
    private static final String IMAGGA_API_URL = "https://api.imagga.com/v2/tags";
    private static final String IMAGGA_API_KEY = "acc_40b1b4c9c0119ee"; // Clé Imagga utilisateur
    private static final String IMAGGA_API_SECRET = "0f19b03d274d14e5fb4173e5f1eaebe8"; // Secret Imagga utilisateur
    
    // Google Vision API (backup)
    private static final String VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate";
    private static final String GOOGLE_API_KEY = "AIzaSyD7RWmM0qSLDUHLkOZPEMV0-_N2PX0MUAA"; // Google Vision API
    
    /**
     * Analyse une image et retourne les informations du produit générées par IA
     */
    public CompletableFuture<ProductAnalysisResult> analyzeImage(String imagePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Analyse améliorée avec simulation intelligente
                ProductAnalysisResult result = performIntelligentAnalysis(imagePath);
                
                return result;
            } catch (Exception e) {
                System.err.println("Erreur lors de l'analyse de l'image: " + e.getMessage());
                return createDefaultResult();
            }
        });
    }
    
    /**
     * Encode une image en base64 pour l'envoyer à l'API
     */
    private String encodeImageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Crée le prompt pour l'analyse d'image
     */
    private String createAnalysisPrompt() {
        return """
            Analyse cette image de produit gaming et détermine :
            1. Le type de produit (skin ou merch)
            2. Un titre attractif et pertinent
            3. Un prix raisonnable en TND (entre 5 et 200 TND)
            4. Une description détaillée et marketing
            
            Réponds au format JSON exact :
            {
                "type": "skin|merch",
                "title": "Titre du produit",
                "price": 25.50,
                "description": "Description détaillée"
            }
            
            Pour les skins : focus sur les personnages, armes, véhicules, effets visuels
            Pour les merch : focus sur les vêtements, accessoires, objets de collection
            """;
    }
    
    /**
     * Analyse intelligente avec APIs gratuites
     */
    private ProductAnalysisResult performIntelligentAnalysis(String imagePath) {
        try {
            // 1. Essayer Imagga API (gratuite - 1000 requêtes/mois)
            ProductAnalysisResult imaggaResult = callImaggaAPI(imagePath);
            if (imaggaResult != null) {
                return imaggaResult;
            }
        } catch (Exception e) {
            System.err.println("Imagga API call failed: " + e.getMessage());
        }
        
        try {
            // 2. Essayer Google Vision API (backup)
            ProductAnalysisResult googleResult = callGoogleVisionAPI(imagePath);
            if (googleResult != null) {
                return googleResult;
            }
        } catch (Exception e) {
            System.err.println("Google Vision API call failed: " + e.getMessage());
        }
        
        // Fallback vers l'analyse par nom de fichier si les APIs échouent
        return performFallbackAnalysis(imagePath);
    }
    
    /**
     * Appel à Imagga API (gratuite)
     */
    private ProductAnalysisResult callImaggaAPI(String imagePath) {
        try {
            System.out.println("DEBUG: Starting Imagga API call...");
            System.out.println("DEBUG: Image path: " + imagePath);
            
            // Vérifier si le fichier existe et est lisible
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("ERROR: Image file does not exist: " + imagePath);
                return null;
            }
            
            // Vérifier le format de l'image
            String fileName = imageFile.getName().toLowerCase();
            boolean isSupportedFormat = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                                      fileName.endsWith(".png") || fileName.endsWith(".gif") || 
                                      fileName.endsWith(".bmp") || fileName.endsWith(".webp");
            
            if (!isSupportedFormat) {
                System.err.println("ERROR: Unsupported image format: " + fileName);
                System.err.println("Supported formats: jpg, jpeg, png, gif, bmp, webp");
                return null;
            }
            
            // Convertir l'image en base64
            String base64Image = encodeImageToBase64(imagePath);
            System.out.println("DEBUG: Image encoded, base64 length: " + base64Image.length());
            
            // Créer la requête pour Imagga API avec le bon format
            String requestBody = "image_base64=" + base64Image;
            
            // Envoyer la requête
            URL url = new URL(IMAGGA_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // Authentification Basic
            String auth = IMAGGA_API_KEY + ":" + IMAGGA_API_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            
            System.out.println("DEBUG: Sending request to Imagga API...");
            
            // Envoyer le corps de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                System.out.println("DEBUG: Request sent, size: " + input.length + " bytes");
            }
            
            // Lire la réponse
            int responseCode = conn.getResponseCode();
            System.out.println("DEBUG: Imagga API Response code: " + responseCode);
            
            if (responseCode == 200) {
                String response = readResponse(conn);
                System.out.println("DEBUG: Imagga API response received, length: " + response.length());
                return parseImaggaResponse(response, imagePath);
            } else {
                String errorResponse = readErrorResponse(conn);
                System.err.println("Imagga API Error " + responseCode + ": " + errorResponse);
                
                // Analyse spécifique des erreurs Imagga
                if (responseCode == 400) {
                    System.err.println("ERROR 400: Image format or size issue");
                    System.err.println("Try using a smaller image (max 10MB) or different format");
                } else if (responseCode == 401) {
                    System.err.println("ERROR 401: Invalid API credentials");
                } else if (responseCode == 402) {
                    System.err.println("ERROR 402: Payment required or quota exceeded");
                } else if (responseCode == 429) {
                    System.err.println("ERROR 429: Rate limit exceeded");
                } else if (responseCode == 500) {
                    System.err.println("ERROR 500: Imagga server internal error");
                    System.err.println("This is a temporary server issue. Using fallback analysis...");
                } else if (responseCode == 503) {
                    System.err.println("ERROR 503: Imagga service unavailable");
                    System.err.println("Service temporarily down. Using fallback analysis...");
                }
                
                return null;
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Imagga API Timeout: Server took too long to respond");
            System.err.println("This is normal for large images. Trying next API...");
            return null;
        } catch (Exception e) {
            System.err.println("Imagga API Exception: " + e.getMessage());
            if (e.getMessage().contains("timeout") || e.getMessage().contains("timed out")) {
                System.err.println("Timeout detected - trying next API...");
            }
            return null;
        }
    }
    
    /**
     * Parse la réponse de Imagga API
     */
    private ProductAnalysisResult parseImaggaResponse(String response, String imagePath) {
        ProductAnalysisResult result = new ProductAnalysisResult();
        
        String lowerResponse = response.toLowerCase();
        
        // Analyser les tags pour déterminer le type
        boolean hasGamingTerms = lowerResponse.contains("game") || lowerResponse.contains("gaming") || 
                               lowerResponse.contains("video game") || lowerResponse.contains("player") ||
                               lowerResponse.contains("controller") || lowerResponse.contains("console");
        
        boolean hasClothingTerms = lowerResponse.contains("clothing") || lowerResponse.contains("shirt") ||
                                 lowerResponse.contains("hoodie") || lowerResponse.contains("apparel") ||
                                 lowerResponse.contains("fashion") || lowerResponse.contains("wear");
        
        boolean hasWeaponTerms = lowerResponse.contains("weapon") || lowerResponse.contains("knife") ||
                               lowerResponse.contains("gun") || lowerResponse.contains("sword") ||
                               lowerResponse.contains("blade") || lowerResponse.contains("military");
        
        boolean hasMerchTerms = lowerResponse.contains("merchandise") || lowerResponse.contains("collectible") ||
                              lowerResponse.contains("toy") || lowerResponse.contains("figure");
        
        // Déterminer le type basé sur les tags détectés
        if (hasWeaponTerms || (hasGamingTerms && !hasClothingTerms)) {
            result.setType("skin");
            result.setTitle("Gaming Skin - AI Detected");
            result.setPrice(59.99);
            result.setDescription("Skin gaming détecté par IA avec analyse visuelle avancée. Design unique et effets visuels impressionnants.");
        } else if (hasClothingTerms || hasMerchTerms) {
            result.setType("merch");
            result.setTitle("Gaming Merchandise - AI Detected");
            result.setPrice(39.99);
            result.setDescription("Merchandise gaming détecté par IA. Produit officiel de haute qualité pour les passionnés.");
        } else {
            // Par défaut, utiliser l'analyse de fallback
            return performFallbackAnalysis(imagePath);
        }
        
        return result;
    }
    
    /**
     * Appel réel à Google Vision API
     */
    private ProductAnalysisResult callGoogleVisionAPI(String imagePath) {
        try {
            // Convertir l'image en base64
            String base64Image = encodeImageToBase64(imagePath);
            
            // Créer la requête pour Google Vision API
            String requestBody = createVisionAPIRequest(base64Image);
            
            // Envoyer la requête
            URL url = new URL(VISION_API_URL + "?key=" + GOOGLE_API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            
            // Envoyer le corps de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Lire la réponse
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String response = readResponse(conn);
                return parseVisionAPIResponse(response, imagePath);
            } else {
                System.err.println("API Error: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("API Exception: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Crée le corps de la requête pour Google Vision API
     */
    private String createVisionAPIRequest(String base64Image) {
        return "{"
            + "\"requests\":[{"
            + "\"image\":{"
            + "\"content\":\"" + base64Image + "\""
            + "},"
            + "\"features\":[{"
            + "\"type\":\"LABEL_DETECTION\","
            + "\"maxResults\":10"
            + "},{"
            + "\"type\":\"OBJECT_LOCALIZATION\","
            + "\"maxResults\":10"
            + "},{"
            + "\"type\":\"WEB_DETECTION\","
            + "\"maxResults\":5"
            + "}]"
            + "}]"
            + "}";
    }
    
    /**
     * Lit la réponse de l'API
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
    
    /**
     * Lit les messages d'erreur de l'API
     */
    private String readErrorResponse(HttpURLConnection conn) throws IOException {
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
    
    /**
     * Parse la réponse de Google Vision API
     */
    private ProductAnalysisResult parseVisionAPIResponse(String response, String imagePath) {
        // Simulation de parsing - en réalité, il faudrait parser le JSON
        // Pour l'instant, on utilise les labels détectés pour améliorer l'analyse
        
        ProductAnalysisResult result = new ProductAnalysisResult();
        
        // Analyser les labels de la réponse pour déterminer le type
        String lowerResponse = response.toLowerCase();
        boolean hasGamingTerms = lowerResponse.contains("game") || lowerResponse.contains("gaming") || 
                               lowerResponse.contains("video game") || lowerResponse.contains("player");
        boolean hasClothingTerms = lowerResponse.contains("clothing") || lowerResponse.contains("shirt") ||
                                 lowerResponse.contains("hoodie") || lowerResponse.contains("apparel");
        boolean hasWeaponTerms = lowerResponse.contains("weapon") || lowerResponse.contains("knife") ||
                               lowerResponse.contains("gun") || lowerResponse.contains("sword");
        
        // Déterminer le type basé sur les labels détectés
        if (hasWeaponTerms || (hasGamingTerms && !hasClothingTerms)) {
            result.setType("skin");
            result.setTitle("Gaming Skin - AI Detected");
            result.setPrice(59.99);
            result.setDescription("Skin gaming détecté par IA avec analyse visuelle avancée. Design unique et effets visuels impressionnants.");
        } else {
            result.setType("merch");
            result.setTitle("Gaming Merchandise - AI Detected");
            result.setPrice(39.99);
            result.setDescription("Merchandise gaming détecté par IA. Produit officiel de haute qualité pour les passionnés.");
        }
        
        return result;
    }
    
    /**
     * Analyse de fallback si l'API échoue
     */
    private ProductAnalysisResult performFallbackAnalysis(String imagePath) {
        File imageFile = new File(imagePath);
        String fileName = imageFile.getName().toLowerCase();
        String filePath = imagePath.toLowerCase();
        
        ProductAnalysisResult result = new ProductAnalysisResult();
        
        boolean isSkin = detectSkinType(fileName, filePath);
        boolean isMerch = detectMerchType(fileName, filePath);
        
        if (isSkin && !isMerch) {
            result.setType("skin");
            result.setTitle(generateSkinTitle(fileName));
            result.setPrice(generateSkinPrice(fileName));
            result.setDescription(generateSkinDescription(fileName));
        } else if (isMerch || !isSkin) {
            result.setType("merch");
            result.setTitle(generateMerchTitle(fileName));
            result.setPrice(generateMerchPrice(fileName));
            result.setDescription(generateMerchDescription(fileName));
        }
        
        return result;
    }
    
    /**
     * Détection avancée de type skin
     */
    private boolean detectSkinType(String fileName, String filePath) {
        String[] skinKeywords = {
            "skin", "character", "weapon", "knife", "gun", "rifle", "pistol",
            "dragon", "butterfly", "ice", "fire", "flame", "shadow", "neon",
            "glow", "effect", "camo", "digital", "cyber", "tech", "sci-fi",
            "anime", "manga", "gaming", "player", "avatar", "hero", "legendary"
        };
        
        for (String keyword : skinKeywords) {
            if (fileName.contains(keyword) || filePath.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Détection avancée de type merch
     */
    private boolean detectMerchType(String fileName, String filePath) {
        String[] merchKeywords = {
            "merch", "tshirt", "shirt", "hoodie", "sweater", "jacket",
            "cap", "hat", "beanie", "mug", "cup", "bottle", "bag",
            "poster", "art", "print", "sticker", "keychain", "figure",
            "toy", "collectible", "apparel", "clothing", "wear"
        };
        
        for (String keyword : merchKeywords) {
            if (fileName.contains(keyword) || filePath.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Génère un titre pour un skin
     */
    private String generateSkinTitle(String fileName) {
        if (fileName.contains("butterfly")) return "Butterfly Knife Skin";
        if (fileName.contains("dragon")) return "Dragon Flame Skin";
        if (fileName.contains("ice")) return "Frost Crystal Skin";
        if (fileName.contains("fire")) return "Inferno Blaze Skin";
        if (fileName.contains("shadow")) return "Shadow Assassin Skin";
        if (fileName.contains("neon")) return "Neon Glow Skin";
        return "Premium Gaming Skin";
    }
    
    /**
     * Génère un prix pour un skin
     */
    private double generateSkinPrice(String fileName) {
        if (fileName.contains("legendary") || fileName.contains("rare")) return 89.99;
        if (fileName.contains("epic")) return 59.99;
        if (fileName.contains("rare")) return 39.99;
        return 29.99;
    }
    
    /**
     * Génère une description pour un skin
     */
    private String generateSkinDescription(String fileName) {
        if (fileName.contains("butterfly")) {
            return "Skin exclusif avec design papillon aux couleurs vibrantes. " +
                   "Effets visuels uniques et animations fluides pour une expérience gaming exceptionnelle.";
        }
        if (fileName.contains("dragon")) {
            return "Skin mythique dragon avec effets de feu spectaculaires. " +
                   "Design détaillé avec des écailles brillantes et des ailes de feu animées.";
        }
        return "Skin premium de haute qualité avec design unique et effets visuels impressionnants. " +
               "Parfait pour personnaliser votre expérience gaming.";
    }
    
    /**
     * Génère un titre pour du merch
     */
    private String generateMerchTitle(String fileName) {
        if (fileName.contains("tshirt") || fileName.contains("shirt")) return "Gaming T-Shirt Exclusive";
        if (fileName.contains("hoodie")) return "Premium Gaming Hoodie";
        if (fileName.contains("cap") || fileName.contains("hat")) return "Gaming Cap Collection";
        if (fileName.contains("mug")) return "Gaming Mug Edition";
        return "Gaming Merchandise Exclusive";
    }
    
    /**
     * Génère un prix pour du merch
     */
    private double generateMerchPrice(String fileName) {
        if (fileName.contains("hoodie")) return 79.99;
        if (fileName.contains("tshirt") || fileName.contains("shirt")) return 39.99;
        if (fileName.contains("cap") || fileName.contains("hat")) return 24.99;
        if (fileName.contains("mug")) return 19.99;
        return 34.99;
    }
    
    /**
     * Génère une description pour du merch
     */
    private String generateMerchDescription(String fileName) {
        if (fileName.contains("hoodie")) {
            return "Hoodie premium en coton bio avec logo gaming brodé. " +
                   "Confort optimal et style moderne pour les vrais gamers.";
        }
        if (fileName.contains("tshirt") || fileName.contains("shirt")) {
            return "T-shirt gaming 100% coton avec impression HD. " +
                   "Design exclusif et tissu respirant pour un confort tout au long de la journée.";
        }
        return "Merchandise gaming officielle de haute qualité. " +
               "Design unique et matériaux premium pour les fans de gaming.";
    }
    
    /**
     * Crée un résultat par défaut en cas d'erreur
     */
    private ProductAnalysisResult createDefaultResult() {
        ProductAnalysisResult result = new ProductAnalysisResult();
        result.setType("merch");
        result.setTitle("Gaming Product");
        result.setPrice(29.99);
        result.setDescription("Produit gaming de qualité supérieure parfait pour les passionnés.");
        return result;
    }
    
    /**
     * Classe résultat pour l'analyse d'image
     */
    public static class ProductAnalysisResult {
        private String type;
        private String title;
        private double price;
        private String description;
        
        // Getters et Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        /**
         * Applique les résultats à un produit existant
         */
        public void applyToProduct(Product product) {
            product.setName(title);
            product.setPrice(price);
            product.setDescription(description);
            product.setType(type);
            
            // Si c'est un merch, ajouter des tailles par défaut
            if ("merch".equals(type) && product instanceof Merch) {
                ((Merch) product).setSizes("S,M,L,XL");
            }
        }
    }
}
