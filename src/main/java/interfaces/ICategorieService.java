package interfaces;

import entities.Categorie;

import java.sql.SQLException;
import java.util.List;

public interface ICategorieService {
    void ajouter(Categorie c) throws SQLException;
    void modifier(Categorie c) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Categorie> afficher() throws SQLException;

    List<Categorie> rechercherParNom(String nom) throws SQLException;
    List<Categorie> trierParNom() throws SQLException;
}