package services;

import entities.Invitation;
import entities.Player;
import entities.Team;
import utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvitationService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();

    private static final int    DURATION_DAYS    = 7;
    private static final int    ANTI_SPAM_LIMIT  = 5;     // 5 invitations max
    private static final int    ANTI_SPAM_HOURS  = 24;    // par 24h

    // ============================================================
    // ENVOYER UNE INVITATION
    // ============================================================
    public void sendInvitation(Team team, Player target, int senderId, String message) throws SQLException {
        // Pré-vérifications de bon sens (avant ouverture transaction)
        if (team.getCaptainId() != senderId)
            throw new IllegalStateException("Seul le capitaine peut inviter.");
        if (target.getGame() != null && target.getGame().equalsIgnoreCase("fifa"))
            throw new IllegalStateException("Les joueurs FIFA ne peuvent pas être invités.");
        if (!team.getGame().equalsIgnoreCase(target.getGame()))
            throw new IllegalStateException("Ce joueur ne joue pas à " + team.getGame() + ".");

        // Anti-spam : pas plus de 5 invitations envoyées par l'équipe en 24h
        if (countRecentInvitations(team.getId(), ANTI_SPAM_HOURS) >= ANTI_SPAM_LIMIT)
            throw new IllegalStateException("Trop d'invitations envoyées récemment (max "
                    + ANTI_SPAM_LIMIT + " par " + ANTI_SPAM_HOURS + "h).");

        if (team.getCurrentPlayers() >= team.getMaxPlayers())
            throw new IllegalStateException("L'équipe est complète.");
        if (target.getTeamId() > 0)
            throw new IllegalStateException(target.getUsername() + " est déjà dans une équipe.");

        if (hasPendingInvitation(team.getId(), target.getId()))
            throw new IllegalStateException("Une invitation est déjà en attente pour ce joueur.");

        String sql = "INSERT INTO invitation(team_id, player_id, sent_by, status, message, sent_at, expires_at) " +
                "VALUES (?,?,?,'PENDING',?, NOW(), DATE_ADD(NOW(), INTERVAL ? DAY))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1,    team.getId());
            ps.setInt(2,    target.getId());
            ps.setInt(3,    senderId);
            ps.setString(4, message);
            ps.setInt(5,    DURATION_DAYS);
            ps.executeUpdate();
        }
    }

    // ============================================================
    // ACCEPTER UNE INVITATION (TRANSACTION + VERROU + 6 RÈGLES)
    // ============================================================
    public boolean acceptInvitation(int invitationId, int playerId) throws SQLException {
        cnx.setAutoCommit(false);
        try {
            // ===== ÉTAPE 1 : Verrouiller l'invitation =====
            Invitation inv;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM invitation WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, invitationId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    cnx.rollback();
                    throw new IllegalStateException("Invitation introuvable.");
                }
                inv = mapBasic(rs);
            }

            // ===== RÈGLE 6 : Non expirée =====
            if (inv.getStatus() != Invitation.Status.PENDING) {
                cnx.rollback();
                throw new IllegalStateException("Cette invitation a déjà été traitée.");
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT expires_at FROM invitation WHERE id = ?")) {
                ps.setInt(1, invitationId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Timestamp exp = rs.getTimestamp("expires_at");
                    if (exp != null && exp.before(new Timestamp(System.currentTimeMillis()))) {
                        // Marque comme expirée et rollback
                        try (PreparedStatement upd = cnx.prepareStatement(
                                "UPDATE invitation SET status='REFUSED', replied_at=NOW() WHERE id=?")) {
                            upd.setInt(1, invitationId);
                            upd.executeUpdate();
                        }
                        cnx.commit();
                        throw new IllegalStateException("Cette invitation a expiré.");
                    }
                }
            }

            // Vérifier que c'est bien le bon joueur
            if (inv.getPlayerId() != playerId) {
                cnx.rollback();
                throw new IllegalStateException("Cette invitation n'est pas pour toi.");
            }

            // ===== ÉTAPE 2 : Verrouiller team =====
            int currentPlayers, maxPlayers;
            String teamGame;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT current_players, max_players, game FROM team WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, inv.getTeamId());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    cnx.rollback();
                    throw new IllegalStateException("Équipe introuvable.");
                }
                currentPlayers = rs.getInt("current_players");
                maxPlayers     = rs.getInt("max_players");
                teamGame       = rs.getString("game");
            }

            // ===== RÈGLE 1 : Équipe non pleine =====
            if (currentPlayers >= maxPlayers) {
                cnx.rollback();
                throw new IllegalStateException("L'équipe est désormais complète.");
            }

            // ===== ÉTAPE 3 : Verrouiller player =====
            Integer existingTeam;
            String  playerGame;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT team_id, game FROM player WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, playerId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    cnx.rollback();
                    throw new IllegalStateException("Joueur introuvable.");
                }
                int t = rs.getInt("team_id");
                existingTeam = rs.wasNull() ? null : t;
                playerGame   = rs.getString("game");
            }

            // ===== RÈGLE 2 : Joueur sans équipe =====
            if (existingTeam != null) {
                cnx.rollback();
                throw new IllegalStateException("Tu es déjà dans une équipe. Quitte-la d'abord.");
            }

            // ===== RÈGLE 5 : Pas pour FIFA =====
            if ("fifa".equalsIgnoreCase(playerGame)) {
                cnx.rollback();
                throw new IllegalStateException("Les joueurs FIFA ne peuvent pas rejoindre une équipe.");
            }
            // Vérif game cohérence
            if (!teamGame.equalsIgnoreCase(playerGame)) {
                cnx.rollback();
                throw new IllegalStateException("Le jeu ne correspond pas (équipe : "
                        + teamGame + ", joueur : " + playerGame + ").");
            }

            // ===== TOUTES LES RÈGLES OK : APPLIQUER LES MISES À JOUR =====

            // Marquer invitation = ACCEPTED
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE invitation SET status='ACCEPTED', replied_at=NOW() WHERE id=?")) {
                ps.setInt(1, invitationId);
                ps.executeUpdate();
            }
            // Affecter team_id
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE player SET team_id=? WHERE id=?")) {
                ps.setInt(1, inv.getTeamId());
                ps.setInt(2, playerId);
                ps.executeUpdate();
            }
            // Incrémenter compteur
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE team SET current_players = current_players + 1 WHERE id=?")) {
                ps.setInt(1, inv.getTeamId());
                ps.executeUpdate();
            }
            // Refuser auto les autres invitations PENDING du même joueur
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE invitation SET status='REFUSED', replied_at=NOW() " +
                            "WHERE player_id=? AND status='PENDING' AND id <> ?")) {
                ps.setInt(1, playerId);
                ps.setInt(2, invitationId);
                ps.executeUpdate();
            }

            cnx.commit();
            return true;

        } catch (SQLException | IllegalStateException e) {
            try { cnx.rollback(); } catch (Exception ignored) {}
            if (e instanceof SQLException sqe) throw sqe;
            throw (IllegalStateException) e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    /** Pour conserver la compatibilité avec l'ancienne signature. */
    public void accept(Invitation inv) throws SQLException {
        acceptInvitation(inv.getId(), inv.getPlayerId());
    }

    public void refuse(Invitation inv) throws SQLException {
        if (inv.getStatus() != Invitation.Status.PENDING)
            throw new IllegalStateException("Cette invitation a déjà été traitée.");
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE invitation SET status='REFUSED', replied_at=NOW() WHERE id=?")) {
            ps.setInt(1, inv.getId());
            ps.executeUpdate();
        }
    }

    // ============================================================
    // HELPERS / QUERIES
    // ============================================================

    /** RÈGLE 4 : compte les invitations envoyées par l'équipe dans les X dernières heures. */
    private int countRecentInvitations(int teamId, int hours) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invitation " +
                "WHERE team_id = ? AND sent_at > DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, hours);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean hasPendingInvitation(int teamId, int playerId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM invitation WHERE team_id=? AND player_id=? AND status='PENDING'")) {
            ps.setInt(1, teamId);
            ps.setInt(2, playerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int countPendingForPlayer(int playerId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM invitation " +
                        "WHERE player_id=? AND status='PENDING' AND (expires_at IS NULL OR expires_at > NOW())")) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Invitation> findInvitationsForPlayer(int playerId) throws SQLException {
        String sql = "SELECT i.*, t.name AS team_name, s.username AS sender_name, p.username AS player_name " +
                "FROM invitation i " +
                "JOIN team   t ON t.id = i.team_id " +
                "JOIN player s ON s.id = i.sent_by " +
                "JOIN player p ON p.id = i.player_id " +
                "WHERE i.player_id = ? ORDER BY i.sent_at DESC";
        return query(sql, playerId);
    }

    public List<Invitation> findInvitationsForTeam(int teamId) throws SQLException {
        String sql = "SELECT i.*, t.name AS team_name, s.username AS sender_name, p.username AS player_name " +
                "FROM invitation i " +
                "JOIN team   t ON t.id = i.team_id " +
                "JOIN player s ON s.id = i.sent_by " +
                "JOIN player p ON p.id = i.player_id " +
                "WHERE i.team_id = ? ORDER BY i.sent_at DESC";
        return query(sql, teamId);
    }

    private List<Invitation> query(String sql, int param) throws SQLException {
        List<Invitation> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Invitation mapBasic(ResultSet rs) throws SQLException {
        Invitation i = new Invitation();
        i.setId(rs.getInt("id"));
        i.setTeamId(rs.getInt("team_id"));
        i.setPlayerId(rs.getInt("player_id"));
        i.setSentBy(rs.getInt("sent_by"));
        i.setStatus(Invitation.Status.valueOf(rs.getString("status")));
        i.setMessage(rs.getString("message"));
        i.setSentAt(rs.getTimestamp("sent_at"));
        i.setRepliedAt(rs.getTimestamp("replied_at"));
        return i;
    }

    private Invitation map(ResultSet rs) throws SQLException {
        Invitation i = mapBasic(rs);
        i.setTeamName(rs.getString("team_name"));
        i.setPlayerName(rs.getString("player_name"));
        i.setSenderName(rs.getString("sender_name"));
        return i;
    }
}