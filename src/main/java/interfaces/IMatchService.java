package interfaces;

import entities.Match;
import java.sql.SQLException;
import java.util.List;

public interface IMatchService {
    void ajouterMatch(Match match) throws SQLException;
    void modifierMatch(Match match) throws SQLException;
    void supprimerMatch(int id) throws SQLException;
    List<Match> afficherMatchs() throws SQLException;
    Match getMatchById(int id) throws SQLException;
    List<Match> trierParDate() throws SQLException;
}