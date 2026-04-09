package interfaces;

import entities.Commentaire;

import java.sql.SQLException;
import java.util.List;

public interface ICommentaireService {
    void ajouter(Commentaire commentaire) throws SQLException;
    void modifier(Commentaire commentaire) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Commentaire> afficherParPost(int postId) throws SQLException;
    Commentaire getById(int id) throws SQLException;
}
