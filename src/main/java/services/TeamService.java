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
        return t;
    }
}