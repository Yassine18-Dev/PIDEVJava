package entities;

import java.time.LocalDate;

/**
 * Entité représentant un code promo
 */
public class PromoCode {
    private int id;
    private String code;
    private PromoType type;
    private double value;
    private LocalDate expirationDate;
    private int maxUsage;
    private int currentUsage;
    private boolean active;

    public enum PromoType {
        PERCENTAGE,
        FIXED
    }

    public PromoCode() {
    }

    public PromoCode(String code, PromoType type, double value, LocalDate expirationDate, int maxUsage) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.expirationDate = expirationDate;
        this.maxUsage = maxUsage;
        this.currentUsage = 0;
        this.active = true;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public PromoType getType() {
        return type;
    }

    public void setType(PromoType type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(int maxUsage) {
        this.maxUsage = maxUsage;
    }

    public int getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(int currentUsage) {
        this.currentUsage = currentUsage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Vérifie si le code est valide pour utilisation
     */
    public boolean isValid() {
        if (!active) {
            return false;
        }
        if (expirationDate != null && LocalDate.now().isAfter(expirationDate)) {
            return false;
        }
        if (maxUsage > 0 && currentUsage >= maxUsage) {
            return false;
        }
        return true;
    }

    /**
     * Incrémente le compteur d'utilisation
     */
    public void incrementUsage() {
        this.currentUsage++;
    }

    /**
     * Calcule la réduction pour un montant donné
     */
    public double calculateDiscount(double amount) {
        if (type == PromoType.PERCENTAGE) {
            return amount * (value / 100);
        } else {
            return Math.min(value, amount);
        }
    }
}
