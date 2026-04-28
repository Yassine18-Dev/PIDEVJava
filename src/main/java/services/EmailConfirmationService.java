package services;

import utils.Mydatabase;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;

public class EmailConfirmationService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Code style gaming : ABCD-EFGH-IJKL (12 caractères + 2 tirets). */
    private String generateGamingCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i == 4 || i == 8) sb.append('-');
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static class EmailChangeRequest {
        public final String token;
        public final String gamingCode;
        public final String pendingEmail;

        public EmailChangeRequest(String token, String gamingCode, String pendingEmail) {
            this.token        = token;
            this.gamingCode   = gamingCode;
            this.pendingEmail = pendingEmail;
        }
    }

    /** Démarre une demande de changement d'email. */
    public EmailChangeRequest requestEmailChange(int playerId, String newEmail) throws SQLException {
        // Vérifier que l'email n'est pas déjà utilisé
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM player WHERE email = ? AND id <> ?")) {
            ps.setString(1, newEmail);
            ps.setInt(2,    playerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new IllegalStateException("Cet email est déjà utilisé par un autre joueur.");
        }

        String token      = generateToken();
        String gamingCode = generateGamingCode();
        Timestamp expires = Timestamp.valueOf(LocalDateTime.now().plusHours(24));

        String sql = "UPDATE player SET pending_email=?, email_confirmation_token=?, " +
                "email_token_expires=?, email_verified=FALSE WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1,    newEmail);
            ps.setString(2,    gamingCode);
            ps.setTimestamp(3, expires);
            ps.setInt(4,       playerId);
            ps.executeUpdate();
        }

        // Simulation d'envoi d'email dans la console IntelliJ
        System.out.println("\n========================================================");
        System.out.println("📧 EMAIL ENVOYÉ À : " + newEmail);
        System.out.println("========================================================");
        System.out.println("  ARENA MIND - CONFIRMATION DE CHANGEMENT D'EMAIL");
        System.out.println("--------------------------------------------------------");
        System.out.println("  Bonjour,");
        System.out.println("  Pour confirmer ton nouvel email, copie ce code :");
        System.out.println();
        System.out.println("              " + gamingCode);
        System.out.println();
        System.out.println("  Ce code expire dans 24h.");
        System.out.println("========================================================\n");

        return new EmailChangeRequest(token, gamingCode, newEmail);
    }

    /** Confirme avec le code reçu. */
    public boolean confirmEmail(int playerId, String code) throws SQLException {
        String pendingEmail;
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT pending_email, email_token_expires FROM player " +
                        "WHERE id=? AND email_confirmation_token=?")) {
            ps.setInt(1,    playerId);
            ps.setString(2, code.trim().toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                throw new IllegalStateException("Code invalide. Vérifie qu'il est bien recopié.");

            pendingEmail = rs.getString("pending_email");
            Timestamp exp = rs.getTimestamp("email_token_expires");
            if (exp != null && exp.before(new Timestamp(System.currentTimeMillis())))
                throw new IllegalStateException("Ce code a expiré. Refais une demande.");
        }

        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE player SET email=?, pending_email=NULL, email_confirmation_token=NULL, " +
                        "email_token_expires=NULL, email_verified=TRUE WHERE id=?")) {
            ps.setString(1, pendingEmail);
            ps.setInt(2,    playerId);
            ps.executeUpdate();
        }
        return true;
    }

    /** Annule une demande en cours. */
    public void cancelChange(int playerId) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE player SET pending_email=NULL, email_confirmation_token=NULL, " +
                        "email_token_expires=NULL, email_verified=TRUE WHERE id=?")) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }
}