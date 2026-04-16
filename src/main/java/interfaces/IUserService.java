package interfaces;

import entities.User;

import java.sql.SQLException;
import java.util.List;

public interface IUserService {
    void ajouter(User user) throws SQLException;
    void modifier(User user) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<User> afficher() throws SQLException;

    List<User> rechercherParUsername(String username) throws SQLException;
    List<User> trierParUsername() throws SQLException;
    List<User> trierParDateCreation() throws SQLException;

    boolean emailExiste(String email) throws SQLException;
    boolean usernameExiste(String username) throws SQLException;
    boolean login(String email, String password) throws SQLException;
}