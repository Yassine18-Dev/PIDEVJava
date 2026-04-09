package entities;

import java.util.Date;

public class SupportMessage {

    private int id;
    private String message;
    private Date sentAt;
    private int ticketId;
    private int senderId;

    public SupportMessage() {
    }

    public SupportMessage(int id, String message, Date sentAt, int ticketId, int senderId) {
        this.id = id;
        this.message = message;
        this.sentAt = sentAt;
        this.ticketId = ticketId;
        this.senderId = senderId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}