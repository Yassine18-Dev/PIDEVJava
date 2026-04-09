package entities;

import java.sql.Timestamp;

public class Like {
    private int id;
    private int postId;
    private int userId;
    private Timestamp dateLike;

    public Like() {
    }

    public Like(int postId, int userId, Timestamp dateLike) {
        this.postId = postId;
        this.userId = userId;
        this.dateLike = dateLike;
    }

    public Like(int id, int postId, int userId, Timestamp dateLike) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.dateLike = dateLike;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getDateLike() {
        return dateLike;
    }

    public void setDateLike(Timestamp dateLike) {
        this.dateLike = dateLike;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", dateLike=" + dateLike +
                '}';
    }
}
