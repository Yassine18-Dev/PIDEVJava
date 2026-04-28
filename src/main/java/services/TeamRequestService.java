package services;

import entities.Player;
import entities.Team;
import entities.TeamRequest;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamRequestService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();

    public void sendRequest(Player player, Team team, String message) throws SQLException {
        if (player.getTeamId() > 0)
            throw new IllegalStateException("Tu es déjà dans une équipe.");
        if (team.getCurrentPlayers() >= team.getMaxPlayers())
            throw new IllegalStateException("Cette équipe est complète.");
        if (!team.getGame().equalsIgnoreCase(player.getGame()))
            throw new IllegalStateException("Cette équipe joue à " + team.getGame()
                    + ", pas à " + player.getGame() + ".");
        if ("fifa".equalsIgnoreCase(player.getGame()))
            throw new IllegalStateException("Les joueurs FIFA ne peuvent pas postuler.");
        if (hasPendingRequest(team.getId(), player.getId()))
            throw new IllegalStateException("Tu as déjà une demande en attente pour cette équipe.");

        String sql = "INSERT INTO team_request(team_id, player_id, status, message) VALUES (?,?,'PENDING',?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1,    team.getId());
            ps.setInt(2,    player.getId());
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    public boolean hasPendingRequest(int teamId, int playerId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM team_request WHERE team_id=? AND player_id=? AND status='PENDING'")) {
            ps.setInt(1, teamId);
            ps.setInt(2, playerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public void acceptRequest(int requestId, int captainId) throws SQLException {
        cnx.setAutoCommit(false);
        try {
            // 1) Verrouiller la demande
            TeamRequest req;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM team_request WHERE id=? FOR UPDATE")) {
                ps.setInt(1, requestId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new IllegalStateException("Demande introuvable.");
                req = mapBasic(rs);
            }
            if (req.getStatus() != TeamRequest.Status.PENDING) {
                cnx.rollback();
                throw new IllegalStateException("Cette demande a déjà été traitée.");
            }

            // 2) Verrouiller team + vérifier capitaine
            int currentPlayers, maxPlayers, teamCaptain;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT current_players, max_players, captain_id FROM team WHERE id=? FOR UPDATE")) {
                ps.setInt(1, req.getTeamId());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new IllegalStateException("Équipe introuvable.");
                currentPlayers = rs.getInt("current_players");
                maxPlayers     = rs.getInt("max_players");
                int cap        = rs.getInt("captain_id");
                teamCaptain    = rs.wasNull() ? 0 : cap;
            }

            if (teamCaptain != captainId) {
                cnx.rollback();
                throw new IllegalStateException("Seul le capitaine de l'équipe peut accepter.");
            }
            if (currentPlayers >= maxPlayers) {
                cnx.rollback();
                throw new IllegalStateException("L'équipe est désormais complète.");
            }

            // 3) Verrouiller player
            Integer existingTeam;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT team_id FROM player WHERE id=? FOR UPDATE")) {
                ps.setInt(1, req.getPlayerId());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new IllegalStateException("Joueur introuvable.");
                int t = rs.getInt("team_id");
                existingTeam = rs.wasNull() ? null : t;
            }
            if (existingTeam != null) {
                cnx.rollback();
                throw new IllegalStateException("Ce joueur a déjà rejoint une autre équipe.");
            }

            // 4) Tout OK : appliquer les mises à jour
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE team_request SET status='ACCEPTED', replied_at=NOW() WHERE id=?")) {
                ps.setInt(1, requestId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE player SET team_id=? WHERE id=?")) {
                ps.setInt(1, req.getTeamId());
                ps.setInt(2, req.getPlayerId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE team SET current_players = current_players + 1 WHERE id=?")) {
                ps.setInt(1, req.getTeamId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE team_request SET status='REFUSED', replied_at=NOW() " +
                            "WHERE player_id=? AND status='PENDING' AND id <> ?")) {
                ps.setInt(1, req.getPlayerId());
                ps.setInt(2, requestId);
                ps.executeUpdate();
            }

            cnx.commit();
        } catch (Exception e) {
            try { cnx.rollback(); } catch (Exception ignored) {}
            if (e instanceof SQLException sqe) throw sqe;
            if (e instanceof IllegalStateException ise) throw ise;
            throw new SQLException(e);
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    public void refuseRequest(int requestId, int captainId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT t.captain_id FROM team_request r JOIN team t ON t.id = r.team_id WHERE r.id=?")) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new IllegalStateException("Demande introuvable.");
            int captain = rs.getInt("captain_id");
            if (rs.wasNull() || captain != captainId)
                throw new IllegalStateException("Seul le capitaine peut refuser.");
        }
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM team_request WHERE id=?")) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public List<TeamRequest> findRequestsForTeam(int teamId) throws SQLException {
        String sql = "SELECT r.*, t.name AS team_name, t.game AS team_game, " +
                "       p.username AS player_name, p.`rank` AS player_rank, p.league_points AS player_lp " +
                "FROM team_request r " +
                "JOIN team   t ON t.id = r.team_id " +
                "JOIN player p ON p.id = r.player_id " +
                "WHERE r.team_id=? ORDER BY r.sent_at DESC";
        return query(sql, teamId);
    }

    public List<TeamRequest> findRequestsByPlayer(int playerId) throws SQLException {
        String sql = "SELECT r.*, t.name AS team_name, t.game AS team_game, " +
                "       p.username AS player_name, p.`rank` AS player_rank, p.league_points AS player_lp " +
                "FROM team_request r " +
                "JOIN team   t ON t.id = r.team_id " +
                "JOIN player p ON p.id = r.player_id " +
                "WHERE r.player_id=? ORDER BY r.sent_at DESC";
        return query(sql, playerId);
    }

    public int countPendingForTeam(int teamId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM team_request WHERE team_id=? AND status='PENDING'")) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private List<TeamRequest> query(String sql, int param) throws SQLException {
        List<TeamRequest> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private TeamRequest mapBasic(ResultSet rs) throws SQLException {
        TeamRequest r = new TeamRequest();
        r.setId(rs.getInt("id"));
        r.setTeamId(rs.getInt("team_id"));
        r.setPlayerId(rs.getInt("player_id"));
        r.setStatus(TeamRequest.Status.valueOf(rs.getString("status")));
        r.setMessage(rs.getString("message"));
        r.setSentAt(rs.getTimestamp("sent_at"));
        r.setRepliedAt(rs.getTimestamp("replied_at"));
        return r;
    }

    private TeamRequest map(ResultSet rs) throws SQLException {
        TeamRequest r = mapBasic(rs);
        r.setTeamName(rs.getString("team_name"));
        r.setTeamGame(rs.getString("team_game"));
        r.setPlayerName(rs.getString("player_name"));
        r.setPlayerRank(rs.getString("player_rank"));
        r.setPlayerLP(rs.getInt("player_lp"));
        return r;
    }
}