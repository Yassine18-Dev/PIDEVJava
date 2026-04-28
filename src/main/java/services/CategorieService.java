package services;

import entities.Categorie;
import interfaces.ICategorieService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements ICategorieService {

    Connection cnx;

    public CategorieService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Categorie c) throws SQLException {
        String req = "INSERT INTO categorie(nom, description) VALUES (?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Categorie c) throws SQLException {
        String req = "UPDATE categorie SET nom=?, description=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setInt(3, c.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM categorie WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Categorie> afficher() throws SQLException {
        List<Categorie> list = new ArrayList<>();
        String req = "SELECT * FROM categorie";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(new Categorie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description")
            ));
        }
        return list;
    }

    // ✅ Recherche avec Stream
    @Override
    public List<Categorie> rechercherParNom(String nom) throws SQLException {
        return afficher().stream()
                .filter(c -> c.getNom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }

    // ✅ Tri avec Stream
    @Override
    public List<Categorie> trierParNom() throws SQLException {
        return afficher().stream()
                .sorted((c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()))
                .toList();
    }
}