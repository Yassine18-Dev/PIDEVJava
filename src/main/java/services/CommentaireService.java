package services;

import entities.Commentaire;
import interfaces.ICommentaireService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements ICommentaireService {

    Connection cnx;

    public CommentaireService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Commentaire commentaire) throws SQLException {
        String req = "INSERT INTO commentaire(contenu, date_creation, post_id, user_id) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, commentaire.getContenu());
        ps.setTimestamp(2, commentaire.getDateCreation());
        ps.setInt(3, commentaire.getPostId());
        ps.setInt(4, commentaire.getUserId());
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            commentaire.setId(rs.getInt(1));
        }
        System.out.println("Commentaire ajouté avec succès.");
    }

    @Override
    public void modifier(Commentaire commentaire) throws SQLException {
        String req = "UPDATE commentaire SET contenu=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, commentaire.getContenu());
        ps.setInt(2, commentaire.getId());
        ps.executeUpdate();
        System.out.println("Commentaire modifié avec succès.");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Commentaire supprimé avec succès.");
    }

    @Override
    public List<Commentaire> afficherParPost(int postId) throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT * FROM commentaire WHERE post_id=? ORDER BY date_creation ASC";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            commentaires.add(new Commentaire(
                    rs.getInt("id"),
                    rs.getString("contenu"),
                    rs.getTimestamp("date_creation"),
                    rs.getInt("post_id"),
                    rs.getInt("user_id")
            ));
        }
        return commentaires;
    }

    @Override
    public Commentaire getById(int id) throws SQLException {
        String req = "SELECT * FROM commentaire WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            return new Commentaire(
                    rs.getInt("id"),
                    rs.getString("contenu"),
                    rs.getTimestamp("date_creation"),
                    rs.getInt("post_id"),
                    rs.getInt("user_id")
            );
        }
        return null;
    }
}
