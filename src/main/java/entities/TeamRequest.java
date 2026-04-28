package entities;

import java.sql.Timestamp;

public class TeamRequest {

    public enum Status { PENDING, ACCEPTED, REFUSED }

    private int       id;
    private int       teamId;
    private int       playerId;
    private Status    status;
    private String    message;
    private Timestamp sentAt;
    private Timestamp repliedAt;

    // Champs joints (pour l'affichage)
    private String teamName;
    private String teamGame;
    private String playerName;
    private String playerRank;
    private int    playerLP;

    public TeamRequest() { this.status = Status.PENDING; }

    public TeamRequest(int teamId, int playerId, String message) {
        this.teamId   = teamId;
        this.playerId = playerId;
        this.message  = message;
        this.status   = Status.PENDING;
    }

    public int       getId()                   { return id; }
    public void      setId(int v)              { this.id = v; }
    public int       getTeamId()               { return teamId; }
    public void      setTeamId(int v)          { this.teamId = v; }
    public int       getPlayerId()             { return playerId; }
    public void      setPlayerId(int v)        { this.playerId = v; }
    public Status    getStatus()               { return status; }
    public void      setStatus(Status v)       { this.status = v; }
    public String    getMessage()              { return message; }
    public void      setMessage(String v)      { this.message = v; }
    public Timestamp getSentAt()               { return sentAt; }
    public void      setSentAt(Timestamp v)    { this.sentAt = v; }
    public Timestamp getRepliedAt()            { return repliedAt; }
    public void      setRepliedAt(Timestamp v) { this.repliedAt = v; }

    public String getTeamName()             { return teamName; }
    public void   setTeamName(String v)     { this.teamName = v; }
    public String getTeamGame()             { return teamGame; }
    public void   setTeamGame(String v)     { this.teamGame = v; }
    public String getPlayerName()           { return playerName; }
    public void   setPlayerName(String v)   { this.playerName = v; }
    public String getPlayerRank()           { return playerRank; }
    public void   setPlayerRank(String v)   { this.playerRank = v; }
    public int    getPlayerLP()             { return playerLP; }
    public void   setPlayerLP(int v)        { this.playerLP = v; }
}