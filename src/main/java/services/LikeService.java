package services;

import entities.Like;
import interfaces.ILikeService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LikeService implements ILikeService {

    Connection cnx;

    public LikeService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Like like) throws SQLException {
        if (aLike(like.getPostId(), like.getUserId())) {
            System.out.println("L'utilisateur a déjà liké ce post.");
            return;
        }
        
        String req = "INSERT INTO likes(post_id, user_id, date_like) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, like.getPostId());
        ps.setInt(2, like.getUserId());
        ps.setTimestamp(3, like.getDateLike());
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            like.setId(rs.getInt(1));
        }
        System.out.println("Like ajouté avec succès.");
    }

    @Override
    public void supprimer(int postId, int userId) throws SQLException {
        String req = "DELETE FROM likes WHERE post_id=? AND user_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, postId);
        ps.setInt(2, userId);
        int rowsAffected = ps.executeUpdate();
        
        if (rowsAffected > 0) {
            System.out.println("Like supprimé avec succès.");
        } else {
            System.out.println("Aucun like trouvé pour ce post et cet utilisateur.");
        }
    }

    @Override
    public boolean aLike(int postId, int userId) throws SQLException {
        String req = "SELECT COUNT(*) FROM likes WHERE post_id=? AND user_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, postId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
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
    public List<Like> afficher() throws SQLException {
        List<Like> likes = new ArrayList<>();
        String req = "SELECT * FROM likes ORDER BY date_like DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            likes.add(new Like(
                    rs.getInt("id"),
                    rs.getInt("post_id"),
                    rs.getInt("user_id"),
                    rs.getTimestamp("date_like")
            ));
        }
        return likes;
    }
}
