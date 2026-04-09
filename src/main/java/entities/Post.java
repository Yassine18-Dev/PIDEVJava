package entities;

import java.sql.Timestamp;

public class Post {
    private int id;
    private String titre;
    private String contenu;
    private String imageUrl;
    private Timestamp dateCreation;
    private int userId;

    public Post() {
    }

    public Post(String titre, String contenu, String imageUrl, Timestamp dateCreation, int userId) {
        this.titre = titre;
        this.contenu = contenu;
        this.imageUrl = imageUrl;
        this.dateCreation = dateCreation;
        this.userId = userId;
    }

    public Post(int id, String titre, String contenu, String imageUrl, Timestamp dateCreation, int userId) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.imageUrl = imageUrl;
        this.dateCreation = dateCreation;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", dateCreation=" + dateCreation +
                ", userId=" + userId +
                '}';
    }
}
