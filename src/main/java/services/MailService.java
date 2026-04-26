package services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailService {

    private static final String SMTP_HOST = "sandbox.smtp.mailtrap.io";
    private static final String SMTP_PORT = "2525";
    private static final String USERNAME = "04c56976900bf5";
    private static final String PASSWORD = "8dab195bc5ea96";
    private static final String FROM_EMAIL = "no-reply@arenamind.com";

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
            System.out.println("Email envoyé vers Mailtrap : " + to);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResetCode(String to, String code) {
        sendMail(
                to,
                "Reset Password - ArenaMind",
                "Bonjour,\n\nVotre code de réinitialisation est : " + code +
                        "\n\nCe code expire dans 10 minutes.\n\nArenaMind"
        );
    }

    public void sendFailedAttempt(String to, int attempt) {
        sendMail(
                to,
                "Alerte sécurité - ArenaMind",
                "Bonjour,\n\nUne tentative de connexion échouée a été détectée sur votre compte.\n\n" +
                        "Tentative : " + attempt + "/3\n\nArenaMind"
        );
    }

    public void sendTemporaryLock(String to, String lockedUntil) {
        sendMail(
                to,
                "Compte temporairement bloqué - ArenaMind",
                "Bonjour,\n\nVotre compte est actuellement bloqué temporairement.\n\n" +
                        "Vous pouvez réessayer après : " + lockedUntil + "\n\nArenaMind"
        );
    }

    public void sendBlock5(String to) {
        sendMail(
                to,
                "Compte bloqué 5 minutes - ArenaMind",
                "Bonjour,\n\nVotre compte a été bloqué pendant 5 minutes après 3 tentatives échouées.\n\nArenaMind"
        );
    }

    public void sendBlock15(String to) {
        sendMail(
                to,
                "Compte bloqué 15 minutes - ArenaMind",
                "Bonjour,\n\nVotre compte a été bloqué pendant 15 minutes après une récidive de tentatives échouées.\n\nArenaMind"
        );
    }

    public void sendBlockPermanent(String to) {
        sendMail(
                to,
                "Compte bloqué définitivement - ArenaMind",
                "Bonjour,\n\nVotre compte a été bloqué définitivement suite à plusieurs tentatives suspectes.\n\n" +
                        "Veuillez contacter l'administrateur.\n\nArenaMind"
        );
    }

    public void sendPermanentBlock(String to) {
        sendBlockPermanent(to);
    }

    public void sendWelcome(String to, String username) {
        sendMail(
                to,
                "Bienvenue sur ArenaMind",
                "Bonjour " + username + ",\n\nVotre compte a été créé avec succès.\n\nArenaMind"
        );
    }
}