package interfaces;

import entities.Tournoi;

import java.sql.SQLException;
import java.util.List;

public interface ITournoiService {
    void ajouter(Tournoi t) throws SQLException;
    void modifier(Tournoi t) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Tournoi> afficher() throws SQLException;

    List<Tournoi> rechercherParNom(String nom) throws SQLException;
    List<Tournoi> trierParNom() throws SQLException;
}
