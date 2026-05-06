package services;

import entities.CarteItem;
import entities.Skin;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarteServiceTest {

    private CarteService carteService;
    private ProductService productService;
    private Skin testProduct;

    @BeforeEach
    void setUp() throws SQLException {
        carteService = CarteService.getInstance();
        productService = new ProductService();
        
        // Clear the cart before each test
        carteService.clear();
        
        // Create a test product
        testProduct = new Skin(0, "Test Cart Skin", 15.99, "A product for cart testing", "http://example.com/cart.jpg", 50);
        
        // Create the product in database
        productService.add(testProduct);
        testProduct = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals("Test Cart Skin"))
                .findFirst()
                .orElse(null);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up cart
        carteService.clear();
        
        // Clean up test product
        if (testProduct != null && testProduct.getId() > 0) {
            productService.delete(testProduct.getId());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should add product to cart successfully")
    void testAddProductToCart() {
        // Act
        carteService.add(testProduct, 2);
        
        // Assert
        List<CarteItem> cartItems = carteService.getAll();
        assertEquals(1, cartItems.size());
        
        CarteItem cartItem = cartItems.get(0);
        assertEquals(testProduct.getId(), cartItem.getProduct().getId());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(31.98, cartItem.getTotalPrice(), 0.001); // 15.99 * 2
    }

    @Test
    @Order(2)
    @DisplayName("Should update quantity when adding same product")
    void testAddSameProductIncreasesQuantity() {
        // Act - Add the same product twice
        carteService.add(testProduct, 2);
        carteService.add(testProduct, 3);
        
        // Assert
        List<CarteItem> cartItems = carteService.getAll();
        assertEquals(1, cartItems.size());
        
        CarteItem cartItem = cartItems.get(0);
        assertEquals(5, cartItem.getQuantity()); // 2 + 3
        assertEquals(79.95, cartItem.getTotalPrice(), 0.001); // 15.99 * 5
    }

    @Test
    @Order(3)
    @DisplayName("Should add product with size correctly")
    void testAddProductWithSize() {
        // Act
        carteService.add(testProduct, 1, "M");
        
        // Assert
        List<CarteItem> cartItems = carteService.getAll();
        assertEquals(1, cartItems.size());
        
        CarteItem cartItem = cartItems.get(0);
        assertEquals("M", cartItem.getSize());
        assertEquals(1, cartItem.getQuantity());
        assertEquals(15.99, cartItem.getTotalPrice(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total price correctly")
    void testCalculateTotalPrice() throws SQLException {
        // Arrange - Add multiple products
        Skin product2 = new Skin(0, "Second Test Skin", 25.00, "Another test product", "http://example.com/product2.jpg", 30);
        productService.add(product2);
        product2 = (Skin) productService.getAll().stream()
                .filter(p -> p.getName().equals("Second Test Skin"))
                .findFirst()
                .orElse(null);
        
        // Act
        carteService.add(testProduct, 2); // 15.99 * 2 = 31.98
        carteService.add(product2, 1);   // 25.00 * 1 = 25.00
        
        // Assert
        double expectedTotal = 31.98 + 25.00;
        assertEquals(expectedTotal, carteService.getTotalPrice(), 0.001);
        assertEquals(3, carteService.getTotalItems()); // 2 + 1
        
        // Clean up
        productService.delete(product2.getId());
    }

    @Test
    @DisplayName("Should remove item from cart")
    void testRemoveItemFromCart() {
        // Arrange
        carteService.add(testProduct, 2);
        List<CarteItem> cartItems = carteService.getAll();
        CarteItem itemToRemove = cartItems.get(0);
        
        // Act
        carteService.remove(itemToRemove);
        
        // Assert
        assertTrue(carteService.isEmpty());
        assertEquals(0, carteService.getTotalItems());
        assertEquals(0.0, carteService.getTotalPrice(), 0.001);
    }

    @Test
    @DisplayName("Should update item quantity")
    void testUpdateItemQuantity() {
        // Arrange
        carteService.add(testProduct, 2);
        List<CarteItem> cartItems = carteService.getAll();
        CarteItem itemToUpdate = cartItems.get(0);
        
        // Act
        carteService.updateQuantity(itemToUpdate, 5);
        
        // Assert
        List<CarteItem> updatedItems = carteService.getAll();
        assertEquals(1, updatedItems.size());
        assertEquals(5, updatedItems.get(0).getQuantity());
        assertEquals(79.95, updatedItems.get(0).getTotalPrice(), 0.001); // 15.99 * 5
    }

    @Test
    @DisplayName("Should remove item when quantity is zero or negative")
    void testUpdateQuantityToZeroRemovesItem() {
        // Arrange
        carteService.add(testProduct, 2);
        List<CarteItem> cartItems = carteService.getAll();
        CarteItem itemToUpdate = cartItems.get(0);
        
        // Act
        carteService.updateQuantity(itemToUpdate, 0);
        
        // Assert
        assertTrue(carteService.isEmpty());
    }

    @Test
    @DisplayName("Should clear cart completely")
    void testClearCart() {
        // Arrange
        carteService.add(testProduct, 2);
        assertFalse(carteService.isEmpty());
        
        // Act
        carteService.clear();
        
        // Assert
        assertTrue(carteService.isEmpty());
        assertEquals(0, carteService.getAll().size());
        assertEquals(0, carteService.getTotalItems());
        assertEquals(0.0, carteService.getTotalPrice(), 0.001);
    }

    @Test
    @DisplayName("Should handle empty cart correctly")
    void testEmptyCartBehavior() {
        // Assert - Initially empty
        assertTrue(carteService.isEmpty());
        assertEquals(0, carteService.getAll().size());
        assertEquals(0, carteService.getTotalItems());
        assertEquals(0.0, carteService.getTotalPrice(), 0.001);
    }
}
