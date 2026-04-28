package services;

import entities.Team;
import interfaces.ITeamService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamService implements ITeamService {

    private final Connection connection;

    public TeamService() {
        connection = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Team team) throws SQLException {
        String sql = "INSERT INTO team (name, game, max_players, current_players, power_score, logo, banner) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, team.getName());
        ps.setString(2, team.getGame());
        ps.setInt(3, team.getMaxPlayers());
        ps.setInt(4, team.getCurrentPlayers());
        ps.setInt(5, team.getPowerScore());
        ps.setString(6, team.getLogo());
        ps.setString(7, team.getBanner());

        ps.executeUpdate();
        System.out.println("Team ajoutée avec succès.");
    }

    @Override
    public void modifier(Team team) throws SQLException {
        String sql = "UPDATE team SET name = ?, game = ?, max_players = ?, current_players = ?, power_score = ?, logo = ?, banner = ? WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, team.getName());
        ps.setString(2, team.getGame());
        ps.setInt(3, team.getMaxPlayers());
        ps.setInt(4, team.getCurrentPlayers());
        ps.setInt(5, team.getPowerScore());
        ps.setString(6, team.getLogo());
        ps.setString(7, team.getBanner());
        ps.setInt(8, team.getId());

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Team modifiée avec succès.");
        } else {
            System.out.println("Aucune team trouvée avec cet id.");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM team WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Team supprimée avec succès.");
        } else {
            System.out.println("Aucune team trouvée avec cet id.");
        }
    }

    @Override
    public List<Team> recuperer() throws SQLException {
        String sql = "SELECT * FROM team";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<Team> teams = new ArrayList<>();

        while (rs.next()) {
            Team t = new Team();
            t.setId(rs.getInt("id"));
            t.setName(rs.getString("name"));
            t.setGame(rs.getString("game"));
            t.setMaxPlayers(rs.getInt("max_players"));
            t.setCurrentPlayers(rs.getInt("current_players"));
            t.setPowerScore(rs.getInt("power_score"));
            t.setLogo(rs.getString("logo"));
            t.setBanner(rs.getString("banner"));
            t.setCreatedAt(rs.getTimestamp("created_at"));

            teams.add(t);
        }

        return teams;
    }

    @Override
    public Optional<Team> getTeamById(int id) throws SQLException {
        return recuperer().stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    @Override
    public List<Team> searchByName(String search) throws SQLException {
        return recuperer().stream()
                .filter(t -> t.getName() != null && t.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Team> sortByPowerScoreDesc() throws SQLException {
        return recuperer().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPowerScore(), t1.getPowerScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Team> getFullTeams() throws SQLException {
        return recuperer().stream()
                .filter(Team::isFull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Team> getRecruitingTeams() throws SQLException {
        return recuperer().stream()
                .filter(Team::canRecruit)
                .collect(Collectors.toList());
    }
}