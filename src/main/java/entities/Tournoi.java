package entities;

import java.math.BigDecimal;

public class Tournoi {
    private int id;
    private String nom;
    private String lieu;
    private String dateDebut;
    private String dateFin;
    private BigDecimal prixInscription = BigDecimal.ZERO;

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
        this.prixInscription = prixInscription != null ? prixInscription : BigDecimal.ZERO;
    }

    public Tournoi(String nom, String lieu, String dateDebut, String dateFin, BigDecimal prixInscription) {
        this.nom = nom;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixInscription = prixInscription != null ? prixInscription : BigDecimal.ZERO;
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

    public BigDecimal getPrixInscription() {
        return prixInscription;
    }

    public void setPrixInscription(BigDecimal prixInscription) {
        this.prixInscription = prixInscription != null ? prixInscription : BigDecimal.ZERO;
    }

    public BigDecimal getPrix() {
        return prixInscription;
    }

    public void setPrix(BigDecimal prix) {
        this.prixInscription = prix != null ? prix : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return nom + "\nLieu : " + lieu + "\nDu " + dateDebut + " au " + dateFin + "\nPrix : " + prixInscription + " €";
    }
}