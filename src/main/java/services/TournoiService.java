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
        assurerColonnePrixTournoi();

        String sql = "INSERT INTO tournoi(nom, lieu, dateDebut, dateFin, prix_inscription) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, tournoi.getNom());
        ps.setString(2, tournoi.getLieu());
        ps.setString(3, tournoi.getDateDebut());
        ps.setString(4, tournoi.getDateFin());
        ps.setBigDecimal(5, tournoi.getPrixInscription());

        ps.executeUpdate();
    }

    @Override
    public void modifierTournoi(Tournoi tournoi) throws SQLException {
        assurerColonnePrixTournoi();

        String sql = "UPDATE tournoi SET nom=?, lieu=?, dateDebut=?, dateFin=?, prix_inscription=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, tournoi.getNom());
        ps.setString(2, tournoi.getLieu());
        ps.setString(3, tournoi.getDateDebut());
        ps.setString(4, tournoi.getDateFin());
        ps.setBigDecimal(5, tournoi.getPrixInscription());
        ps.setInt(6, tournoi.getId());

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
        assurerColonnePrixTournoi();

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
            t.setPrixInscription(lirePrix(rs));

            list.add(t);
        }

        return list;
    }

    @Override
    public Tournoi getTournoiById(int id) throws SQLException {
        assurerColonnePrixTournoi();

        String sql = "SELECT * FROM tournoi WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Tournoi t = new Tournoi();

            t.setId(rs.getInt("id"));
            t.setNom(rs.getString("nom"));
            t.setLieu(rs.getString("lieu"));
            t.setDateDebut(rs.getString("dateDebut"));
            t.setDateFin(rs.getString("dateFin"));
            t.setPrixInscription(lirePrix(rs));

            return t;
        }

        return null;
    }

    public BigDecimal getPrix(int idTournoi) throws SQLException {
        Tournoi tournoi = getTournoiById(idTournoi);

        if (tournoi == null || tournoi.getPrixInscription() == null) {
            return BigDecimal.ZERO;
        }

        return tournoi.getPrixInscription();
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

        if (dejaInscrit(idUtilisateur, idTournoi)) {
            return;
        }

        BigDecimal prix = getPrix(idTournoi);

        String sql = "INSERT INTO inscription_tournoi(idUtilisateur, idTournoi, montant, dateInscription) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, idUtilisateur);
        ps.setInt(2, idTournoi);
        ps.setBigDecimal(3, prix);
        ps.setString(4, LocalDate.now().toString());

        ps.executeUpdate();
    }

    public void inscrire(Integer idUtilisateur, int idTournoi) throws SQLException {
        if (idUtilisateur == null) {
            throw new SQLException("Utilisateur non connecté");
        }

        inscrire(idUtilisateur.intValue(), idTournoi);
    }

    private BigDecimal lirePrix(ResultSet rs) {
        try {
            BigDecimal prix = rs.getBigDecimal("prix_inscription");
            if (prix != null) return prix;
        } catch (Exception ignored) {}

        try {
            BigDecimal prix = rs.getBigDecimal("prixInscription");
            if (prix != null) return prix;
        } catch (Exception ignored) {}

        return BigDecimal.ZERO;
    }

    private void assurerColonnePrixTournoi() throws SQLException {
        if (!colonneExiste("tournoi", "prix_inscription")) {
            ajouterColonneSiManquante("tournoi", "prix_inscription", "DECIMAL(10,2) DEFAULT 0");
        }
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