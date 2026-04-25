package entities;

import java.sql.Timestamp;

public class Team {

    private int id;
    private String name;
    private String game;
    private int maxPlayers;
    private int currentPlayers;
    private int powerScore;
    private String logo;
    private String banner;
    private Timestamp createdAt;
    private int captainId;

    public Team() {
        this.maxPlayers = 5;
        this.currentPlayers = 0;
        this.powerScore = 0;
    }

    public Team(String name, String game) {
        this.name = name;
        this.game = game;
        this.maxPlayers = 5;
        this.currentPlayers = 0;
        this.powerScore = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public int getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; }
    public int getPowerScore() { return powerScore; }
    public void setPowerScore(int powerScore) { this.powerScore = powerScore; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int  getCaptainId()             { return captainId; }
    public void setCaptainId(int v)        { this.captainId = v; }

    public boolean isFull()                { return currentPlayers >= maxPlayers; }
    public boolean canRecruit()            { return currentPlayers < maxPlayers; }
    public boolean isCaptain(int playerId) { return captainId == playerId; }

    @Override
    public String toString() {
        return name + " (" + game + ")";
    }
}