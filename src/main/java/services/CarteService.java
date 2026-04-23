package services;

import entities.CarteItem;
import entities.Product;
import interfaces.ICarteService;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion du panier (implémentation en mémoire)
 */
public class CarteService implements ICarteService {
    
    private List<CarteItem> carteItems;
    private static CarteService instance;
    
    private CarteService() {
        this.carteItems = new ArrayList<>();
    }
    
    /**
     * Récupère l'instance singleton du service
     * @return L'instance du service
     */
    public static CarteService getInstance() {
        if (instance == null) {
            instance = new CarteService();
        }
        return instance;
    }
    
    @Override
    public void add(Product product, int quantity) {
        // Vérifier si le produit est déjà dans le panier
        for (CarteItem item : carteItems) {
            if (item.getProduct().getId() == product.getId()) {
                // Mettre à jour la quantité
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        // Ajouter un nouvel article
        CarteItem newItem = new CarteItem(product, quantity);
        carteItems.add(newItem);
    }

    public void add(Product product, int quantity, String size) {
        // Vérifier si le produit est déjà dans le panier avec la même taille
        for (CarteItem item : carteItems) {
            if (item.getProduct().getId() == product.getId()) {
                if (size != null && size.equals(item.getSize())) {
                    // Mettre à jour la quantité
                    item.setQuantity(item.getQuantity() + quantity);
                    return;
                }
            }
        }

        // Ajouter un nouvel article avec taille
        CarteItem newItem = new CarteItem(product, quantity, size);
        carteItems.add(newItem);
    }
    
    @Override
    public void remove(CarteItem carteItem) {
        carteItems.remove(carteItem);
    }
    
    @Override
    public void updateQuantity(CarteItem carteItem, int newQuantity) {
        if (newQuantity <= 0) {
            remove(carteItem);
        } else {
            carteItem.setQuantity(newQuantity);
        }
    }
    
    @Override
    public void clear() {
        carteItems.clear();
    }
    
    @Override
    public List<CarteItem> getAll() {
        return new ArrayList<>(carteItems);
    }
    
    @Override
    public int getTotalItems() {
        return carteItems.stream()
                .mapToInt(CarteItem::getQuantity)
                .sum();
    }
    
    @Override
    public double getTotalPrice() {
        return carteItems.stream()
                .mapToDouble(CarteItem::getTotalPrice)
                .sum();
    }
    
    @Override
    public boolean isEmpty() {
        return carteItems.isEmpty();
    }
}
