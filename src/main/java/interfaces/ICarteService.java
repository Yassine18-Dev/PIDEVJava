package interfaces;

import entities.CarteItem;
import entities.Product;
import java.util.List;

/**
 * Interface pour le service de gestion du panier
 */
public interface ICarteService {
    
    /**
     * Ajoute un produit au panier
     * @param product Le produit à ajouter
     * @param quantity La quantité à ajouter
     */
    void add(Product product, int quantity);
    
    /**
     * Supprime un article du panier
     * @param carteItem L'article à supprimer
     */
    void remove(CarteItem carteItem);
    
    /**
     * Met à jour la quantité d'un article
     * @param carteItem L'article à mettre à jour
     * @param newQuantity La nouvelle quantité
     */
    void updateQuantity(CarteItem carteItem, int newQuantity);
    
    /**
     * Vide le panier
     */
    void clear();
    
    /**
     * Récupère tous les articles du panier
     * @return La liste des articles
     */
    List<CarteItem> getAll();
    
    /**
     * Récupère le nombre total d'articles
     * @return Le nombre total
     */
    int getTotalItems();
    
    /**
     * Récupère le prix total du panier
     * @return Le prix total
     */
    double getTotalPrice();
    
    /**
     * Vérifie si le panier est vide
     * @return true si vide, false sinon
     */
    boolean isEmpty();
}
