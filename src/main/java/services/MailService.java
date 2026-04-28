package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailService {

    private static final String SMTP_HOST = "sandbox.smtp.mailtrap.io";
    private static final String SMTP_PORT = "2525";

    // Identifiants Mailtrap
    private static final String USERNAME = "04c56976900bf5";
    private static final String PASSWORD = "8dab195bc5ea96";

    private static final String FROM_EMAIL = "support@arenamind.com";

    private Session getSession() {
        Properties props = new Properties();

        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    private void sendMail(String to, String subject, String text) {
        try {
            Message message = new MimeMessage(getSession());

            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

            System.out.println("Email envoyé avec Mailtrap à : " + to);

        } catch (Exception e) {
            System.out.println("Erreur envoi email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendFailedAttempt(String to, int attempt) {
        sendMail(
                to,
                "Alerte sécurité - Tentative échouée",
                "Bonjour,\n\n" +
                        "Une tentative de connexion échouée a été détectée sur votre compte ArenaMind.\n\n" +
                        "Tentative : " + attempt + "/3\n\n" +
                        "Si ce n'était pas vous, veuillez vérifier la sécurité de votre compte.\n\n" +
                        "Support : support@arenamind.com\n\n" +
                        "ArenaMind Security Team"
        );
    }

    public void sendAccountBlockedSupportMail(String to, int minutes) {
        sendMail(
                to,
                "Compte temporairement bloqué - ArenaMind",
                "Bonjour,\n\n" +
                        "Votre compte ArenaMind a été temporairement bloqué pendant " + minutes + " minutes " +
                        "suite à 3 tentatives de connexion échouées.\n\n" +
                        "Si ce n'était pas vous, veuillez contacter le support ArenaMind pour vérifier votre compte.\n\n" +
                        "Support : support@arenamind.com\n\n" +
                        "ArenaMind Security Team"
        );
    }

    public void sendPermanentSupportMail(String to) {
        sendMail(
                to,
                "Compte bloqué définitivement - ArenaMind",
                "Bonjour,\n\n" +
                        "Votre compte ArenaMind a été bloqué définitivement suite à plusieurs tentatives suspectes.\n\n" +
                        "Pour vérifier ou réactiver votre compte, veuillez contacter le support ArenaMind.\n\n" +
                        "Support : support@arenamind.com\n\n" +
                        "ArenaMind Security Team"
        );
    }

    public void sendTemporaryLock(String to, String lockedUntil) {
        sendMail(
                to,
                "Compte temporairement bloqué - ArenaMind",
                "Bonjour,\n\n" +
                        "Votre compte est actuellement bloqué temporairement.\n\n" +
                        "Vous pouvez réessayer après : " + lockedUntil + "\n\n" +
                        "Si ce n'était pas vous, contactez le support.\n\n" +
                        "Support : support@arenamind.com"
        );
    }

    public void sendPermanentBlock(String to) {
        sendPermanentSupportMail(to);
    }

    public void sendBlock5(String to) {
        sendAccountBlockedSupportMail(to, 5);
    }

    public void sendBlock15(String to) {
        sendAccountBlockedSupportMail(to, 15);
    }

    public void sendBlockPermanent(String to) {
        sendPermanentSupportMail(to);
    }

    public void sendResetCode(String to, String code) {
        sendMail(
                to,
                "Code de réinitialisation - ArenaMind",
                "Bonjour,\n\n" +
                        "Votre code de réinitialisation est : " + code + "\n\n" +
                        "Ce code expire dans 10 minutes.\n\n" +
                        "ArenaMind Security Team"
        );
    }

    public void sendWelcome(String to, String username) {
        sendMail(
                to,
                "Bienvenue sur ArenaMind",
                "Bonjour " + username + ",\n\n" +
                        "Votre compte a été créé avec succès.\n\n" +
                        "Bienvenue dans ArenaMind."
        );
    }
}