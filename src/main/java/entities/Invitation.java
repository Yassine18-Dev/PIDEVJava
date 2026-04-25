package entities;

import java.sql.Timestamp;

public class Invitation {

    public enum Status { PENDING, ACCEPTED, REFUSED }

    private int       id;
    private int       teamId;
    private int       playerId;
    private int       sentBy;
    private Status    status;
    private String    message;
    private Timestamp sentAt;
    private Timestamp repliedAt;

    private String teamName;
    private String playerName;
    private String senderName;

    public Invitation() { this.status = Status.PENDING; }

    public Invitation(int teamId, int playerId, int sentBy, String message) {
        this.teamId = teamId;
        this.playerId = playerId;
        this.sentBy = sentBy;
        this.message = message;
        this.status = Status.PENDING;
    }

    public int       getId()                  { return id; }
    public void      setId(int v)             { this.id = v; }
    public int       getTeamId()              { return teamId; }
    public void      setTeamId(int v)         { this.teamId = v; }
    public int       getPlayerId()            { return playerId; }
    public void      setPlayerId(int v)       { this.playerId = v; }
    public int       getSentBy()              { return sentBy; }
    public void      setSentBy(int v)         { this.sentBy = v; }
    public Status    getStatus()              { return status; }
    public void      setStatus(Status v)      { this.status = v; }
    public String    getMessage()             { return message; }
    public void      setMessage(String v)     { this.message = v; }
    public Timestamp getSentAt()              { return sentAt; }
    public void      setSentAt(Timestamp v)   { this.sentAt = v; }
    public Timestamp getRepliedAt()           { return repliedAt; }
    public void      setRepliedAt(Timestamp v){ this.repliedAt = v; }

    public String getTeamName()           { return teamName; }
    public void   setTeamName(String v)   { this.teamName = v; }
    public String getPlayerName()         { return playerName; }
    public void   setPlayerName(String v) { this.playerName = v; }
    public String getSenderName()         { return senderName; }
    public void   setSenderName(String v) { this.senderName = v; }
}