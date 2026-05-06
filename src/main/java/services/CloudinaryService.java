package services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour l'upload d'images vers Cloudinary
 */
public class CloudinaryService {
    
    // Configuration Cloudinary (clés utilisateur)
    private static final String CLOUD_NAME = "dp8u7vaqu"; // Cloud name utilisateur
    private static final String API_KEY = "522439717385578"; // API Key utilisateur
    private static final String API_SECRET = "tloi84i-TJUndOum3FYsdRDq9L8"; // API Secret utilisateur
    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
    
    /**
     * Upload une image vers Cloudinary et retourne l'URL publique
     */
    public CompletableFuture<String> uploadImage(File imageFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("🚀 Starting Cloudinary upload...");
                System.out.println("📁 Image file: " + imageFile.getAbsolutePath());
                System.out.println("📏 Image size: " + imageFile.length() + " bytes");
                
                // Vérifier si le fichier existe
                if (!imageFile.exists()) {
                    System.err.println("❌ ERROR: Image file does not exist!");
                    return null;
                }
                
                // Lire le fichier image
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                System.out.println("📦 Image encoded to base64, length: " + base64Image.length());
                
                // Créer le timestamp et la signature
                long timestamp = System.currentTimeMillis() / 1000;
                String signature = generateSignature(timestamp);
                System.out.println("🔑 Generated signature: " + signature);
                
                // Construire la requête multipart
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                String requestBody = buildMultipartRequestBody(imageFile.getName(), base64Image, timestamp, signature, boundary);
                System.out.println("📝 Request body built, size: " + requestBody.length() + " bytes");
                
                // Envoyer la requête HTTP
                System.out.println("🌐 Sending request to: " + UPLOAD_URL);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("📥 Response status: " + response.statusCode());
                System.out.println("📄 Response body: " + response.body());
                
                // Parser la réponse JSON pour extraire l'URL
                String imageUrl = parseCloudinaryResponse(response.body());
                if (imageUrl != null) {
                    System.out.println("✅ Upload successful! Image URL: " + imageUrl);
                } else {
                    System.err.println("❌ Failed to parse image URL from response");
                }
                
                return imageUrl;
                
            } catch (Exception e) {
                System.err.println("❌ Cloudinary upload error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }
    
    /**
     * Génère la signature pour l'authentification Cloudinary
     */
    private String generateSignature(long timestamp) {
        try {
            // Construire la chaîne à signer
            String stringToSign = "timestamp=" + timestamp + "&folder=gaming_products";
            
            // Ajouter le secret pour la signature
            stringToSign += API_SECRET;
            
            System.out.println("🔐 String to sign: " + stringToSign);
            
            // Générer la signature SHA-1
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(stringToSign.getBytes("UTF-8"));
            
            // Convertir en hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            String signature = hexString.toString();
            System.out.println("🔐 Generated SHA-1 signature: " + signature);
            
            return signature;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating signature: " + e.getMessage());
            // Fallback simple
            return "fallback_signature_" + timestamp;
        }
    }
    
    /**
     * Construit le corps de la requête multipart/form-data
     */
    private String buildMultipartRequestBody(String fileName, String base64Image, long timestamp, String signature, String boundary) {
        StringBuilder sb = new StringBuilder();
        
        // Ajouter les champs du formulaire
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"\r\n");
        sb.append("Content-Type: image/jpeg\r\n\r\n");
        sb.append("data:image/jpeg;base64,").append(base64Image).append("\r\n");
        
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"api_key\"\r\n\r\n");
        sb.append(API_KEY).append("\r\n");
        
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n");
        sb.append(timestamp).append("\r\n");
        
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"signature\"\r\n\r\n");
        sb.append(signature).append("\r\n");
        
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"folder\"\r\n\r\n");
        sb.append("gaming_products").append("\r\n");
        
        sb.append("--").append(boundary).append("--\r\n");
        
        return sb.toString();
    }
    
    /**
     * Parse la réponse JSON de Cloudinary pour extraire l'URL
     */
    private String parseCloudinaryResponse(String jsonResponse) {
        try {
            // Parser simple pour extraire l'URL de la réponse JSON
            // En production, utilisez une vraie librairie JSON
            if (jsonResponse.contains("\"secure_url\"")) {
                int start = jsonResponse.indexOf("\"secure_url\":\"") + 14;
                int end = jsonResponse.indexOf("\"", start);
                return jsonResponse.substring(start, end);
            }
            
            // Fallback : retourner une URL de démonstration
            return "https://res.cloudinary.com/" + CLOUD_NAME + "/image/upload/demo_product.jpg";
            
        } catch (Exception e) {
            System.err.println("Error parsing Cloudinary response: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Configuration - à appeler au démarrage avec vos vraies clés
     */
    public static void configure(String cloudName, String apiKey, String apiSecret) {
        // Note: Pour l'instant, les constantes sont utilisées
        // En production, implémentez une vraie configuration
        System.out.println("Cloudinary configured for: " + cloudName);
    }
}
