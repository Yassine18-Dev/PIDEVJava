package services;

import entities.PromoCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des codes promo
 */
public class PromoCodeService {
    private static PromoCodeService instance;
    private Connection connection;

    private PromoCodeService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidev", "root", "");
            createTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    public static PromoCodeService getInstance() {
        if (instance == null) {
            instance = new PromoCodeService();
        }
        return instance;
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS promo_codes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "code VARCHAR(50) UNIQUE NOT NULL, " +
                "type VARCHAR(20) NOT NULL, " +
                "value DOUBLE NOT NULL, " +
                "expiration_date DATE, " +
                "max_usage INT DEFAULT 0, " +
                "current_usage INT DEFAULT 0, " +
                "active BOOLEAN DEFAULT TRUE)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table promo_codes: " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau code promo
     */
    public PromoCode create(PromoCode promoCode) {
        String sql = "INSERT INTO promo_codes (code, type, value, expiration_date, max_usage, current_usage, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, promoCode.getCode());
            pstmt.setString(2, promoCode.getType().name());
            pstmt.setDouble(3, promoCode.getValue());
            pstmt.setDate(4, promoCode.getExpirationDate() != null ? Date.valueOf(promoCode.getExpirationDate()) : null);
            pstmt.setInt(5, promoCode.getMaxUsage());
            pstmt.setInt(6, promoCode.getCurrentUsage());
            pstmt.setBoolean(7, promoCode.isActive());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    promoCode.setId(rs.getInt(1));
                }
            }
            
            return promoCode;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du code promo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère un code promo par son code
     */
    public Optional<PromoCode> getByCode(String code) {
        String sql = "SELECT * FROM promo_codes WHERE code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, code);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPromoCode(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du code promo: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Récupère tous les codes promo
     */
    public List<PromoCode> getAll() {
        List<PromoCode> promoCodes = new ArrayList<>();
        String sql = "SELECT * FROM promo_codes";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                promoCodes.add(mapResultSetToPromoCode(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des codes promo: " + e.getMessage());
        }
        
        return promoCodes;
    }

    /**
     * Met à jour un code promo
     */
    public boolean update(PromoCode promoCode) {
        String sql = "UPDATE promo_codes SET code = ?, type = ?, value = ?, expiration_date = ?, " +
                "max_usage = ?, current_usage = ?, active = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, promoCode.getCode());
            pstmt.setString(2, promoCode.getType().name());
            pstmt.setDouble(3, promoCode.getValue());
            pstmt.setDate(4, promoCode.getExpirationDate() != null ? Date.valueOf(promoCode.getExpirationDate()) : null);
            pstmt.setInt(5, promoCode.getMaxUsage());
            pstmt.setInt(6, promoCode.getCurrentUsage());
            pstmt.setBoolean(7, promoCode.isActive());
            pstmt.setInt(8, promoCode.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du code promo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un code promo
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM promo_codes WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du code promo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valide un code promo et retourne le code promo si valide
     */
    public Optional<PromoCode> validateCode(String code) {
        Optional<PromoCode> promoCodeOpt = getByCode(code);
        
        if (promoCodeOpt.isEmpty()) {
            return Optional.empty();
        }
        
        PromoCode promoCode = promoCodeOpt.get();
        
        if (!promoCode.isValid()) {
            return Optional.empty();
        }
        
        return promoCodeOpt;
    }

    /**
     * Applique un code promo et incrémente son utilisation
     */
    public boolean applyCode(String code) {
        Optional<PromoCode> promoCodeOpt = validateCode(code);
        
        if (promoCodeOpt.isEmpty()) {
            return false;
        }
        
        PromoCode promoCode = promoCodeOpt.get();
        promoCode.incrementUsage();
        
        return update(promoCode);
    }

    /**
     * Désactive un code promo
     */
    public boolean deactivate(int id) {
        String sql = "UPDATE promo_codes SET active = FALSE WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation du code promo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Active un code promo
     */
    public boolean activate(int id) {
        String sql = "UPDATE promo_codes SET active = TRUE WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'activation du code promo: " + e.getMessage());
            return false;
        }
    }

    private PromoCode mapResultSetToPromoCode(ResultSet rs) throws SQLException {
        PromoCode promoCode = new PromoCode();
        promoCode.setId(rs.getInt("id"));
        promoCode.setCode(rs.getString("code"));
        promoCode.setType(PromoCode.PromoType.valueOf(rs.getString("type")));
        promoCode.setValue(rs.getDouble("value"));
        
        Date expirationDate = rs.getDate("expiration_date");
        if (expirationDate != null) {
            promoCode.setExpirationDate(expirationDate.toLocalDate());
        }
        
        promoCode.setMaxUsage(rs.getInt("max_usage"));
        promoCode.setCurrentUsage(rs.getInt("current_usage"));
        promoCode.setActive(rs.getBoolean("active"));
        
        return promoCode;
    }
}
