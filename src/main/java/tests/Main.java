package tests;

import entities.*;
import services.ProductService;
import utils.ImageUpload;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    private static ProductService ps = new ProductService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        
        while (running) {
            showMainMenu();
            int choice = getIntInput(1, 3);
            
            switch (choice) {
                case 1:
                    showProductsMenu();
                    break;
                case 2:
                    showProductManagementMenu();
                    break;
                case 3:
                    System.out.println("Au revoir!");
                    running = false;
                    break;
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
            }
        }   
        scanner.close();
    }
    
    
    /**
     * Affiche le menu principal de l'application
     */
    private static void showMainMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Afficher les produits");
        System.out.println("2. Gestion des produits");
        System.out.println("3. Quitter");
        System.out.print("Votre choix: ");
    }
    
                        
    /**
     * Affiche le menu d'affichage des produits
     * Permet de choisir entre tous les produits, merch seulement, ou skins seulement
     */
    private static void showProductsMenu() {
        System.out.println("\n--- AFFICHER LES PRODUITS ---");
        System.out.println("1. Afficher tous les produits");
        System.out.println("2. Afficher les Merch");
        System.out.println("3. Afficher les Skins");
        System.out.println("4. Retour au menu principal");
        System.out.print("Votre choix: ");
        
        int choice = getIntInput(1, 4);
        
        switch (choice) {
            case 1:
                displayAllProducts();
                break;
            case 2:
                displayMerchProducts();
                break;
            case 3:
                displaySkinProducts();
                break;
            case 4:
                return;
            default:
                System.out.println("Option invalide.");
        }
    }
    
    /**
     * Affiche tous les produits avec leurs informations complètes
     */
    private static void displayAllProducts() {
        System.out.println("\n--- TOUS LES PRODUITS ---");
        for (Product p : ps.getAll()) {
            String display = p.getName() + " - " + p.getStockStatus() + " - " + p.getPrice() + "TND";
            if (p instanceof Merch) {
                display += " - Tailles: " + ((Merch) p).getSizes();
            }
            System.out.println(display);
        }
    }
    
    /**
     * Affiche uniquement les produits de type Merch
     */
    private static void displayMerchProducts() {
        System.out.println("\n--- MERCH ---");
        for (Product p : ps.getByType("merch")) {
            System.out.println(p.getName() + " - " + p.getStockStatus() + " - " + p.getPrice() + "TND - Tailles: " + ((Merch) p).getSizes());
        }
    }
    
    /**
     * Affiche uniquement les produits de type Skin
     */
    private static void displaySkinProducts() {
        System.out.println("\n--- SKINS ---");
        for (Product p : ps.getByType("skin")) {
            System.out.println(p.getName() + " - " + p.getStockStatus() + " - " + p.getPrice() + "TND");
        }
    }
    
    // ==================== MENU GESTION PRODUITS ===================
    
    /**
     * Affiche le menu de gestion des produits
     * Permet d'accéder à toutes les opérations CRUD
     */
    private static void showProductManagementMenu() {
        boolean managing = true;
        
        while (managing) {
            System.out.println("\n--- GESTION DES PRODUITS ---");
            System.out.println("1. Ajouter un produit");
            System.out.println("2. Modifier un produit");
            System.out.println("3. Supprimer un produit");
            System.out.println("4. Lister tous les produits");
            System.out.println("5. Réduire le stock (achat)");
            System.out.println("6. Retour au menu principal");
            System.out.print("Votre choix: ");
            
            int choice = getIntInput(1, 6);
            
            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    updateProduct();
                    break;
                case 3:
                    deleteProduct();
                    break;
                case 4:
                    listAllProducts();
                    break;
                case 5:
                    reduceStock();
                    break;
                case 6:
                    managing = false;
                    break;
                default:
                    System.out.println("Option invalide.");
            }
        }
    }
    
    // ==================== OPÉRATIONS CRUD ===================
    
    /**
     * Ajoute un nouveau produit dans la base de données
     * Permet de créer des Skins ou des Merch avec upload d'image
     */
    private static void addProduct() {
        System.out.println("\n--- AJOUTER UN PRODUIT ---");
        System.out.println("1. Skin");
        System.out.println("2. Merch");
        System.out.print("Type de produit: ");
        
        int type = getIntInput(1, 2);
        
        System.out.print("Nom: ");
        String name = getNonEmptyInput("Nom");
        
        System.out.print("Prix: ");
        double price = getPositiveDoubleInput("Prix");
        
        System.out.print("Description: ");
        String description = getNonEmptyInput("Description");
        
        String image = ImageUpload.handleImageUpload(scanner);
        if (image == null) {
            System.out.println("Opération annulée.");
            return;
        }
        
        System.out.print("Stock: ");
        int stock = getPositiveIntInput("Stock");
        
        if (type == 1) {
            Product skin = new Skin(0, name, price, description, image, stock);
            ps.add(skin);
            System.out.println("Skin ajouté avec succès!");
        } else if (type == 2) {
            System.out.print("Tailles (ex: S,M,L): ");
            String sizes = getNonEmptyInput("Tailles");
            Product merch = new Merch(0, name, price, description, image, stock, sizes);
            ps.add(merch);
            System.out.println("Merch ajouté avec succès!");
        }
    }
    
    // ==================== OPÉRATION MISE À JOUR ===================
    
    /**
     * Modifie un produit existant dans la base de données
     * Permet de mettre à jour toutes les informations y compris l'image
     */
    private static void updateProduct() {
        System.out.println("\n--- MODIFIER UN PRODUIT ---");
        listAllProducts();
        System.out.print("ID du produit à modifier: ");
        int id = getPositiveIntInput("ID");
        
        Product product = ps.getById(id);
        if (product == null) {
            System.out.println("Produit non trouvé!");
            return;
        }
        
        System.out.println("Informations actuelles:");
        System.out.println("Nom: " + product.getName());
        System.out.println("Prix: " + product.getPrice() + "TND");
        System.out.println("Description: " + product.getDescription());
        System.out.println("Image: " + product.getImage());
        System.out.println("Stock: " + product.getStock());
        
        System.out.println("\nNouvelles informations (laissez vide pour conserver la valeur actuelle):");
        
        System.out.print("Nouveau nom [" + product.getName() + "]: ");
        String name = getOptionalInput(product.getName());
        
        System.out.print("Nouveau prix [" + product.getPrice() + "]: ");
        String priceInput = scanner.nextLine().trim();
        double price = priceInput.isEmpty() ? product.getPrice() : getPositiveDoubleFromInput(priceInput);
        
        System.out.print("Nouvelle description [" + product.getDescription() + "]: ");
        String description = getOptionalInput(product.getDescription());
        
        System.out.println("\n--- MODIFICATION DE L'IMAGE ---");
        System.out.println("1. Conserver l'image actuelle: " + product.getImage());
        System.out.println("2. Changer l'image");
        System.out.print("Votre choix: ");
        
        int imageChoice = getIntInput(1, 2);
        String image = product.getImage();
        
        if (imageChoice == 2) {
            String newImage = ImageUpload.handleImageUpload(scanner);
            if (newImage == null) {
                System.out.println("Opération annulée. Image conservée.");
            } else {
                image = newImage;
            }
        }
        
        System.out.print("Nouveau stock [" + product.getStock() + "]: ");
        String stockInput = scanner.nextLine().trim();
        int stock = stockInput.isEmpty() ? product.getStock() : getPositiveIntFromInput(stockInput);
        
        // Mettre à jour le produit
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setImage(image);
        product.setStock(stock);
        ps.update(product);
        System.out.println("Produit modifié avec succès!");
    }
    
    // ==================== OPÉRATIONS SUPPRESSION ET LISTAGE ===================
    
    /**
     * Supprime un produit de la base de données
     */
    private static void deleteProduct() {
        System.out.println("\n--- SUPPRIMER UN PRODUIT ---");
        listAllProducts();
        System.out.print("ID du produit à supprimer: ");
        int id = getPositiveIntInput("ID");
        
        ps.delete(id);
        System.out.println("Produit supprimé avec succès!");
    }
    
    /**
     * Affiche la liste complète des produits avec ID
     */
    private static void listAllProducts() {
        System.out.println("\n--- LISTE DES PRODUITS ---");
        for (Product p : ps.getAll()) {
            String display = "ID: " + p.getId() + " | " + p.getName() + " | " + p.getPrice() + "TND | " + p.getStockStatus();
            
            // Ajouter les tailles pour les produits Merch
            if (p instanceof Merch) {
                display += " | Tailles: " + ((Merch) p).getSizes();
            }
            
            System.out.println(display);
        }
    }
    
    // ==================== OPÉRATION RÉDUCTION STOCK ===================
    
    /**
     * Réduit le stock d'un produit (simulation d'achat)
     * Pour les Merch, permet de sélectionner une taille spécifique
     */
    private static void reduceStock() {
        System.out.println("\n--- RÉDUIRE LE STOCK ---");
        listAllProducts();
        System.out.print("ID du produit: ");
        int productId = getPositiveIntInput("ID");
        
        Product product = ps.getById(productId);
        if (product == null) {
            System.out.println("Produit non trouvé!");
            return;
        }
        
        // Si c'est un Merch, demander la taille
        String selectedSize = null;
        if (product instanceof Merch) {
            selectedSize = selectSizeForMerch((Merch) product);
            if (selectedSize == null) {
                return; // Utilisateur a annulé
            }
        }
        
        System.out.print("Quantité à réduire: ");
        int quantity = getPositiveIntInput("Quantité");
        
        // Passer la taille sélectionnée au service
        ps.reduceStock(productId, quantity, selectedSize);
    }
    
    // ==================== SÉLECTION TAILLE MERCH ===================
    
    /**
     * Affiche le menu de sélection des tailles pour les produits Merch
     
     */
    private static String selectSizeForMerch(Merch merch) {
        String[] sizes = merch.getSizes().split(",");
        
        System.out.println("\n--- SÉLECTIONNER LA TAILLE ---");
        System.out.println("Tailles disponibles pour " + merch.getName() + ":");
        
        for (int i = 0; i < sizes.length; i++) {
            System.out.println((i + 1) + ". " + sizes[i].trim());
        }
        System.out.println((sizes.length + 1) + ". Annuler");
        System.out.print("Votre choix: ");
        
        int choice = getIntInput(1, sizes.length + 1);
        
        if (choice == sizes.length + 1) {
            System.out.println("Opération annulée.");
            return null;
        }
        
        return sizes[choice - 1].trim();
    }
    
    
    /**
     * Valide et récupère un entier dans une plage donnée
     
     */
    private static int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.println("Veuillez entrer un nombre compris entre " + min + " et " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Veuillez entrer un nombre.");
                scanner.next();
            }
        }
    }
    
    /**
     * Valide et récupère un nombre décimal positif
     
     */
    private static double getPositiveDoubleInput(String field) {
        while (true) {
            try {
                double input = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                if (input > 0) {
                    return input;
                } else {
                    System.out.println("Veuillez entrer un nombre positif pour le champ '" + field + "'.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Veuillez entrer un nombre pour le champ '" + field + "'.");
                scanner.next();
            }
        }
    }
    
    /**
     * Valide et récupère un entier positif
     
     */
    private static int getPositiveIntInput(String field) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (input > 0) {
                    return input;
                } else {
                    System.out.println("Veuillez entrer un nombre positif pour le champ '" + field + "'.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Veuillez entrer un nombre pour le champ '" + field + "'.");
                scanner.next();
            }
        }
    }
    
    /**
     * Valide et récupère une chaîne non vide
     
     */
    private static String getNonEmptyInput(String field) {
        while (true) {
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                return input;
            } else {
                System.out.println("Veuillez entrer une valeur non vide pour le champ '" + field + "'.");
            }
        }
    }
    
    /**
     * Récupère une entrée optionnelle avec valeur par défaut
     
     */
    private static String getOptionalInput(String currentValue) {
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }
    
    /**
     * Convertit et valide une chaîne en nombre décimal positif
     
     */
    private static double getPositiveDoubleFromInput(String input) {
        try {
            double value = Double.parseDouble(input);
            if (value > 0) {
                return value;
            } else {
                System.out.println("Veuillez entrer un nombre positif.");
                return getPositiveDoubleInput("Valeur");
            }
        } catch (NumberFormatException e) {
            System.out.println("Format invalide. Veuillez entrer un nombre.");
            return getPositiveDoubleInput("Valeur");
        }
    }
    
    /**
     * Convertit et valide une chaîne en entier positif
    
     */
    private static int getPositiveIntFromInput(String input) {
        try {
            int value = Integer.parseInt(input);
            if (value > 0) {
                return value;
            } else {
                System.out.println("Veuillez entrer un nombre positif.");
                return getPositiveIntInput("Valeur");
            }
        } catch (NumberFormatException e) {
            System.out.println("Format invalide. Veuillez entrer un nombre.");
            return getPositiveIntInput("Valeur");
        }
    }
}
