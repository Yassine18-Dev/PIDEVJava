package entities;

public class Merch extends Product {
    private String sizes; // "S,M,L,XL"

    public Merch(int id, String name, double price, String description, String image, int stock, String sizes) {
        super(id, name, price, description, image, stock, "merch");
        this.sizes = sizes;
    }

    public String getSizes() {
        return sizes;
    }

    public void setSizes(String sizes) {
        this.sizes = sizes;
    }
}