package utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.util.Map;

public class CloudinaryService {
    private static Cloudinary cloudinary;

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "demo",
            "api_key", "876843748372837",
            "api_secret", "a676b5862b3789e6f8c9763f7b4e8f7"
        ));
    }

    public static String uploadImage(File imageFile) {
        try {
            System.out.println("Tentative d'upload: " + imageFile.getAbsolutePath());
            System.out.println("Fichier existe: " + imageFile.exists());
            System.out.println("Taille: " + imageFile.length() + " bytes");
            
            Map uploadResult = cloudinary.uploader().upload(imageFile, ObjectUtils.asMap(
                "folder", "blog_posts",
                "resource_type", "auto",
                "transformation", ObjectUtils.asMap(
                    "width", 800,
                    "height", 600,
                    "crop", "limit"
                )
            ));
            
            String url = (String) uploadResult.get("url");
            System.out.println("Upload réussi! URL: " + url);
            return url;
        } catch (Exception e) {
            System.err.println("Erreur upload Cloudinary: " + e.getMessage());
            System.err.println("Utilisation du mode simulation - URL locale");
            e.printStackTrace();
            
            // Mode simulation : retourner une URL de test
            return "https://picsum.photos/seed/" + System.currentTimeMillis() + "/800/600.jpg";
        }
    }

    public static String uploadImage(String imagePath) {
        return uploadImage(new File(imagePath));
    }
}
