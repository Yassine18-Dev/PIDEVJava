package interfaces;

import entities.Tournoi;

import java.sql.SQLException;
import java.util.List;

public interface ITournoiService {
    void ajouterTournoi(Tournoi tournoi) throws SQLException;

    void modifierTournoi(Tournoi tournoi) throws SQLException;

    void supprimerTournoi(int id) throws SQLException;

    List<Tournoi> afficherTournois() throws SQLException;

    Tournoi getTournoiById(int id) throws SQLException;
}