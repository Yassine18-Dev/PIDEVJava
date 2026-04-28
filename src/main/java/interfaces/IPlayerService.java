package interfaces;

import entities.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IPlayerService {

    void ajouter(Player player) throws SQLException;

    void modifier(Player player) throws SQLException;

    void supprimer(int id) throws SQLException;

    List<Player> recuperer() throws SQLException;

    Optional<Player> getPlayerById(int id) throws SQLException;

    List<Player> searchByUsername(String search) throws SQLException;

    List<Player> sortByRankDesc() throws SQLException;

    List<Player> filterByGame(String game) throws SQLException;

    List<Player> getFreeAgents() throws SQLException;

    List<Player> getTop3Players() throws SQLException;
}