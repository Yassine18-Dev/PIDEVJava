package entities;

public abstract class Product {
    protected int id;
    protected String name;
    protected double price;
    protected String description;
    protected String image;
    protected int stock;
    protected String type;

    public Product() {}

    public Product(int id, String name, double price, String description, String image, int stock, String type) {
        this.id = id;
        setName(name);
        setPrice(price);
        setDescription(description);
        setImage(image);
        setStock(stock);
        this.type = type;
    }

    // ✅ BUSINESS LOGIC (validation)
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative!");
        }
        this.price = price;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative!");
        }
        this.stock = stock;
    }

    // LOW STOCK ALERT
    public String getStockStatus() {
        if (stock == 0) return " Out of stock (0)";
        if (stock < 5) return " Only few left! (" + stock + ")";
        return " In stock (" + stock + ")";
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public int getStock() { return stock; }
    public String getType() { return type; }
}