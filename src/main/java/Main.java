import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

import entities.Commentaire;
import entities.Like;
import entities.Post;
import services.CommentaireService;
import services.LikeService;
import services.PostService;
import validation.PostValidator;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final PostService postService = new PostService();
    private static final CommentaireService commentaireService = new CommentaireService();
    private static final LikeService likeService = new LikeService();
    
    private static final int CURRENT_USER_ID = 1;

    public static void main(String[] args) {
        try {
            menuPrincipal();
        } catch (SQLException e) {
            System.err.println("Erreur de base de données: " + e.getMessage());
        }
    }

    private static void menuPrincipal() throws SQLException {
        while (true) {
            System.out.println("\n=== MENU PRINCIPAL - FORUM ESPORTS ===");
            System.out.println("1. Gestion des posts");
            System.out.println("2. Gestion des commentaires");
            System.out.println("3. Gestion des likes");
            System.out.println("4. Afficher tous les posts avec statistiques");
            System.out.println("5. Quitter");
            System.out.print("Votre choix: ");

            int choix = lireEntier();
            
            switch (choix) {
                case 1 -> menuPosts();
                case 2 -> menuCommentaires();
                case 3 -> menuLikes();
                case 4 -> afficherPostsWithStats();
                case 5 -> {
                    System.out.println("Au revoir!");
                    return;
                }
                default -> System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static void menuPosts() throws SQLException {
        while (true) {
            System.out.println("\n=== GESTION DES POSTS ===");
            System.out.println("1. Créer un post");
            System.out.println("2. Modifier un post");
            System.out.println("3. Supprimer un post");
            System.out.println("4. Afficher tous les posts");
            System.out.println("5. Retour au menu principal");
            System.out.print("Votre choix: ");

            int choix = lireEntier();
            
            switch (choix) {
                case 1 -> creerPost();
                case 2 -> modifierPost();
                case 3 -> supprimerPost();
                case 4 -> afficherPosts();
                case 5 -> { return; }
                default -> System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static void creerPost() throws SQLException {
        System.out.println("\n--- Création d'un nouveau post ---");
        
        String titre = lireChaineNonVide("Titre (min 3 caractères): ");
        
        String contenu = lireChaineNonVide("Contenu: ");
        
        String imageUrl = lireChaine("URL de l'image (optionnel): ");
        if (imageUrl.isEmpty()) {
            imageUrl = null;
        }
        
        Post post = new Post(titre, contenu, imageUrl, new Timestamp(System.currentTimeMillis()), CURRENT_USER_ID);
        
        List<String> erreurs = PostValidator.valider(post);
        if (!erreurs.isEmpty()) {
            System.out.println("\nErreurs de validation:");
            for (String erreur : erreurs) {
                System.out.println("- " + erreur);
            }
            return;
        }
        
        postService.ajouter(post);
        System.out.println("Post créé avec succès!");
    }

    private static void modifierPost() throws SQLException {
        afficherPosts();
        System.out.print("ID du post à modifier: ");
        int id = lireEntier();
        
        Post post = postService.getById(id);
        if (post == null) {
            System.out.println("Post non trouvé.");
            return;
        }
        
        System.out.println("Post actuel: " + post.getTitre());
        String titre = lireChaine("Nouveau titre (laisser vide pour garder l'ancien): ");
        String contenu = lireChaine("Nouveau contenu (laisser vide pour garder l'ancien): ");
        String imageUrl = lireChaine("Nouvelle URL d'image (laisser vide pour garder l'ancienne): ");
        
        if (!titre.trim().isEmpty()) post.setTitre(titre);
        if (!contenu.trim().isEmpty()) post.setContenu(contenu);
        if (!imageUrl.trim().isEmpty()) post.setImageUrl(imageUrl);
        
        List<String> erreurs = PostValidator.valider(post);
        if (!erreurs.isEmpty()) {
            System.out.println("\nErreurs de validation:");
            for (String erreur : erreurs) {
                System.out.println("- " + erreur);
            }
            return;
        }
        
        postService.modifier(post);
    }

    private static void supprimerPost() throws SQLException {
        afficherPosts();
        System.out.print("ID du post à supprimer: ");
        int id = lireEntier();
        
        postService.supprimer(id);
    }

    private static void afficherPosts() throws SQLException {
        List<Post> posts = postService.afficher();
        if (posts.isEmpty()) {
            System.out.println("Aucun post trouvé.");
            return;
        }
        
        System.out.println("\n--- LISTE DES POSTS ---");
        for (Post post : posts) {
            int nbLikes = postService.getNombreLikes(post.getId());
            int nbCommentaires = postService.getNombreCommentaires(post.getId());
            
            System.out.println("ID: " + post.getId());
            System.out.println("Titre: " + post.getTitre());
            System.out.println("Contenu: " + (post.getContenu().length() > 100 ? 
                post.getContenu().substring(0, 100) + "..." : post.getContenu()));
            System.out.println("Date: " + post.getDateCreation());
            System.out.println("Likes: " + nbLikes + " | Commentaires: " + nbCommentaires);
            if (post.getImageUrl() != null) {
                System.out.println("Image: " + post.getImageUrl());
            }
            System.out.println("--------------------------------");
        }
    }

    private static void afficherPostsWithStats() throws SQLException {
        afficherPosts();
    }

    private static void menuCommentaires() throws SQLException {
        while (true) {
            System.out.println("\n=== GESTION DES COMMENTAIRES ===");
            System.out.println("1. Ajouter un commentaire");
            System.out.println("2. Modifier un commentaire");
            System.out.println("3. Supprimer un commentaire");
            System.out.println("4. Afficher les commentaires d'un post");
            System.out.println("5. Retour au menu principal");
            System.out.print("Votre choix: ");

            int choix = lireEntier();
            
            switch (choix) {
                case 1 -> ajouterCommentaire();
                case 2 -> modifierCommentaire();
                case 3 -> supprimerCommentaire();
                case 4 -> afficherCommentairesPost();
                case 5 -> { return; }
                default -> System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static void ajouterCommentaire() throws SQLException {
        afficherPosts();
        System.out.print("ID du post pour lequel commenter: ");
        int postId = lireEntier();
        
        if (postService.getById(postId) == null) {
            System.out.println("Post non trouvé.");
            return;
        }
        
        String contenu = lireChaineNonVide("Votre commentaire: ");
        
        Commentaire commentaire = new Commentaire(contenu, new Timestamp(System.currentTimeMillis()), postId, CURRENT_USER_ID);
        commentaireService.ajouter(commentaire);
    }

    private static void modifierCommentaire() throws SQLException {
        System.out.print("ID du commentaire à modifier: ");
        int id = lireEntier();
        
        Commentaire commentaire = commentaireService.getById(id);
        if (commentaire == null) {
            System.out.println("Commentaire non trouvé.");
            return;
        }
        
        System.out.println("Commentaire actuel: " + commentaire.getContenu());
        String contenu = lireChaineNonVide("Nouveau contenu: ");
        
        commentaire.setContenu(contenu);
        commentaireService.modifier(commentaire);
    }

    private static void supprimerCommentaire() throws SQLException {
        System.out.print("ID du commentaire à supprimer: ");
        int id = lireEntier();
        
        commentaireService.supprimer(id);
    }

    private static void afficherCommentairesPost() throws SQLException {
        afficherPosts();
        System.out.print("ID du post: ");
        int postId = lireEntier();
        
        List<Commentaire> commentaires = commentaireService.afficherParPost(postId);
        if (commentaires.isEmpty()) {
            System.out.println("Aucun commentaire pour ce post.");
            return;
        }
        
        System.out.println("\n--- COMMENTAIRES DU POST " + postId + " ---");
        for (Commentaire commentaire : commentaires) {
            System.out.println("ID: " + commentaire.getId());
            System.out.println("Contenu: " + commentaire.getContenu());
            System.out.println("Date: " + commentaire.getDateCreation());
            System.out.println("--------------------------------");
        }
    }

    private static void menuLikes() throws SQLException {
        while (true) {
            System.out.println("\n=== GESTION DES LIKES ===");
            System.out.println("1. Liker un post");
            System.out.println("2. Retirer son like");
            System.out.println("3. Vérifier si on a liké un post");
            System.out.println("4. Retour au menu principal");
            System.out.print("Votre choix: ");

            int choix = lireEntier();
            
            switch (choix) {
                case 1 -> likerPost();
                case 2 -> retirerLike();
                case 3 -> verifierLike();
                case 4 -> { return; }
                default -> System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static void likerPost() throws SQLException {
        afficherPosts();
        System.out.print("ID du post à liker: ");
        int postId = lireEntier();
        
        if (postService.getById(postId) == null) {
            System.out.println("Post non trouvé.");
            return;
        }
        
        Like like = new Like(postId, CURRENT_USER_ID, new Timestamp(System.currentTimeMillis()));
        likeService.ajouter(like);
    }

    private static void retirerLike() throws SQLException {
        afficherPosts();
        System.out.print("ID du post à unliker: ");
        int postId = lireEntier();
        
        likeService.supprimer(postId, CURRENT_USER_ID);
    }

    private static void verifierLike() throws SQLException {
        afficherPosts();
        System.out.print("ID du post à vérifier: ");
        int postId = lireEntier();
        
        boolean aLike = likeService.aLike(postId, CURRENT_USER_ID);
        System.out.println(aLike ? "Vous avez liké ce post." : "Vous n'avez pas liké ce post.");
    }

    private static int lireEntier() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Veuillez entrer un nombre valide: ");
            }
        }
    }

    private static String lireChaineNonVide(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Ce champ ne peut pas être vide. Veuillez réessayer.");
        }
    }

    private static String lireChaine(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
