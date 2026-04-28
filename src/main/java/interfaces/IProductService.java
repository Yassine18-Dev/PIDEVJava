package interfaces;

import entities.Product;
import java.util.List;

public interface IProductService {
    void add(Product p);
    void update(Product p);
    void delete(int id);
    List<Product> getAll();

    // ✅ FILTERS
    List<Product> getByType(String type);
    List<Product> getByPrice(double min, double max);
}