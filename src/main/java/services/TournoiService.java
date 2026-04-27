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
    public void ajouterTournoi(Tournoi tournoi) throws SQLException {
        String sql = "INSERT INTO tournoi(nom, lieu, dateDebut, dateFin) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, tournoi.getNom());
        ps.setString(2, tournoi.getLieu());
        ps.setString(3, tournoi.getDateDebut());
        ps.setString(4, tournoi.getDateFin());
        ps.executeUpdate();
        System.out.println("Tournoi ajouté avec succès");
    }

    @Override
    public void modifierTournoi(Tournoi tournoi) throws SQLException {
        String sql = "UPDATE tournoi SET nom=?, lieu=?, dateDebut=?, dateFin=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, tournoi.getNom());
        ps.setString(2, tournoi.getLieu());
        ps.setString(3, tournoi.getDateDebut());
        ps.setString(4, tournoi.getDateFin());
        ps.setInt(5, tournoi.getId());
        ps.executeUpdate();
        System.out.println("Tournoi modifié avec succès");
    }

    @Override
    public void supprimerTournoi(int id) throws SQLException {
        String sql = "DELETE FROM tournoi WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Tournoi supprimé avec succès");
    }

    @Override
    public List<Tournoi> afficherTournois() throws SQLException {
        List<Tournoi> list = new ArrayList<>();
        String sql = "SELECT * FROM tournoi";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Tournoi t = new Tournoi();
            t.setId(rs.getInt("id"));
            t.setNom(rs.getString("nom"));
            t.setLieu(rs.getString("lieu"));
            t.setDateDebut(rs.getString("dateDebut"));
            t.setDateFin(rs.getString("dateFin"));
            list.add(t);
        }

        return list;
    }

    @Override
    public Tournoi getTournoiById(int id) throws SQLException {
        String sql = "SELECT * FROM tournoi WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Tournoi(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("lieu"),
                    rs.getString("dateDebut"),
                    rs.getString("dateFin")
            );
        }
        return null;
    }
}