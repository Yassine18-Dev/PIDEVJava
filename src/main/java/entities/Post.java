package entities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Post {
    private int id;
    private String titre;
    private String contenu;
    private String imageUrl;
    private Timestamp dateCreation;
    private int userId;
    private int likes;
    private List<String> comments;

    // Liste statique partagée pour stocker les posts en mode simulation
    private static List<Post> postsData = new ArrayList<>();

    public Post() {
        this.comments = new ArrayList<>();
        this.likes = 0;
    }

    public Post(String titre, String contenu, String imageUrl, Timestamp dateCreation, int userId) {
        this.titre = titre;
        this.contenu = contenu;
        this.imageUrl = imageUrl;
        this.dateCreation = dateCreation;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.likes = 0;
    }

    public Post(int id, String titre, String contenu, String imageUrl, Timestamp dateCreation, int userId) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.imageUrl = imageUrl;
        this.dateCreation = dateCreation;
        this.userId = userId;
        this.comments = new ArrayList<>();
        this.likes = 0;
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public void addLike() {
        this.likes++;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    // Méthodes statiques pour gérer la liste partagée
    public static List<Post> getPostsData() {
        return postsData;
    }

    public static void addPost(Post post) {
        post.setId(postsData.size() + 1);
        postsData.add(post);
    }

    public static void removePost(Post post) {
        postsData.remove(post);
    }

    public static void updatePost(Post post) {
        int index = postsData.indexOf(post);
        if (index != -1) {
            postsData.set(index, post);
        }
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
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }
}
