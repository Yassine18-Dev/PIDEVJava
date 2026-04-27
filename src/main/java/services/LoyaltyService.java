package services;

import entities.LoyaltyAccount;
import entities.LoyaltyTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion du programme de fidélité
 */
public class LoyaltyService {
    private static LoyaltyService instance;
    private Connection connection;

    // Taux de conversion : 1 TND = 1 point, 100 points = 1 TND
    private static final int POINTS_PER_TND = 1;
    private static final int TND_PER_100_POINTS = 1;

    private LoyaltyService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidev", "root", "");
            createTablesIfNotExists();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    public static LoyaltyService getInstance() {
        if (instance == null) {
            instance = new LoyaltyService();
        }
        return instance;
    }

    private void createTablesIfNotExists() {
        // Table loyalty_accounts
        String accountsSql = "CREATE TABLE IF NOT EXISTS loyalty_accounts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT UNIQUE NOT NULL, " +
                "current_points INT DEFAULT 0, " +
                "total_points_earned INT DEFAULT 0, " +
                "tier VARCHAR(20) DEFAULT 'BRONZE')";
        
        // Table loyalty_transactions
        String transactionsSql = "CREATE TABLE IF NOT EXISTS loyalty_transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "loyalty_account_id INT NOT NULL, " +
                "type VARCHAR(20) NOT NULL, " +
                "points INT NOT NULL, " +
                "amount DOUBLE, " +
                "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "description TEXT, " +
                "FOREIGN KEY (loyalty_account_id) REFERENCES loyalty_accounts(id))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(accountsSql);
            stmt.execute(transactionsSql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables fidélité: " + e.getMessage());
        }
    }

    /**
     * Récupère ou crée un compte fidélité pour un utilisateur
     */
    public LoyaltyAccount getOrCreateAccount(int userId) {
        Optional<LoyaltyAccount> accountOpt = getAccountByUserId(userId);
        
        if (accountOpt.isPresent()) {
            return accountOpt.get();
        }
        
        // Créer un nouveau compte
        LoyaltyAccount newAccount = new LoyaltyAccount(userId);
        return createAccount(newAccount);
    }

    /**
     * Récupère un compte fidélité par ID utilisateur
     */
    public Optional<LoyaltyAccount> getAccountByUserId(int userId) {
        String sql = "SELECT * FROM loyalty_accounts WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du compte fidélité: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Crée un nouveau compte fidélité
     */
    public LoyaltyAccount createAccount(LoyaltyAccount account) {
        String sql = "INSERT INTO loyalty_accounts (user_id, current_points, total_points_earned, tier) " +
                "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getUserId());
            pstmt.setInt(2, account.getCurrentPoints());
            pstmt.setInt(3, account.getTotalPointsEarned());
            pstmt.setString(4, account.getTier().name());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getInt(1));
                }
            }
            
            return account;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du compte fidélité: " + e.getMessage());
            return null;
        }
    }

    /**
     * Met à jour un compte fidélité
     */
    public boolean updateAccount(LoyaltyAccount account) {
        String sql = "UPDATE loyalty_accounts SET current_points = ?, total_points_earned = ?, tier = ? " +
                "WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, account.getCurrentPoints());
            pstmt.setInt(2, account.getTotalPointsEarned());
            pstmt.setString(3, account.getTier().name());
            pstmt.setInt(4, account.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du compte fidélité: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ajoute des points après un achat
     * 1 TND = 1 point
     */
    public LoyaltyAccount.LoyaltyTier addPoints(int userId, double amount) {
        int pointsEarned = (int) (amount * POINTS_PER_TND);
        
        LoyaltyAccount account = getOrCreateAccount(userId);
        LoyaltyAccount.LoyaltyTier oldTier = account.getTier();
        
        account.addPoints(pointsEarned);
        updateAccount(account);
        
        // Enregistrer la transaction
        LoyaltyTransaction transaction = new LoyaltyTransaction(
                account.getId(),
                LoyaltyTransaction.TransactionType.EARNED,
                pointsEarned,
                amount,
                "Points gagnés suite à un achat"
        );
        createTransaction(transaction);
        
        // Vérifier si le palier a changé
        LoyaltyAccount.LoyaltyTier newTier = account.updateTier();
        if (newTier != null) {
            updateAccount(account);
        }
        
        return newTier != null ? newTier : oldTier;
    }

    /**
     * Utilise des points pour une réduction
     * 100 points = 1 TND
     */
    public boolean redeemPoints(int userId, int points) {
        if (points < 100 || points % 100 != 0) {
            return false; // Les points doivent être utilisés par multiples de 100
        }
        
        LoyaltyAccount account = getOrCreateAccount(userId);
        
        if (!account.redeemPoints(points)) {
            return false;
        }
        
        updateAccount(account);
        
        // Enregistrer la transaction
        double discount = (points / 100.0) * TND_PER_100_POINTS;
        LoyaltyTransaction transaction = new LoyaltyTransaction(
                account.getId(),
                LoyaltyTransaction.TransactionType.REDEEMED,
                points,
                discount,
                "Points utilisés pour une réduction"
        );
        createTransaction(transaction);
        
        return true;
    }

    /**
     * Récupère l'historique des transactions d'un compte
     */
    public List<LoyaltyTransaction> getTransactionHistory(int accountId) {
        List<LoyaltyTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM loyalty_transactions WHERE loyalty_account_id = ? " +
                "ORDER BY transaction_date DESC LIMIT 20";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique: " + e.getMessage());
        }
        
        return transactions;
    }

    /**
     * Calcule la réduction en TND pour un nombre de points
     */
    public double calculateDiscountFromPoints(int points) {
        if (points < 100 || points % 100 != 0) {
            return 0;
        }
        return (points / 100.0) * TND_PER_100_POINTS;
    }

    /**
     * Vérifie si l'utilisateur a suffisamment de points
     */
    public boolean hasEnoughPoints(int userId, int points) {
        Optional<LoyaltyAccount> accountOpt = getAccountByUserId(userId);
        return accountOpt.isPresent() && accountOpt.get().getCurrentPoints() >= points;
    }

    /**
     * Crée une transaction de fidélité
     */
    private LoyaltyTransaction createTransaction(LoyaltyTransaction transaction) {
        String sql = "INSERT INTO loyalty_transactions (loyalty_account_id, type, points, amount, description) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, transaction.getLoyaltyAccountId());
            pstmt.setString(2, transaction.getType().name());
            pstmt.setInt(3, transaction.getPoints());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getDescription());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    transaction.setId(rs.getInt(1));
                }
            }
            
            return transaction;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la transaction: " + e.getMessage());
            return null;
        }
    }

    private LoyaltyAccount mapResultSetToAccount(ResultSet rs) throws SQLException {
        LoyaltyAccount account = new LoyaltyAccount();
        account.setId(rs.getInt("id"));
        account.setUserId(rs.getInt("user_id"));
        account.setCurrentPoints(rs.getInt("current_points"));
        account.setTotalPointsEarned(rs.getInt("total_points_earned"));
        account.setTier(LoyaltyAccount.LoyaltyTier.valueOf(rs.getString("tier")));
        return account;
    }

    private LoyaltyTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setId(rs.getInt("id"));
        transaction.setLoyaltyAccountId(rs.getInt("loyalty_account_id"));
        transaction.setType(LoyaltyTransaction.TransactionType.valueOf(rs.getString("type")));
        transaction.setPoints(rs.getInt("points"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setDescription(rs.getString("description"));
        return transaction;
    }
}
