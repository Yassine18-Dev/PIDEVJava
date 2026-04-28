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

    private String avatar;
    private int    vision        = 50;
    private int    shooting      = 50;
    private int    reflex        = 50;
    private int    teamplay      = 50;
    private int    communication = 50;
    private double winrate       = 50.0;
    private double kda           = 1.0;
    private int    mvpCount      = 0;

    // ===== Confirmation email =====
    private String    pendingEmail;
    private String    emailConfirmationToken;
    private Timestamp emailTokenExpires;
    private boolean   emailVerified = true;

    public Player() { this.teamId = 0; }

    public Player(String username, String email, String password, String game,
                  String rank, int leaguePoints, int teamId) {
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
        this.teamId = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public int getLeaguePoints() { return leaguePoints; }
    public void setLeaguePoints(int leaguePoints) { this.leaguePoints = leaguePoints; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public Timestamp getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Timestamp registeredAt) { this.registeredAt = registeredAt; }

    public String getAvatar()             { return avatar; }
    public void   setAvatar(String v)     { this.avatar = v; }
    public int    getVision()             { return vision; }
    public void   setVision(int v)        { this.vision = v; }
    public int    getShooting()           { return shooting; }
    public void   setShooting(int v)      { this.shooting = v; }
    public int    getReflex()             { return reflex; }
    public void   setReflex(int v)        { this.reflex = v; }
    public int    getTeamplay()           { return teamplay; }
    public void   setTeamplay(int v)      { this.teamplay = v; }
    public int    getCommunication()      { return communication; }
    public void   setCommunication(int v) { this.communication = v; }
    public double getWinrate()            { return winrate; }
    public void   setWinrate(double v)    { this.winrate = v; }
    public double getKda()                { return kda; }
    public void   setKda(double v)        { this.kda = v; }
    public int    getMvpCount()           { return mvpCount; }
    public void   setMvpCount(int v)      { this.mvpCount = v; }

    public String    getPendingEmail()                        { return pendingEmail; }
    public void      setPendingEmail(String v)                { this.pendingEmail = v; }
    public String    getEmailConfirmationToken()              { return emailConfirmationToken; }
    public void      setEmailConfirmationToken(String v)      { this.emailConfirmationToken = v; }
    public Timestamp getEmailTokenExpires()                   { return emailTokenExpires; }
    public void      setEmailTokenExpires(Timestamp v)        { this.emailTokenExpires = v; }
    public boolean   isEmailVerified()                        { return emailVerified; }
    public void      setEmailVerified(boolean v)              { this.emailVerified = v; }

    public boolean hasPendingEmailChange() {
        return pendingEmail != null && !pendingEmail.isBlank();
    }

    @Override
    public String toString() {
        return username + " — " + (rank == null ? "Unranked" : rank);
    }
}