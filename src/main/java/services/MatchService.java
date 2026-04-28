package services;

import entities.Match;
import interfaces.IMatchService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchService implements IMatchService {

    Connection cnx;

    public MatchService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Match m) throws SQLException {
        String req = "INSERT INTO matchs(equipe1, equipe2, date_match, score, tournoi_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, m.getEquipe1());
        ps.setString(2, m.getEquipe2());
        ps.setString(3, m.getDateMatch());
        ps.setString(4, m.getScore());
        ps.setInt(5, m.getTournoiId());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Match m) throws SQLException {
        String req = "UPDATE matchs SET equipe1=?, equipe2=?, date_match=?, score=?, tournoi_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, m.getEquipe1());
        ps.setString(2, m.getEquipe2());
        ps.setString(3, m.getDateMatch());
        ps.setString(4, m.getScore());
        ps.setInt(5, m.getTournoiId());
        ps.setInt(6, m.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM matchs WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Match> afficher() throws SQLException {
        List<Match> list = new ArrayList<>();
        String req = "SELECT * FROM matchs";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(new Match(
                    rs.getInt("id"),
                    rs.getString("equipe1"),
                    rs.getString("equipe2"),
                    rs.getString("date_match"),
                    rs.getString("score"),
                    rs.getInt("tournoi_id")
            ));
        }
        return list;
    }

    @Override
    public List<Match> rechercherParEquipe(String equipe) throws SQLException {
        return afficher().stream()
                .filter(m -> m.getEquipe1().toLowerCase().contains(equipe.toLowerCase())
                        || m.getEquipe2().toLowerCase().contains(equipe.toLowerCase()))
                .toList();
    }

    @Override
    public List<Match> trierParDate() throws SQLException {
        return afficher().stream()
                .sorted((m1, m2) -> m1.getDateMatch().compareToIgnoreCase(m2.getDateMatch()))
                .toList();
    }
}