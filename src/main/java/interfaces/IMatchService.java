package interfaces;

import entities.Match;

import java.sql.SQLException;
import java.util.List;

public interface IMatchService {
    void ajouter(Match m) throws SQLException;
    void modifier(Match m) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Match> afficher() throws SQLException;

    List<Match> rechercherParEquipe(String equipe) throws SQLException;
    List<Match> trierParDate() throws SQLException;
}