package services;

import entities.*;
import interfaces.IProductService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService implements IProductService {

    Connection cnx = Mydatabase.getInstance().getCnx();

    // ✅ CREATE
    @Override
    public void add(Product p) {
        String sql = "INSERT INTO product (name, price, description, image, stock, type, sizes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getImage());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getType());

            if (p instanceof Merch) {
                ps.setString(7, ((Merch) p).getSizes());
            } else {
                ps.setString(7, null);
            }

            ps.executeUpdate();
            System.out.println("✅ Product added!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ UPDATE
    @Override
    public void update(Product p) {
        String sql = "UPDATE product SET name=?, price=?, description=?, image=?, stock=?, sizes=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getImage());
            ps.setInt(5, p.getStock());

            if (p instanceof Merch) {
                ps.setString(6, ((Merch) p).getSizes());
            } else {
                ps.setString(6, null);
            }

            ps.setInt(7, p.getId());

            ps.executeUpdate();
            System.out.println("✅ Product updated!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM product WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Product deleted!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ALL
    @Override
    public List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM product";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                String type = rs.getString("type");

                if ("merch".equals(type)) {
                    list.add(new Merch(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("description"),
                            rs.getString("image"),
                            rs.getInt("stock"),
                            rs.getString("sizes")
                    ));
                } else {
                    list.add(new Skin(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("description"),
                            rs.getString("image"),
                            rs.getInt("stock")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // ✅ FILTER BY TYPE
    @Override
    public List<Product> getByType(String type) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : getAll()) {
            if (p.getType().equalsIgnoreCase(type)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    // ✅ FILTER BY PRICE
    @Override
    public List<Product> getByPrice(double min, double max) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : getAll()) {
            if (p.getPrice() >= min && p.getPrice() <= max) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    // GET BY ID
    public Product getById(int id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String type = rs.getString("type");
                
                if ("merch".equals(type)) {
                    return new Merch(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("description"),
                            rs.getString("image"),
                            rs.getInt("stock"),
                            rs.getString("sizes")
                    );
                } else {
                    return new Skin(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("description"),
                            rs.getString("image"),
                            rs.getInt("stock")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return null;
    }

    // CART LOGIC (reduce stock)
    public void reduceStock(int productId, int quantity) {
        reduceStock(productId, quantity, null);
    }
    
    public void reduceStock(int productId, int quantity, String size) {
        String sql = "UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(" Purchase successful!" + (size != null ? " (Taille: " + size + ")" : ""));
            } else {
                System.out.println(" Not enough stock!");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}