package services;

import entities.Tournoi;
import interfaces.ITournoiService;
import utils.Mydatabase;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TournoiService implements ITournoiService {

    private final Connection cnx;

    public TournoiService() {
        cnx = Mydatabase.getInstance().getCnx();
    }

    @Override
    public void ajouterTournoi(Tournoi tournoi) throws SQLException {
        assurerColonnesTournoi();

        String sql = """
                INSERT INTO tournoi(
                    nom, lieu, dateDebut, dateFin,
                    prix_inscription, max_participants,
                    current_participants, discount_price
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, tournoi.getNom());
        ps.setString(2, tournoi.getLieu());
        ps.setString(3, tournoi.getDateDebut());
        ps.setString(4, tournoi.getDateFin());
        ps.setBigDecimal(5, tournoi.getPrixInscription());
        ps.setInt(6, tournoi.getMaxParticipants());
        ps.setInt(7, tournoi.getCurrentParticipants());

        if (tournoi.getDiscountPrice() != null) {
            ps.setBigDecimal(8, tournoi.getDiscountPrice());
        } else {
            ps.setNull(8, Types.DECIMAL);
        }

        ps.executeUpdate();
    }

    @Override
    public void modifierTournoi(Tournoi tournoi) throws SQLException {
        assurerColonnesTournoi();

        String sql = """
                UPDATE tournoi SET
                    nom=?,
                    lieu=?,
                    dateDebut=?,
                    dateFin=?,
                    prix_inscription=?,
                    max_participants=?,
                    current_participants=?,
                    discount_price=?
                WHERE id=?
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, tournoi.getNom());
        ps.setString(2, tournoi.getLieu());
        ps.setString(3, tournoi.getDateDebut());
        ps.setString(4, tournoi.getDateFin());
        ps.setBigDecimal(5, tournoi.getPrixInscription());
        ps.setInt(6, tournoi.getMaxParticipants());
        ps.setInt(7, tournoi.getCurrentParticipants());

        if (tournoi.getDiscountPrice() != null) {
            ps.setBigDecimal(8, tournoi.getDiscountPrice());
        } else {
            ps.setNull(8, Types.DECIMAL);
        }

        ps.setInt(9, tournoi.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimerTournoi(int id) throws SQLException {
        String sql = "DELETE FROM tournoi WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Tournoi> afficherTournois() throws SQLException {
        assurerColonnesTournoi();

        List<Tournoi> list = new ArrayList<>();
        String sql = "SELECT * FROM tournoi";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(lireTournoi(rs));
        }

        return list;
    }

    @Override
    public Tournoi getTournoiById(int id) throws SQLException {
        assurerColonnesTournoi();

        String sql = "SELECT * FROM tournoi WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return lireTournoi(rs);
        }

        return null;
    }

    public BigDecimal getPrix(int idTournoi) throws SQLException {
        Tournoi tournoi = getTournoiById(idTournoi);

        if (tournoi == null) {
            return BigDecimal.ZERO;
        }

        return tournoi.getPrixFinal();
    }

    public boolean teamDejaInscrite(int teamId, int tournoiId) throws SQLException {
        creerTableInscriptionTeamSiNecessaire();

        String sql = """
                SELECT COUNT(*) 
                FROM inscription_team_tournoi 
                WHERE team_id=? AND tournoi_id=?
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, teamId);
        ps.setInt(2, tournoiId);

        ResultSet rs = ps.executeQuery();

        return rs.next() && rs.getInt(1) > 0;
    }

    public void inscrireTeam(int teamId, int tournoiId) throws SQLException {
        creerTableInscriptionTeamSiNecessaire();
        assurerColonnesTournoi();

        Tournoi tournoi = getTournoiById(tournoiId);

        if (tournoi == null) {
            throw new SQLException("Tournoi introuvable.");
        }

        if (tournoi.getCurrentParticipants() >= tournoi.getMaxParticipants()) {
            throw new SQLException("Ce tournoi est complet.");
        }

        if (teamDejaInscrite(teamId, tournoiId)) {
            throw new SQLException("Cette team est déjà inscrite à ce tournoi.");
        }

        BigDecimal prix = tournoi.getPrixFinal();

        String sql = """
                INSERT INTO inscription_team_tournoi(
                    team_id, tournoi_id, montant, date_inscription, statut
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, teamId);
        ps.setInt(2, tournoiId);
        ps.setBigDecimal(3, prix);
        ps.setString(4, LocalDate.now().toString());
        ps.setString(5, "CONFIRMED");

        ps.executeUpdate();

        incrementerParticipants(tournoiId);
    }

    public void incrementerParticipants(int idTournoi) throws SQLException {
        assurerColonnesTournoi();

        String sql = """
                UPDATE tournoi
                SET current_participants = current_participants + 1
                WHERE id=? AND current_participants < max_participants
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idTournoi);
        ps.executeUpdate();
    }

    public boolean dejaInscrit(int idUtilisateur, int idTournoi) throws SQLException {
        creerTableInscriptionSiNecessaire();

        String sql = "SELECT COUNT(*) FROM inscription_tournoi WHERE idUtilisateur=? AND idTournoi=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idUtilisateur);
        ps.setInt(2, idTournoi);

        ResultSet rs = ps.executeQuery();

        return rs.next() && rs.getInt(1) > 0;
    }

    public boolean dejaInscrit(Integer idUtilisateur, int idTournoi) throws SQLException {
        if (idUtilisateur == null) {
            return false;
        }

        return dejaInscrit(idUtilisateur.intValue(), idTournoi);
    }

    public void inscrire(int idUtilisateur, int idTournoi) throws SQLException {
        creerTableInscriptionSiNecessaire();
        assurerColonnesTournoi();

        if (dejaInscrit(idUtilisateur, idTournoi)) {
            return;
        }

        Tournoi tournoi = getTournoiById(idTournoi);

        if (tournoi == null) {
            throw new SQLException("Tournoi introuvable.");
        }

        if (tournoi.getCurrentParticipants() >= tournoi.getMaxParticipants()) {
            throw new SQLException("Ce tournoi est complet.");
        }

        BigDecimal prix = tournoi.getPrixFinal();

        String sql = """
                INSERT INTO inscription_tournoi(
                    idUtilisateur, idTournoi, montant, dateInscription
                )
                VALUES (?, ?, ?, ?)
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idUtilisateur);
        ps.setInt(2, idTournoi);
        ps.setBigDecimal(3, prix);
        ps.setString(4, LocalDate.now().toString());

        ps.executeUpdate();

        incrementerParticipants(idTournoi);
    }

    public void inscrire(Integer idUtilisateur, int idTournoi) throws SQLException {
        if (idUtilisateur == null) {
            throw new SQLException("Utilisateur non connecté");
        }

        inscrire(idUtilisateur.intValue(), idTournoi);
    }

    private Tournoi lireTournoi(ResultSet rs) throws SQLException {
        Tournoi t = new Tournoi();

        t.setId(rs.getInt("id"));
        t.setNom(rs.getString("nom"));
        t.setLieu(rs.getString("lieu"));
        t.setDateDebut(rs.getString("dateDebut"));
        t.setDateFin(rs.getString("dateFin"));
        t.setPrixInscription(lirePrix(rs));
        t.setMaxParticipants(rs.getInt("max_participants"));
        t.setCurrentParticipants(rs.getInt("current_participants"));
        t.setDiscountPrice(rs.getBigDecimal("discount_price"));

        return t;
    }

    private BigDecimal lirePrix(ResultSet rs) {
        try {
            BigDecimal prix = rs.getBigDecimal("prix_inscription");
            if (prix != null) return prix;
        } catch (Exception ignored) {}

        return BigDecimal.ZERO;
    }

    private void assurerColonnesTournoi() throws SQLException {
        ajouterColonneSiManquante("tournoi", "prix_inscription", "DECIMAL(10,2) DEFAULT 0");
        ajouterColonneSiManquante("tournoi", "max_participants", "INT NOT NULL DEFAULT 10");
        ajouterColonneSiManquante("tournoi", "current_participants", "INT NOT NULL DEFAULT 0");
        ajouterColonneSiManquante("tournoi", "discount_price", "DECIMAL(10,2) NULL");
    }

    private void creerTableInscriptionTeamSiNecessaire() throws SQLException {
        String createSql = """
                CREATE TABLE IF NOT EXISTS inscription_team_tournoi (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    team_id INT NOT NULL,
                    tournoi_id INT NOT NULL,
                    montant DECIMAL(10,2) DEFAULT 0,
                    date_inscription VARCHAR(50),
                    statut VARCHAR(30) DEFAULT 'CONFIRMED',
                    UNIQUE(team_id, tournoi_id)
                )
                """;

        Statement st = cnx.createStatement();
        st.executeUpdate(createSql);

        ajouterColonneSiManquante("inscription_team_tournoi", "team_id", "INT NOT NULL");
        ajouterColonneSiManquante("inscription_team_tournoi", "tournoi_id", "INT NOT NULL");
        ajouterColonneSiManquante("inscription_team_tournoi", "montant", "DECIMAL(10,2) DEFAULT 0");
        ajouterColonneSiManquante("inscription_team_tournoi", "date_inscription", "VARCHAR(50)");
        ajouterColonneSiManquante("inscription_team_tournoi", "statut", "VARCHAR(30) DEFAULT 'CONFIRMED'");
    }

    private void creerTableInscriptionSiNecessaire() throws SQLException {
        String createSql = """
                CREATE TABLE IF NOT EXISTS inscription_tournoi (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    idUtilisateur INT NULL,
                    idTournoi INT NULL,
                    montant DECIMAL(10,2) DEFAULT 0,
                    dateInscription VARCHAR(50)
                )
                """;

        Statement st = cnx.createStatement();
        st.executeUpdate(createSql);

        ajouterColonneSiManquante("inscription_tournoi", "idUtilisateur", "INT NULL");
        ajouterColonneSiManquante("inscription_tournoi", "idTournoi", "INT NULL");
        ajouterColonneSiManquante("inscription_tournoi", "montant", "DECIMAL(10,2) DEFAULT 0");
        ajouterColonneSiManquante("inscription_tournoi", "dateInscription", "VARCHAR(50)");
    }

    private void ajouterColonneSiManquante(String table, String colonne, String definition) throws SQLException {
        if (!colonneExiste(table, colonne)) {
            String sql = "ALTER TABLE " + table + " ADD COLUMN " + colonne + " " + definition;
            Statement st = cnx.createStatement();
            st.executeUpdate(sql);
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