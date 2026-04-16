package entities;

import java.sql.Timestamp;

public class User {
    private int id;
    private String email;
    private String password;
    private String username;
    private String roleType;
    private String status;
    private String bio;
    private String favoriteGame;
    private Timestamp createdAt;

    public User() {
    }

    public User(int id, String email, String password, String username,
                String roleType, String status, String bio,
                String favoriteGame, Timestamp createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.roleType = roleType;
        this.status = status;
        this.bio = bio;
        this.favoriteGame = favoriteGame;
        this.createdAt = createdAt;
    }

    public User(String email, String password, String username,
                String roleType, String status, String bio,
                String favoriteGame, Timestamp createdAt) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.roleType = roleType;
        this.status = status;
        this.bio = bio;
        this.favoriteGame = favoriteGame;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getFavoriteGame() { return favoriteGame; }
    public void setFavoriteGame(String favoriteGame) { this.favoriteGame = favoriteGame; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}