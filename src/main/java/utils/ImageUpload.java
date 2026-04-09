package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class ImageUpload {

    public static String uploadImage(File sourceFile) throws IOException {

        String uploadDir = "uploads/";
        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdir();
        }

        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File destination = new File(uploadDir + fileName);

        Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return destination.getPath();
    }
    
    public static String handleImageUpload(Scanner scanner) {
        System.out.println("\n--- UPLOAD D'IMAGE ---");
        System.out.println("Options:");
        System.out.println("1. Entrer le chemin complet du fichier image");
        System.out.println("2. Utiliser une image par défaut");
        System.out.println("3. Annuler");
        System.out.print("Votre choix: ");
        
        int choice;
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Choix invalide. Utilisation d'une image par défaut.");
            return "default.png";
        }
        
        switch (choice) {
            case 1:
                return uploadFromPath(scanner);
            case 2:
                return getDefaultImage();
            case 3:
                return null;
            default:
                System.out.println("Choix invalide. Utilisation d'une image par défaut.");
                return "default.png";
        }
    }
    
    private static String uploadFromPath(Scanner scanner) {
        System.out.print("Entrez le chemin complet de l'image (ex: C:/Users/Desktop/image.jpg): ");
        String imagePath = scanner.nextLine().trim();
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            System.out.println("Fichier non trouvé! Utilisation d'une image par défaut.");
            return "default.png";
        }
        
        if (!isValidImageFile(imageFile)) {
            System.out.println("Type de fichier non valide! Utilisez: jpg, jpeg, png, gif. Image par défaut utilisée.");
            return "default.png";
        }
        
        try {
            String uploadedPath = uploadImage(imageFile);
            System.out.println("Image uploadée avec succès: " + uploadedPath);
            return uploadedPath;
        } catch (IOException e) {
            System.out.println("Erreur lors de l'upload: " + e.getMessage() + ". Image par défaut utilisée.");
            return "default.png";
        }
    }
    
    private static boolean isValidImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || 
               fileName.endsWith(".gif");
    }
    
    private static String getDefaultImage() {
        System.out.println("Image par défaut utilisée.");
        return "default.png";
    }
}