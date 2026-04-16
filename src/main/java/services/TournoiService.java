package services;

import entities.Tournoi;
import interfaces.ITournoiService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournoiService implements ITournoiService {

    Connection cnx;

    public TournoiService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Tournoi t) throws SQLException {
        String req = "INSERT INTO tournoi(nom, lieu, date_debut, date_fin) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, t.getNom());
        ps.setString(2, t.getLieu());
        ps.setString(3, t.getDateDebut());
        ps.setString(4, t.getDateFin());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Tournoi t) throws SQLException {
        String req = "UPDATE tournoi SET nom=?, lieu=?, date_debut=?, date_fin=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, t.getNom());
        ps.setString(2, t.getLieu());
        ps.setString(3, t.getDateDebut());
        ps.setString(4, t.getDateFin());
        ps.setInt(5, t.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM tournoi WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Tournoi> afficher() throws SQLException {
        List<Tournoi> list = new ArrayList<>();
        String req = "SELECT * FROM tournoi";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(new Tournoi(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("lieu"),
                    rs.getString("date_debut"),
                    rs.getString("date_fin")
            ));
        }
        return list;
    }

    @Override
    public List<Tournoi> rechercherParNom(String nom) throws SQLException {
        return afficher().stream()
                .filter(t -> t.getNom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }

    @Override
    public List<Tournoi> trierParNom() throws SQLException {
        return afficher().stream()
                .sorted((t1, t2) -> t1.getNom().compareToIgnoreCase(t2.getNom()))
                .toList();
    }
}