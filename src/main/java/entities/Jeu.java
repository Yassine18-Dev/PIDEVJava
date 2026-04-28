package entities;

import java.sql.Date;

public class Jeu {
    private int id;
    private String nom;
    private String studio;
    private Date dateSortie;
    private String modeJeu;
    private int categorieId;

    public Jeu() {
    }

    public Jeu(int id, String nom, String studio, Date dateSortie, String modeJeu, int categorieId) {
        this.id = id;
        this.nom = nom;
        this.studio = studio;
        this.dateSortie = dateSortie;
        this.modeJeu = modeJeu;
        this.categorieId = categorieId;
    }

    public Jeu(String nom, String studio, Date dateSortie, String modeJeu, int categorieId) {
        this.nom = nom;
        this.studio = studio;
        this.dateSortie = dateSortie;
        this.modeJeu = modeJeu;
        this.categorieId = categorieId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public Date getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(Date dateSortie) {
        this.dateSortie = dateSortie;
    }

    public String getModeJeu() {
        return modeJeu;
    }

    public void setModeJeu(String modeJeu) {
        this.modeJeu = modeJeu;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    @Override
    public String toString() {
        return "Jeu{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", studio='" + studio + '\'' +
                ", dateSortie=" + dateSortie +
                ", modeJeu='" + modeJeu + '\'' +
                ", categorieId=" + categorieId +
                '}';
    }
}