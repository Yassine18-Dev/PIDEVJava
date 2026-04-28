package utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class TranslateService {
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    // Traduction simple avec Google Translate API (non officielle mais gratuite)
    public static String translate(String text, String targetLang) {
        try {
            // Détection automatique de la langue source
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format("%s?client=gtx&sl=auto&tl=%s&dt=t&q=%s", 
                GOOGLE_TRANSLATE_URL, targetLang, encodedText);

            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return parseGoogleTranslateResponse(responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur traduction: " + e.getMessage());
        }
        return text; // Retourne le texte original en cas d'erreur
    }

    // Parse la réponse de Google Translate
    private static String parseGoogleTranslateResponse(String responseBody) {
        try {
            // La réponse de Google Translate est un tableau complexe
            // Format: [[[["texte traduit", "langue source", ...], ...], ...], ...]
            if (responseBody.startsWith("[[[")) {
                int start = responseBody.indexOf("\"") + 1;
                int end = responseBody.indexOf("\"", start);
                if (end > start) {
                    return responseBody.substring(start, end);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing réponse: " + e.getMessage());
        }
        return "Traduction indisponible";
    }

    // Détecte la langue du texte (basé sur les premiers caractères)
    public static String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "auto";
        }
        
        String cleanText = text.trim().toLowerCase();
        
        // Détection simple basée sur les caractères
        if (cleanText.matches(".*[\u0600-\u06FF].*")) {
            return "ar"; // Contient des caractères arabes
        } else if (cleanText.matches(".*[a-zA-Z].*")) {
            // Si contient des lettres latines, vérifie si c'est probablement français
            String[] frenchWords = {"le", "la", "les", "de", "du", "des", "et", "est", "dans", "pour", "avec", "une", "un", "il", "elle", "nous", "vous", "ils", "elles"};
            for (String word : frenchWords) {
                if (cleanText.contains(" " + word + " ") || cleanText.startsWith(word + " ") || cleanText.endsWith(" " + word)) {
                    return "fr";
                }
            }
            return "en"; // Par défaut anglais si lettres latines
        }
        
        return "auto";
    }

    // Traduction intelligente selon la langue détectée
    public static String translateIntelligently(String text) {
        try {
            String detectedLang = detectLanguage(text);
            System.out.println("Langue détectée: " + detectedLang);
            
            switch (detectedLang) {
                case "fr":
                    // Si français, traduire en arabe et anglais
                    String arTranslation = translate(text, "ar");
                    String enTranslation = translate(text, "en");
                    return "AR: " + arTranslation + "\nEN: " + enTranslation;
                case "ar":
                    // Si arabe, traduire en français et anglais
                    String frTranslation = translate(text, "fr");
                    String enTranslation2 = translate(text, "en");
                    return "FR: " + frTranslation + "\nEN: " + enTranslation2;
                case "en":
                default:
                    // Si anglais ou autre, traduire en français et arabe
                    String frTranslation2 = translate(text, "fr");
                    String arTranslation2 = translate(text, "ar");
                    return "FR: " + frTranslation2 + "\nAR: " + arTranslation2;
            }
        } catch (Exception e) {
            System.err.println("Erreur traduction intelligente: " + e.getMessage());
            return "Traduction indisponible";
        }
    }

    public static String translateToFrench(String text) {
        return translate(text, "fr");
    }

    public static String translateToEnglish(String text) {
        return translate(text, "en");
    }

    public static String translateToArabic(String text) {
        return translate(text, "ar");
    }
}
