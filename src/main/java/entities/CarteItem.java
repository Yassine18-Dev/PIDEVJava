package entities;

/**
 * Entité représentant un article dans le panier
 */
public class CarteItem {
    private int id;
    private Product product;
    private int quantity;
    private double totalPrice;
    private String size;

    public CarteItem() {
    }

    public CarteItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = product.getPrice() * quantity;
    }

    public CarteItem(int id, Product product, int quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = product.getPrice() * quantity;
    }

    public CarteItem(Product product, int quantity, String size) {
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = product.getPrice() * quantity;
        this.size = size;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    // Méthode utilitaire pour mettre à jour le prix total
    private void updateTotalPrice() {
        if (product != null) {
            this.totalPrice = product.getPrice() * quantity;
        }
    }

    // Méthode pour incrémenter la quantité
    public void incrementQuantity() {
        this.quantity++;
        updateTotalPrice();
    }

    // Méthode pour décrémenter la quantité
    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            updateTotalPrice();
        }
    }

    @Override
    public String toString() {
        return "CarteItem{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
