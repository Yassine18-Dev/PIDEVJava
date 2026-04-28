package interfaces;

import entities.Jeu;

import java.sql.SQLException;
import java.util.List;

public interface IJeuService {
    void ajouter(Jeu j) throws SQLException;
    void modifier(Jeu j) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Jeu> afficher() throws SQLException;

    List<Jeu> rechercherParNom(String nom) throws SQLException;
    List<Jeu> trierParNom() throws SQLException;
    List<Jeu> trierParDateSortie() throws SQLException;
}