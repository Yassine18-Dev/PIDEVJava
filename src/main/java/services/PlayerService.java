package services;

import entities.Player;
import interfaces.IPlayerService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerService implements IPlayerService {

    private final Connection connection;

    public PlayerService() {
        connection = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Player player) throws SQLException {
        String sql = "INSERT INTO player (username, email, password, game, rank, league_points, team_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, player.getUsername());
        ps.setString(2, player.getEmail());
        ps.setString(3, player.getPassword());
        ps.setString(4, player.getGame());
        ps.setString(5, player.getRank());
        ps.setInt(6, player.getLeaguePoints());

        if (player.getTeamId() <= 0) {
            ps.setNull(7, Types.INTEGER);
        } else {
            ps.setInt(7, player.getTeamId());
        }

        ps.executeUpdate();
        System.out.println("Player ajouté avec succès.");
    }

    @Override
    public void modifier(Player player) throws SQLException {
        String sql = "UPDATE player SET username = ?, email = ?, password = ?, game = ?, rank = ?, league_points = ?, team_id = ? WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, player.getUsername());
        ps.setString(2, player.getEmail());
        ps.setString(3, player.getPassword());
        ps.setString(4, player.getGame());
        ps.setString(5, player.getRank());
        ps.setInt(6, player.getLeaguePoints());

        if (player.getTeamId() <= 0) {
            ps.setNull(7, Types.INTEGER);
        } else {
            ps.setInt(7, player.getTeamId());
        }

        ps.setInt(8, player.getId());

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Player modifié avec succès.");
        } else {
            System.out.println("Aucun player trouvé avec cet id.");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM player WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Player supprimé avec succès.");
        } else {
            System.out.println("Aucun player trouvé avec cet id.");
        }
    }

    @Override
    public List<Player> recuperer() throws SQLException {
        String sql = "SELECT * FROM player";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<Player> players = new ArrayList<>();

        while (rs.next()) {
            Player p = new Player();
            p.setId(rs.getInt("id"));
            p.setUsername(rs.getString("username"));
            p.setEmail(rs.getString("email"));
            p.setPassword(rs.getString("password"));
            p.setGame(rs.getString("game"));
            p.setRank(rs.getString("rank"));
            p.setLeaguePoints(rs.getInt("league_points"));

            int teamId = rs.getInt("team_id");
            if (rs.wasNull()) {
                p.setTeamId(-1);
            } else {
                p.setTeamId(teamId);
            }

            p.setRegisteredAt(rs.getTimestamp("registered_at"));
            players.add(p);
        }

        return players;
    }

    @Override
    public Optional<Player> getPlayerById(int id) throws SQLException {
        return recuperer().stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    @Override
    public List<Player> searchByUsername(String search) throws SQLException {
        return recuperer().stream()
                .filter(p -> p.getUsername() != null && p.getUsername().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Player> sortByRankDesc() throws SQLException {
        return recuperer().stream()
                .sorted((p1, p2) -> compareRank(p2.getRank(), p1.getRank()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Player> filterByGame(String game) throws SQLException {
        return recuperer().stream()
                .filter(p -> p.getGame() != null && p.getGame().equalsIgnoreCase(game))
                .collect(Collectors.toList());
    }

    @Override
    public List<Player> getFreeAgents() throws SQLException {
        return recuperer().stream()
                .filter(p -> p.getTeamId() == -1)
                .collect(Collectors.toList());
    }

    @Override
    public List<Player> getTop3Players() throws SQLException {
        return recuperer().stream()
                .sorted((p1, p2) -> compareRank(p2.getRank(), p1.getRank()))
                .limit(3)
                .collect(Collectors.toList());
    }

    private int compareRank(String rank1, String rank2) {
        List<String> rankOrder = Arrays.asList(
                "Unranked",
                "Iron",
                "Bronze",
                "Silver",
                "Gold",
                "Platinum",
                "Diamond",
                "Master",
                "Grandmaster",
                "Challenger",
                "Immortal"
        );

        String normalizedRank1 = normalizeRank(rank1);
        String normalizedRank2 = normalizeRank(rank2);

        int idx1 = rankOrder.indexOf(normalizedRank1);
        int idx2 = rankOrder.indexOf(normalizedRank2);

        if (idx1 == -1) idx1 = 0;
        if (idx2 == -1) idx2 = 0;

        return Integer.compare(idx1, idx2);
    }

    private String normalizeRank(String rank) {
        if (rank == null || rank.trim().isEmpty()) {
            return "Unranked";
        }

        String value = rank.trim();

        if (value.startsWith("Iron")) return "Iron";
        if (value.startsWith("Bronze")) return "Bronze";
        if (value.startsWith("Silver")) return "Silver";
        if (value.startsWith("Gold")) return "Gold";
        if (value.startsWith("Platinum")) return "Platinum";
        if (value.startsWith("Diamond")) return "Diamond";
        if (value.startsWith("Master")) return "Master";
        if (value.startsWith("Grandmaster")) return "Grandmaster";
        if (value.startsWith("Challenger")) return "Challenger";
        if (value.startsWith("Immortal")) return "Immortal";

        return "Unranked";
    }
}