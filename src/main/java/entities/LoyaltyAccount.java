package entities;

/**
 * Entité représentant le compte fidélité d'un utilisateur
 */
public class LoyaltyAccount {
    private int id;
    private int userId;
    private int currentPoints;
    private int totalPointsEarned;
    private LoyaltyTier tier;

    public enum LoyaltyTier {
        BRONZE(0, 0),
        SILVER(500, 5),
        GOLD(1500, 10);

        private final int pointsThreshold;
        private final int discountPercentage;

        LoyaltyTier(int pointsThreshold, int discountPercentage) {
            this.pointsThreshold = pointsThreshold;
            this.discountPercentage = discountPercentage;
        }

        public int getPointsThreshold() {
            return pointsThreshold;
        }

        public int getDiscountPercentage() {
            return discountPercentage;
        }

        /**
         * Détermine le palier en fonction des points
         */
        public static LoyaltyTier fromPoints(int points) {
            if (points >= GOLD.pointsThreshold) {
                return GOLD;
            } else if (points >= SILVER.pointsThreshold) {
                return SILVER;
            } else {
                return BRONZE;
            }
        }

        /**
         * Calcule la progression vers le palier suivant
         */
        public double getProgressToNextTier(int currentPoints) {
            if (this == GOLD) {
                return 100.0; // Palier maximum
            }
            
            LoyaltyTier nextTier = getNextTier();
            if (nextTier == null) {
                return 100.0;
            }
            
            int range = nextTier.pointsThreshold - this.pointsThreshold;
            int progress = currentPoints - this.pointsThreshold;
            
            return Math.min(100.0, (progress * 100.0) / range);
        }

        /**
         * Retourne le palier suivant
         */
        public LoyaltyTier getNextTier() {
            if (this == BRONZE) {
                return SILVER;
            } else if (this == SILVER) {
                return GOLD;
            }
            return null;
        }
    }

    public LoyaltyAccount() {
        this.currentPoints = 0;
        this.totalPointsEarned = 0;
        this.tier = LoyaltyTier.BRONZE;
    }

    public LoyaltyAccount(int userId) {
        this.userId = userId;
        this.currentPoints = 0;
        this.totalPointsEarned = 0;
        this.tier = LoyaltyTier.BRONZE;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }

    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void setTotalPointsEarned(int totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public LoyaltyTier getTier() {
        return tier;
    }

    public void setTier(LoyaltyTier tier) {
        this.tier = tier;
    }

    /**
     * Ajoute des points au compte
     */
    public void addPoints(int points) {
        this.currentPoints += points;
        this.totalPointsEarned += points;
        updateTier();
    }

    /**
     * Utilise des points du compte
     */
    public boolean redeemPoints(int points) {
        if (currentPoints >= points) {
            this.currentPoints -= points;
            updateTier();
            return true;
        }
        return false;
    }

    /**
     * Met à jour le palier en fonction des points actuels
     */
    public LoyaltyTier updateTier() {
        LoyaltyTier newTier = LoyaltyTier.fromPoints(currentPoints);
        LoyaltyTier oldTier = this.tier;
        this.tier = newTier;
        return oldTier != newTier ? newTier : null;
    }

    /**
     * Calcule la réduction du palier actuel
     */
    public double getDiscountPercentage() {
        return tier.getDiscountPercentage();
    }

    /**
     * Calcule la progression vers le palier suivant
     */
    public double getProgressToNextTier() {
        return tier.getProgressToNextTier(currentPoints);
    }

    /**
     * Retourne le nombre de points nécessaires pour le palier suivant
     */
    public int getPointsToNextTier() {
        LoyaltyTier nextTier = tier.getNextTier();
        if (nextTier == null) {
            return 0;
        }
        return nextTier.pointsThreshold - currentPoints;
    }
}
