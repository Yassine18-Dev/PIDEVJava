package validation;

import java.util.ArrayList;
import java.util.List;

import entities.Post;

@SuppressWarnings("unused")
public class PostValidator {
    
    public static List<String> valider(Post post) {
        List<String> erreurs = new ArrayList<>();
        
        if (post.getTitre() == null || post.getTitre().trim().isEmpty()) {
            erreurs.add("Le titre ne peut pas être vide.");
        } else if (post.getTitre().trim().length() < 3) {
            erreurs.add("Le titre doit contenir au moins 3 caractères.");
        }
        
        if (post.getContenu() == null || post.getContenu().trim().isEmpty()) {
            erreurs.add("Le contenu est obligatoire.");
        }
        
        if (post.getImageUrl() != null && !post.getImageUrl().trim().isEmpty()) {
            if (!validerUrl(post.getImageUrl())) {
                erreurs.add("L'URL de l'image n'est pas valide.");
            }
        }
        
        return erreurs;
    }
    
    private static boolean validerUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            // Valider simplement en essayant de créer l'URL
            java.net.URL urlCheck = new java.net.URL(url.trim());
            return urlCheck.getProtocol() != null;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }
    
    public static boolean estValide(Post post) {
        return valider(post).isEmpty();
    }
}
