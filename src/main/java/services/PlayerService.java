package services;

import entities.Player;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();

    public Player getById(int id) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM player WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFull(rs);
        }
        return null;
    }

    public Player findFirst() throws SQLException {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM player ORDER BY id ASC LIMIT 1")) {
            if (rs.next()) return mapFull(rs);
        }
        return null;
    }

    public List<Player> findAll() throws SQLException {
        List<Player> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM player ORDER BY username")) {
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    public List<Player> findByTeam(int teamId) throws SQLException {
        List<Player> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM player WHERE team_id=?")) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    public List<Player> findFreePlayers(String game) throws SQLException {
        List<Player> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM player WHERE team_id IS NULL AND game=?")) {
            ps.setString(1, game);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    /** Update profil COMPLET (gardé pour compat — mais préfère updateProfileSafe). */
    public void updateProfile(Player p) throws SQLException {
        String sql = "UPDATE player SET username=?, email=?, avatar=?, " +
                "vision=?, shooting=?, reflex=?, teamplay=?, communication=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getUsername());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getAvatar());
            ps.setInt(4,    p.getVision());
            ps.setInt(5,    p.getShooting());
            ps.setInt(6,    p.getReflex());
            ps.setInt(7,    p.getTeamplay());
            ps.setInt(8,    p.getCommunication());
            ps.setInt(9,    p.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Update SAFE : modifie tout SAUF l'email (qui passe par EmailConfirmationService).
     */
    public void updateProfileSafe(Player p) throws SQLException {
        String sql = "UPDATE player SET username=?, avatar=?, " +
                "vision=?, shooting=?, reflex=?, teamplay=?, communication=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getUsername());
            ps.setString(2, p.getAvatar());
            ps.setInt(3,    p.getVision());
            ps.setInt(4,    p.getShooting());
            ps.setInt(5,    p.getReflex());
            ps.setInt(6,    p.getTeamplay());
            ps.setInt(7,    p.getCommunication());
            ps.setInt(8,    p.getId());
            ps.executeUpdate();
        }
    }

    /** Vérifie si un username est déjà pris par quelqu'un d'autre. */
    public boolean isUsernameTaken(String username, int excludeId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM player WHERE username = ? AND id <> ?")) {
            ps.setString(1, username);
            ps.setInt(2,    excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public void setTeam(int playerId, Integer teamId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE player SET team_id=? WHERE id=?")) {
            if (teamId == null || teamId <= 0) ps.setNull(1, Types.INTEGER);
            else                                ps.setInt(1, teamId);
            ps.setInt(2, playerId);
            ps.executeUpdate();
        }
    }

    private Player mapFull(ResultSet rs) throws SQLException {
        Player p = new Player();
        p.setId(rs.getInt("id"));
        p.setUsername(rs.getString("username"));
        p.setEmail(rs.getString("email"));
        p.setPassword(rs.getString("password"));
        p.setGame(rs.getString("game"));
        p.setRank(rs.getString("rank"));
        p.setLeaguePoints(rs.getInt("league_points"));
        int teamId = rs.getInt("team_id");
        p.setTeamId(rs.wasNull() ? 0 : teamId);
        p.setRegisteredAt(rs.getTimestamp("registered_at"));
        p.setAvatar(rs.getString("avatar"));
        p.setVision(rs.getInt("vision"));
        p.setShooting(rs.getInt("shooting"));
        p.setReflex(rs.getInt("reflex"));
        p.setTeamplay(rs.getInt("teamplay"));
        p.setCommunication(rs.getInt("communication"));
        p.setWinrate(rs.getDouble("winrate"));
        p.setKda(rs.getDouble("kda"));
        p.setMvpCount(rs.getInt("mvp_count"));

        // Confirmation email
        p.setPendingEmail(rs.getString("pending_email"));
        p.setEmailConfirmationToken(rs.getString("email_confirmation_token"));
        p.setEmailTokenExpires(rs.getTimestamp("email_token_expires"));
        p.setEmailVerified(rs.getBoolean("email_verified"));

        return p;
    }
}