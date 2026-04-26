package services;

import utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;

public class SecurityService {

    private final Connection cnx;

    public SecurityService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    public boolean isBlocked(String email) throws SQLException {
        String req = "SELECT blocked FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("blocked");
            }
        }
    }

    public boolean isLocked(String email) throws SQLException {
        String req = "SELECT locked_until FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp lockedUntil = rs.getTimestamp("locked_until");

                    return lockedUntil != null &&
                            lockedUntil.toLocalDateTime().isAfter(LocalDateTime.now());
                }
            }
        }

        return false;
    }

    public String getLockedUntil(String email) throws SQLException {
        String req = "SELECT locked_until FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp lockedUntil = rs.getTimestamp("locked_until");

                    if (lockedUntil != null) {
                        return lockedUntil.toString();
                    }
                }
            }
        }

        return "inconnu";
    }

    public void reset(String email) throws SQLException {
        String req = "UPDATE user SET failed_attempts=0, locked_until=NULL WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    public int fail(String email) throws SQLException {
        String req = "SELECT failed_attempts, lock_level FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int attempts = rs.getInt("failed_attempts") + 1;
                    int level = rs.getInt("lock_level");

                    if (attempts < 3) {
                        updateAttempts(email, attempts);
                        return attempts;
                    }

                    if (level == 0) {
                        lock(email, 5, 1);
                        return 5;
                    } else if (level == 1) {
                        lock(email, 15, 2);
                        return 15;
                    } else {
                        block(email);
                        return -1;
                    }
                }
            }
        }

        return 0;
    }

    private void updateAttempts(String email, int attempts) throws SQLException {
        String req = "UPDATE user SET failed_attempts=? WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, attempts);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    private void lock(String email, int minutes, int nextLevel) throws SQLException {
        String req = "UPDATE user SET failed_attempts=0, lock_level=?, locked_until=? WHERE email=?";

        Timestamp lockedUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(minutes));

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, nextLevel);
            ps.setTimestamp(2, lockedUntil);
            ps.setString(3, email);
            ps.executeUpdate();
        }
    }

    private void block(String email) throws SQLException {
        String req = "UPDATE user SET failed_attempts=0, blocked=TRUE WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }
}