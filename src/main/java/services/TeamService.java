package services;

import entities.Team;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();

    public Team getById(int id) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM team WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFull(rs);
        }
        return null;
    }

    public List<Team> findAll() throws SQLException {
        List<Team> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM team ORDER BY name")) {
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    public void incrementCurrentPlayers(int teamId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE team SET current_players = current_players + 1 WHERE id=?")) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        }
    }

    public void decrementCurrentPlayers(int teamId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE team SET current_players = GREATEST(0, current_players - 1) WHERE id=?")) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        }
    }

    public List<Team> findRecruitingTeams(String game, int excludePlayerId) throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.* FROM team t " +
                "WHERE t.current_players < t.max_players " +
                "  AND t.game = ? " +
                "  AND (t.captain_id IS NULL OR t.captain_id <> ?) " +
                "ORDER BY t.power_score DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            ps.setInt(2,    excludePlayerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    public void updateTeamInfo(Team t) throws SQLException {
        String sql = "UPDATE team SET name=?, max_players=?, logo=?, banner=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setInt(2,    t.getMaxPlayers());
            ps.setString(3, t.getLogo());
            ps.setString(4, t.getBanner());
            ps.setInt(5,    t.getId());
            ps.executeUpdate();
        }
    }

    public boolean isTeamNameTaken(String name, int excludeId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM team WHERE name = ? AND id <> ?")) {
            ps.setString(1, name);
            ps.setInt(2,    excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** Met à jour le webhook Discord d'une équipe. */
    public void updateWebhook(int teamId, String webhookUrl) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE team SET discord_webhook_url=? WHERE id=?")) {
            ps.setString(1, webhookUrl);
            ps.setInt(2, teamId);
            ps.executeUpdate();
        }
    }

    private Team mapFull(ResultSet rs) throws SQLException {
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
        int cap = rs.getInt("captain_id");
        t.setCaptainId(rs.wasNull() ? 0 : cap);
        t.setDiscordWebhookUrl(rs.getString("discord_webhook_url"));
        return t;
    }
}