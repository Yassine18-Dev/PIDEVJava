package services;

import entities.Match;
import interfaces.IMatchService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchService implements IMatchService {

    private Connection cnx;

    public MatchService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouterMatch(Match match) throws SQLException {
        String sql = "INSERT INTO matchs(equipe1, equipe2, dateMatch, score, tournoiId) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, match.getEquipe1());
        ps.setString(2, match.getEquipe2());
        ps.setString(3, match.getDateMatch());
        ps.setString(4, match.getScore());
        ps.setInt(5, match.getTournoiId());
        ps.executeUpdate();
    }

    @Override
    public void modifierMatch(Match match) throws SQLException {
        String sql = "UPDATE matchs SET equipe1 = ?, equipe2 = ?, dateMatch = ?, score = ?, tournoiId = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, match.getEquipe1());
        ps.setString(2, match.getEquipe2());
        ps.setString(3, match.getDateMatch());
        ps.setString(4, match.getScore());
        ps.setInt(5, match.getTournoiId());
        ps.setInt(6, match.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimerMatch(int id) throws SQLException {
        String sql = "DELETE FROM matchs WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Match> afficherMatchs() throws SQLException {
        List<Match> list = new ArrayList<>();
        String sql = "SELECT * FROM matchs";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Match m = new Match();
            m.setId(rs.getInt("id"));
            m.setEquipe1(rs.getString("equipe1"));
            m.setEquipe2(rs.getString("equipe2"));
            m.setDateMatch(rs.getString("dateMatch"));
            m.setScore(rs.getString("score"));
            m.setTournoiId(rs.getInt("tournoiId"));
            list.add(m);
        }

        return list;
    }

    @Override
    public Match getMatchById(int id) throws SQLException {
        String sql = "SELECT * FROM matchs WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Match(
                    rs.getInt("id"),
                    rs.getString("equipe1"),
                    rs.getString("equipe2"),
                    rs.getString("dateMatch"),
                    rs.getString("score"),
                    rs.getInt("tournoiId")
            );
        }

        return null;
    }

    @Override
    public List<Match> trierParDate() throws SQLException {
        List<Match> list = new ArrayList<>();
        String sql = "SELECT * FROM matchs ORDER BY dateMatch ASC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Match m = new Match();
            m.setId(rs.getInt("id"));
            m.setEquipe1(rs.getString("equipe1"));
            m.setEquipe2(rs.getString("equipe2"));
            m.setDateMatch(rs.getString("dateMatch"));
            m.setScore(rs.getString("score"));
            m.setTournoiId(rs.getInt("tournoiId"));
            list.add(m);
        }

        return list;
    }
}