package interfaces;

import entities.Post;

import java.sql.SQLException;
import java.util.List;

public interface IPostService {
    void ajouter(Post post) throws SQLException;
    void modifier(Post post) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Post> afficher() throws SQLException;
    Post getById(int id) throws SQLException;
    int getNombreLikes(int postId) throws SQLException;
    int getNombreCommentaires(int postId) throws SQLException;
}
