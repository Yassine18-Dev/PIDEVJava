package services;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Service moderne d'analyse d'image : Upload Cloudinary + Analyse Gemini API
 */
public class ModernImageAnalysisService {
    
    private final CloudinaryService cloudinaryService;
    private final GeminiAPIService geminiAPIService;
    
    public ModernImageAnalysisService() {
        this.cloudinaryService = new CloudinaryService();
        this.geminiAPIService = new GeminiAPIService();
        
        // Configuration Cloudinary (à remplacer avec vos vraies clés)
        CloudinaryService.configure("dp8u7vaqu", "522439717385578", "tloi84i-TJUndOum3FYsdRDq9L8");
    }
    
    /**
     * Analyse complète : Upload vers Cloudinary + Analyse via Gemini
     */
    public CompletableFuture<GeminiAPIService.ProductAnalysisResult> analyzeImage(File imageFile) {
        return cloudinaryService.uploadImage(imageFile)
            .thenCompose(imageUrl -> {
                if (imageUrl == null) {
                    // Fallback si l'upload échoue
                    return CompletableFuture.completedFuture(createFallbackResult());
                }
                
                System.out.println("✅ Image uploaded to Cloudinary: " + imageUrl);
                System.out.println("🤖 Analyzing with Gemini API...");
                
                // Analyser l'image via Gemini API
                return geminiAPIService.analyzeImageFromUrl(imageUrl);
            })
            .exceptionally(throwable -> {
                System.err.println("❌ Analysis failed: " + throwable.getMessage());
                return createFallbackResult();
            });
    }
    
    /**
     * Analyse rapide avec une URL existante (pas d'upload)
     */
    public CompletableFuture<GeminiAPIService.ProductAnalysisResult> analyzeImageFromUrl(String imageUrl) {
        System.out.println("🤖 Analyzing existing image URL with Gemini API...");
        return geminiAPIService.analyzeImageFromUrl(imageUrl)
            .exceptionally(throwable -> {
                System.err.println("❌ Analysis failed: " + throwable.getMessage());
                return createFallbackResult();
            });
    }
    
    /**
     * Crée un résultat par défaut
     */
    private GeminiAPIService.ProductAnalysisResult createFallbackResult() {
        GeminiAPIService.ProductAnalysisResult result = new GeminiAPIService.ProductAnalysisResult();
        result.setName("Gaming Product");
        result.setDescription("High-quality gaming product for enthusiasts.");
        result.setType("merch");
        result.setPrice(39.99);
        return result;
    }
}
