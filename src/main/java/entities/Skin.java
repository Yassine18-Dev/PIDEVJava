package entities;

public class Skin extends Product {

    public Skin(int id, String name, double price, String description, String image, int stock) {
        super(id, name, price, description, image, stock, "skin");
    }
}