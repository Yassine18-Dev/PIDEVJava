package entities;

import java.time.LocalDateTime;

/**
 * Entité représentant une transaction de fidélité
 */
public class LoyaltyTransaction {
    private int id;
    private int loyaltyAccountId;
    private TransactionType type;
    private int points;
    private double amount;
    private LocalDateTime transactionDate;
    private String description;

    public enum TransactionType {
        EARNED,
        REDEEMED
    }

    public LoyaltyTransaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public LoyaltyTransaction(int loyaltyAccountId, TransactionType type, int points, double amount, String description) {
        this.loyaltyAccountId = loyaltyAccountId;
        this.type = type;
        this.points = points;
        this.amount = amount;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLoyaltyAccountId() {
        return loyaltyAccountId;
    }

    public void setLoyaltyAccountId(int loyaltyAccountId) {
        this.loyaltyAccountId = loyaltyAccountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
