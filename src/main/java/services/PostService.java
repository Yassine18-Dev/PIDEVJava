package services;

import entities.Post;
import interfaces.IPostService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements IPostService {

    Connection cnx;

    public PostService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Post post) throws SQLException {
        String req = "INSERT INTO post(titre, contenu, image_url, date_creation, user_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, post.getTitre());
        ps.setString(2, post.getContenu());
        ps.setString(3, post.getImageUrl());
        ps.setTimestamp(4, post.getDateCreation());
        ps.setInt(5, post.getUserId());
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            post.setId(rs.getInt(1));
        }
        System.out.println("Post ajouté avec succès.");
    }

    @Override
    public void modifier(Post post) throws SQLException {
        String req = "UPDATE post SET titre=?, contenu=?, image_url=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, post.getTitre());
        ps.setString(2, post.getContenu());
        ps.setString(3, post.getImageUrl());
        ps.setInt(4, post.getId());
        ps.executeUpdate();
        System.out.println("Post modifié avec succès.");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM post WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Post supprimé avec succès.");
    }

    @Override
    public List<Post> afficher() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String req = "SELECT * FROM post ORDER BY date_creation DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            posts.add(new Post(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("contenu"),
                    rs.getString("image_url"),
                    rs.getTimestamp("date_creation"),
                    rs.getInt("user_id")
            ));
        }
        return posts;
    }

    @Override
    public Post getById(int id) throws SQLException {
        String req = "SELECT * FROM post WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return new Post(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("contenu"),
                    rs.getString("image_url"),
                    rs.getTimestamp("date_creation"),
                    rs.getInt("user_id")
            );
        }
        return null;
    }

    @Override
    public int getNombreLikes(int postId) throws SQLException {
        String req = "SELECT COUNT(*) FROM likes WHERE post_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    @Override
    public int getNombreCommentaires(int postId) throws SQLException {
        String req = "SELECT COUNT(*) FROM commentaire WHERE post_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
}
