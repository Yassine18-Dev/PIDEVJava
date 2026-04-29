package entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Tournoi {
    private int id;
    private String nom;
    private String lieu;
    private String dateDebut;
    private String dateFin;
    private BigDecimal prixInscription = BigDecimal.ZERO;

    private int maxParticipants = 10;
    private int currentParticipants = 0;
    private BigDecimal discountPrice;

    public Tournoi() {}

    public Tournoi(int id, String nom, String lieu, String dateDebut, String dateFin) {
        this.id = id;
        this.nom = nom;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Tournoi(String nom, String lieu, String dateDebut, String dateFin) {
        this.nom = nom;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Tournoi(int id, String nom, String lieu, String dateDebut, String dateFin, BigDecimal prixInscription) {
        this.id = id;
        this.nom = nom;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        setPrixInscription(prixInscription);
    }

    public Tournoi(String nom, String lieu, String dateDebut, String dateFin, BigDecimal prixInscription) {
        this.nom = nom;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        setPrixInscription(prixInscription);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }

    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }

    public BigDecimal getPrixInscription() { return prixInscription; }

    public void setPrixInscription(BigDecimal prixInscription) {
        this.prixInscription = prixInscription != null ? prixInscription : BigDecimal.ZERO;
    }

    public BigDecimal getPrix() { return prixInscription; }

    public void setPrix(BigDecimal prix) {
        this.prixInscription = prix != null ? prix : BigDecimal.ZERO;
    }

    public int getMaxParticipants() { return maxParticipants; }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getCurrentParticipants() { return currentParticipants; }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public BigDecimal getDiscountPrice() { return discountPrice; }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }

    public boolean isDiscountAvailable() {
        try {
            if (dateDebut == null || dateDebut.isBlank()) return false;
            if (maxParticipants <= 0) return false;
            if (currentParticipants >= maxParticipants) return false;

            LocalDate debut = LocalDate.parse(dateDebut);
            LocalDate demain = LocalDate.now().plusDays(1);

            return debut.equals(demain);

        } catch (Exception e) {
            return false;
        }
    }

    public BigDecimal getPrixFinal() {
        if (isDiscountAvailable() && discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountPrice.setScale(2, RoundingMode.HALF_UP);
        }

        return prixInscription.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return nom
                + "\nLieu : " + lieu
                + "\nDu " + dateDebut + " au " + dateFin
                + "\nPrix : " + getPrixFinal() + " €"
                + "\nParticipants : " + currentParticipants + "/" + maxParticipants;
    }
    public String getStatut() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate debut = java.time.LocalDate.parse(dateDebut);
            java.time.LocalDate fin = java.time.LocalDate.parse(dateFin);

            if (today.isBefore(debut)) return "A_VENIR";
            if (today.isAfter(fin)) return "TERMINE";
            return "EN_COURS";

        } catch (Exception e) {
            return "EN_COURS";
        }
    }
}