package services;

import entities.Product;
import entities.Skin;
import entities.Merch;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    private ProductService productService;
    private Skin testSkin;
    private Merch testMerch;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
        
        // Créer un skin de test
        testSkin = new Skin(0, "Test Skin", 29.99, "A beautiful test skin", "http://example.com/image.jpg", 100);
        
        // Créer un merch de test
        testMerch = new Merch(0, "Test T-Shirt", 19.99, "A test t-shirt", "http://example.com/tshirt.jpg", 50, "S,M,L,XL");
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new skin successfully")
    void testCreateSkin() throws SQLException {
        // Act
        productService.add(testSkin);
        Skin createdSkin = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals(testSkin.getName()))
                .findFirst()
                .orElse(null);
        
        // Assert
        assertNotNull(createdSkin);
        assertNotNull(createdSkin.getId());
        assertEquals(testSkin.getName(), createdSkin.getName());
        assertEquals(testSkin.getPrice(), createdSkin.getPrice());
        assertEquals(testSkin.getStock(), createdSkin.getStock());
        assertEquals("skin", createdSkin.getType());
        
        // Clean up
        productService.delete(createdSkin.getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should retrieve all products")
    void testGetAllProducts() throws SQLException {
        // Arrange - Create test products first
        productService.add(testSkin);
        productService.add(testMerch);
        Skin createdSkin = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals(testSkin.getName()))
                .findFirst()
                .orElse(null);
        Merch createdMerch = (Merch) productService.getAll().stream()
                .filter(p -> p.getName().equals(testMerch.getName()))
                .findFirst()
                .orElse(null);
        
        // Act
        List<Product> products = productService.getAll();
        
        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.stream().anyMatch(p -> p.getId() == createdSkin.getId()));
        assertTrue(products.stream().anyMatch(p -> p.getId() == createdMerch.getId()));
        
        // Clean up
        productService.delete(createdSkin.getId());
        productService.delete(createdMerch.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Should update product stock correctly")
    void testUpdateProductStock() throws SQLException {
        // Arrange - Create a test product
        productService.add(testSkin);
        Skin createdSkin = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals(testSkin.getName()))
                .findFirst()
                .orElse(null);
        int initialStock = createdSkin.getStock();
        int reductionAmount = 10;
        
        // Act
        productService.reduceStock(createdSkin.getId(), reductionAmount);
        
        // Assert
        Product updatedProduct = productService.getById(createdSkin.getId());
        assertNotNull(updatedProduct);
        assertEquals(initialStock - reductionAmount, updatedProduct.getStock());
        
        // Clean up
        productService.delete(createdSkin.getId());
    }

    @Test
    @DisplayName("Should not reduce stock below zero")
    void testReduceStockBelowZero() throws SQLException {
        // Arrange - Create a product with low stock
        Skin lowStockSkin = new Skin(0, "Low Stock Skin", 15.99, "Low stock test", "http://example.com/low.jpg", 5);
        productService.add(lowStockSkin);
        Skin createdSkin = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals("Low Stock Skin"))
                .findFirst()
                .orElse(null);
        int initialStock = createdSkin.getStock();
        
        // Act - Try to reduce more than available
        productService.reduceStock(createdSkin.getId(), 10);
        
        // Assert - Stock should remain unchanged
        Product updatedProduct = productService.getById(createdSkin.getId());
        assertNotNull(updatedProduct);
        assertEquals(initialStock, updatedProduct.getStock());
        
        // Clean up
        productService.delete(createdSkin.getId());
    }

    @Test
    @DisplayName("Should find products by type")
    void testFindProductsByType() throws SQLException {
        // Arrange - Create products of different types
        productService.add(testSkin);
        productService.add(testMerch);
        Skin createdSkin = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals(testSkin.getName()))
                .findFirst()
                .orElse(null);
        Merch createdMerch = (Merch) productService.getAll().stream()
                .filter(p -> p.getName().equals(testMerch.getName()))
                .findFirst()
                .orElse(null);
        
        // Act
        List<Product> allProducts = productService.getAll();
        List<Skin> skins = allProducts.stream()
                .filter(p -> p instanceof Skin)
                .map(p -> (Skin) p)
                .toList();
        List<Merch> merch = allProducts.stream()
                .filter(p -> p instanceof Merch)
                .map(p -> (Merch) p)
                .toList();
        
        // Assert
        assertNotNull(skins);
        assertNotNull(merch);
        assertTrue(skins.stream().anyMatch(p -> p.getId() == createdSkin.getId()));
        assertTrue(merch.stream().anyMatch(p -> p.getId() == createdMerch.getId()));
        
        // Clean up
        productService.delete(createdSkin.getId());
        productService.delete(createdMerch.getId());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() throws SQLException {
        // Arrange - Create a test product
        productService.add(testSkin);
        Skin createdSkin = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals(testSkin.getName()))
                .findFirst()
                .orElse(null);
        Integer productId = createdSkin.getId();
        
        // Act
        productService.delete(productId);
        
        // Assert
        Product deletedProduct = productService.getById(productId);
        assertNull(deletedProduct);
    }
}
