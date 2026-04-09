package services;

import entities.Jeu;
import interfaces.IJeuService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JeuService implements IJeuService {

    Connection cnx;

    public JeuService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Jeu j) throws SQLException {
        String req = "INSERT INTO jeu(nom, studio, date_sortie, mode_jeu, categorie_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, j.getNom());
        ps.setString(2, j.getStudio());
        ps.setDate(3, j.getDateSortie());
        ps.setString(4, j.getModeJeu());
        ps.setInt(5, j.getCategorieId());
        ps.executeUpdate();
        System.out.println("Jeu ajouté.");
    }

    @Override
    public void modifier(Jeu j) throws SQLException {
        String req = "UPDATE jeu SET nom=?, studio=?, date_sortie=?, mode_jeu=?, categorie_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, j.getNom());
        ps.setString(2, j.getStudio());
        ps.setDate(3, j.getDateSortie());
        ps.setString(4, j.getModeJeu());
        ps.setInt(5, j.getCategorieId());
        ps.setInt(6, j.getId());
        ps.executeUpdate();
        System.out.println("Jeu modifié.");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM jeu WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Jeu supprimé.");
    }

    @Override
    public List<Jeu> afficher() throws SQLException {
        List<Jeu> jeux = new ArrayList<>();
        String req = "SELECT * FROM jeu";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            jeux.add(new Jeu(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("studio"),
                    rs.getDate("date_sortie"),
                    rs.getString("mode_jeu"),
                    rs.getInt("categorie_id")
            ));
        }
        return jeux;
    }

    @Override
    public List<Jeu> rechercherParNom(String nom) throws SQLException {
        return afficher().stream()
                .filter(j -> j.getNom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }
    @Override
    public List<Jeu> trierParNom() throws SQLException {
        return afficher().stream()
                .sorted((j1, j2) -> j1.getNom().compareToIgnoreCase(j2.getNom()))
                .toList();
    }
    @Override
    public List<Jeu> trierParDateSortie() throws SQLException {
        return afficher().stream()
                .sorted((j1, j2) -> j2.getDateSortie().compareTo(j1.getDateSortie()))
                .toList();
    }
}