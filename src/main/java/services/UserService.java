package services;

import entities.User;
import interfaces.IUserService;
import utils.Mydatabase;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserService implements IUserService {

    private final Connection cnx;

    public UserService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req = "INSERT INTO user(email, roles, password, username, role_type, status, bio, favorite_game, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, "[\"ROLE_USER\"]");
            ps.setString(3, hashIfNeeded(user.getPassword()));
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getRoleType());
            ps.setString(6, user.getStatus());
            ps.setString(7, user.getBio());
            ps.setString(8, user.getFavoriteGame());
            ps.setTimestamp(9, user.getCreatedAt());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(User user) throws SQLException {
        String req = "UPDATE user SET email=?, password=?, username=?, role_type=?, status=?, bio=?, favorite_game=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, hashIfNeeded(user.getPassword()));
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getRoleType());
            ps.setString(5, user.getStatus());
            ps.setString(6, user.getBio());
            ps.setString(7, user.getFavoriteGame());
            ps.setInt(8, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM user WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT id, email, password, username, role_type, status, bio, favorite_game, created_at FROM user";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }

        return users;
    }

    @Override
    public List<User> rechercherParUsername(String username) throws SQLException {
        String key = username == null ? "" : username.trim().toLowerCase();

        return afficher().stream()
                .filter(u -> u.getUsername() != null &&
                        u.getUsername().toLowerCase().contains(key))
                .toList();
    }

    @Override
    public List<User> trierParUsername() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<User> trierParDateCreation() throws SQLException {
        return afficher().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public boolean emailExiste(String email) throws SQLException {
        String req = "SELECT id FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean usernameExiste(String username) throws SQLException {
        String req = "SELECT id FROM user WHERE username=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean login(String email, String password) throws SQLException {
        return loginUser(email, password) != null;
    }

    public User loginUser(String email, String password) throws SQLException {
        String req = "SELECT * FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    String hashedInput = PasswordUtils.hashPassword(password);

                    if (dbPassword.equals(hashedInput)) {
                        return mapUser(rs);
                    }
                }
            }
        }

        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }

        return null;
    }

    public String getRoleByEmail(String email) throws SQLException {
        String req = "SELECT role_type FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role_type");
                }
            }
        }

        return null;
    }

    public void saveResetCode(String email, String code) throws SQLException {
        String req = "UPDATE user SET reset_code=?, reset_code_expire=DATE_ADD(NOW(), INTERVAL 10 MINUTE) WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, code);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public boolean verifyResetCode(String email, String code) throws SQLException {
        String req = "SELECT id FROM user WHERE email=? AND reset_code=? AND reset_code_expire > NOW()";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.setString(2, code);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updatePasswordByEmail(String email, String newPassword) throws SQLException {
        String req = "UPDATE user SET password=?, reset_code=NULL, reset_code_expire=NULL WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, hashIfNeeded(newPassword));
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("username"),
                rs.getString("role_type"),
                rs.getString("status"),
                rs.getString("bio"),
                rs.getString("favorite_game"),
                rs.getTimestamp("created_at")
        );
    }

    private String hashIfNeeded(String password) {
        if (password == null) {
            return null;
        }

        if (isSha256Hash(password)) {
            return password;
        }

        return PasswordUtils.hashPassword(password);
    }

    private boolean isSha256Hash(String value) {
        return value != null && value.matches("^[a-fA-F0-9]{64}$");
    }
}