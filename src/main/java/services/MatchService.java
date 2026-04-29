package services;

import entities.Match;
import interfaces.IMatchService;
import utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchService implements IMatchService {

    private final Connection cnx;

    public MatchService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouterMatch(Match match) throws SQLException {
        assurerColonnesMatch();

        String sql = "INSERT INTO matchs(equipe1, equipe2, dateMatch, heureMatch, score, tournoi_id, team1_id, team2_id, rappel_envoye, etat) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, false, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, match.getEquipe1());
        ps.setString(2, match.getEquipe2());
        ps.setString(3, match.getDateMatch());
        ps.setString(4, match.getHeureMatch());
        ps.setString(5, match.getScore() == null || match.getScore().isBlank() ? "0-0" : match.getScore());

        if (match.getTournoiId() > 0) ps.setInt(6, match.getTournoiId());
        else ps.setNull(6, Types.INTEGER);

        if (match.getTeam1Id() > 0) ps.setInt(7, match.getTeam1Id());
        else ps.setNull(7, Types.INTEGER);

        if (match.getTeam2Id() > 0) ps.setInt(8, match.getTeam2Id());
        else ps.setNull(8, Types.INTEGER);

        ps.setString(9, match.getEtat() == null || match.getEtat().isBlank() ? "A_VENIR" : match.getEtat());

        ps.executeUpdate();
    }

    @Override
    public void modifierMatch(Match match) throws SQLException {
        assurerColonnesMatch();

        String sql = "UPDATE matchs SET equipe1=?, equipe2=?, dateMatch=?, heureMatch=?, score=?, tournoi_id=?, team1_id=?, team2_id=?, etat=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, match.getEquipe1());
        ps.setString(2, match.getEquipe2());
        ps.setString(3, match.getDateMatch());
        ps.setString(4, match.getHeureMatch());
        ps.setString(5, match.getScore() == null || match.getScore().isBlank() ? "0-0" : match.getScore());

        if (match.getTournoiId() > 0) ps.setInt(6, match.getTournoiId());
        else ps.setNull(6, Types.INTEGER);

        if (match.getTeam1Id() > 0) ps.setInt(7, match.getTeam1Id());
        else ps.setNull(7, Types.INTEGER);

        if (match.getTeam2Id() > 0) ps.setInt(8, match.getTeam2Id());
        else ps.setNull(8, Types.INTEGER);

        ps.setString(9, match.getEtat() == null || match.getEtat().isBlank() ? "A_VENIR" : match.getEtat());

        ps.setInt(10, match.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimerMatch(int id) throws SQLException {
        String sql = "DELETE FROM matchs WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Match> afficherMatchs() throws SQLException {
        assurerColonnesMatch();

        List<Match> list = new ArrayList<>();
        String sql = "SELECT * FROM matchs ORDER BY dateMatch ASC, heureMatch ASC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(lireMatch(rs));
        }

        return list;
    }

    public List<Match> afficherMatchsSansRappel() throws SQLException {
        assurerColonnesMatch();

        List<Match> list = new ArrayList<>();

        String sql = "SELECT * FROM matchs " +
                "WHERE rappel_envoye = false OR rappel_envoye = 0 OR rappel_envoye IS NULL " +
                "ORDER BY dateMatch ASC, heureMatch ASC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(lireMatch(rs));
        }

        return list;
    }

    @Override
    public Match getMatchById(int id) throws SQLException {
        assurerColonnesMatch();

        String sql = "SELECT * FROM matchs WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return lireMatch(rs);
        }

        return null;
    }

    @Override
    public List<Match> trierParDate() throws SQLException {
        assurerColonnesMatch();

        List<Match> list = new ArrayList<>();
        String sql = "SELECT * FROM matchs ORDER BY dateMatch ASC, heureMatch ASC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(lireMatch(rs));
        }

        return list;
    }

    public List<Match> afficherMatchsParTournoi(int tournoiId) throws SQLException {
        assurerColonnesMatch();

        List<Match> list = new ArrayList<>();

        String sql = "SELECT * FROM matchs WHERE tournoi_id=? ORDER BY dateMatch ASC, heureMatch ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, tournoiId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(lireMatch(rs));
        }

        return list;
    }

    public List<Match> afficherMatchsHorsTournoi() throws SQLException {
        assurerColonnesMatch();

        List<Match> list = new ArrayList<>();

        String sql = "SELECT * FROM matchs WHERE tournoi_id IS NULL OR tournoi_id=0 ORDER BY dateMatch ASC, heureMatch ASC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(lireMatch(rs));
        }

        return list;
    }

    public void marquerRappelEnvoye(int id) throws SQLException {
        assurerColonnesMatch();

        String sql = "UPDATE matchs SET rappel_envoye = true WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private Match lireMatch(ResultSet rs) throws SQLException {
        Match m = new Match();

        m.setId(rs.getInt("id"));
        m.setEquipe1(rs.getString("equipe1"));
        m.setEquipe2(rs.getString("equipe2"));
        m.setDateMatch(rs.getString("dateMatch"));
        m.setHeureMatch(lireString(rs, "heureMatch"));
        m.setScore(rs.getString("score"));

        int tournoiId = rs.getInt("tournoi_id");
        m.setTournoiId(rs.wasNull() ? 0 : tournoiId);

        int team1Id = rs.getInt("team1_id");
        m.setTeam1Id(rs.wasNull() ? 0 : team1Id);

        int team2Id = rs.getInt("team2_id");
        m.setTeam2Id(rs.wasNull() ? 0 : team2Id);

        String etat = lireString(rs, "etat");
        m.setEtat(etat == null || etat.isBlank() ? "A_VENIR" : etat);

        return m;
    }

    private String lireString(ResultSet rs, String colonne) {
        try {
            String value = rs.getString(colonne);
            return value == null ? "" : value;
        } catch (Exception e) {
            return "";
        }
    }

    private void assurerColonnesMatch() throws SQLException {
        if (!colonneExiste("matchs", "heureMatch")) {
            Statement st = cnx.createStatement();
            st.executeUpdate("ALTER TABLE matchs ADD COLUMN heureMatch VARCHAR(10) NULL AFTER dateMatch");
        }

        if (!colonneExiste("matchs", "team1_id")) {
            Statement st = cnx.createStatement();
            st.executeUpdate("ALTER TABLE matchs ADD COLUMN team1_id INT NULL");
        }

        if (!colonneExiste("matchs", "team2_id")) {
            Statement st = cnx.createStatement();
            st.executeUpdate("ALTER TABLE matchs ADD COLUMN team2_id INT NULL");
        }

        if (!colonneExiste("matchs", "rappel_envoye")) {
            Statement st = cnx.createStatement();
            st.executeUpdate("ALTER TABLE matchs ADD COLUMN rappel_envoye BOOLEAN DEFAULT FALSE");
        }

        if (!colonneExiste("matchs", "etat")) {
            Statement st = cnx.createStatement();
            st.executeUpdate("ALTER TABLE matchs ADD COLUMN etat VARCHAR(20) DEFAULT 'A_VENIR'");
        }
    }

    private boolean colonneExiste(String table, String colonne) throws SQLException {
        DatabaseMetaData metaData = cnx.getMetaData();

        ResultSet rs = metaData.getColumns(null, null, table, colonne);
        if (rs.next()) return true;

        rs = metaData.getColumns(null, null, table.toLowerCase(), colonne);
        if (rs.next()) return true;

        rs = metaData.getColumns(null, null, table.toUpperCase(), colonne);
        return rs.next();
    }
}