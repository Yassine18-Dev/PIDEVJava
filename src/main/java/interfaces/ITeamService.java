package interfaces;

import entities.Team;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ITeamService {

    void ajouter(Team team) throws SQLException;

    void modifier(Team team) throws SQLException;

    void supprimer(int id) throws SQLException;

    List<Team> recuperer() throws SQLException;

    Optional<Team> getTeamById(int id) throws SQLException;

    List<Team> searchByName(String search) throws SQLException;

    List<Team> sortByPowerScoreDesc() throws SQLException;

    List<Team> getFullTeams() throws SQLException;

    List<Team> getRecruitingTeams() throws SQLException;
}