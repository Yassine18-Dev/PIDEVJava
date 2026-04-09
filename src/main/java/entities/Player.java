package entities;

import java.sql.Timestamp;

public class Player {

    private int id;
    private String username;
    private String email;
    private String password;
    private String game;
    private String rank;
    private int leaguePoints;
    private int teamId;
    private Timestamp registeredAt;

    public Player() {
        this.teamId = -1;
    }

    public Player(String username, String email, String password, String game, String rank, int leaguePoints, int teamId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.game = game;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.teamId = teamId;
    }

    public Player(String username, String email, String game, String rank, int leaguePoints) {
        this.username = username;
        this.email = email;
        this.password = "password123";
        this.game = game;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.teamId = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }


    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }


    public int getLeaguePoints() {
        return leaguePoints;
    }

    public void setLeaguePoints(int leaguePoints) {
        this.leaguePoints = leaguePoints;
    }


    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }


    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", game='" + game + '\'' +
                ", rank='" + rank + '\'' +
                ", leaguePoints=" + leaguePoints +
                ", teamId=" + teamId +
                ", registeredAt=" + registeredAt +
                '}';
    }
}