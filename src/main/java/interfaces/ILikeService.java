package interfaces;

import entities.Like;

import java.sql.SQLException;
import java.util.List;

public interface ILikeService {
    void ajouter(Like like) throws SQLException;
    void supprimer(int postId, int userId) throws SQLException;
    boolean aLike(int postId, int userId) throws SQLException;
    int getNombreLikes(int postId) throws SQLException;
    List<Like> afficher() throws SQLException;
}
