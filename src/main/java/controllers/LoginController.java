package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import services.MailService;
import services.SecurityService;
import services.UserService;
import services.GoogleAuthService;
import services.CaptchaService;
import services.CaptchaLocalServer;
import utils.SessionManager;

public class LoginController {

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private WebView captchaWebView;

    private String captchaToken;

    private final UserService userService = new UserService();
    private final SecurityService sec = new SecurityService();
    private final MailService mail = new MailService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final CaptchaService captchaService = new CaptchaService();

    @FXML
    public void initialize() {
        try {
            if (captchaWebView != null) {

                CaptchaLocalServer.start();

                captchaWebView.getEngine().load("http://localhost:9999/recaptcha.html");

                captchaWebView.getEngine().titleProperty().addListener((obs, oldTitle, newTitle) -> {
                    if (newTitle != null && newTitle.startsWith("TOKEN:")) {
                        captchaToken = newTitle.substring("TOKEN:".length());
                        System.out.println("Captcha token reçu");
                    }

                    if ("TOKEN_EXPIRED".equals(newTitle)) {
                        captchaToken = null;
                        System.out.println("Captcha expiré");
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void login() {
        try {
            String email = tfEmail.getText().trim();
            String pass = pfPassword.getText().trim();

            if (email.isEmpty() && pass.isEmpty()) {
                showAlert("Erreur", "Email et mot de passe obligatoires", Alert.AlertType.ERROR);
                return;
            }

            if (email.isEmpty()) {
                showAlert("Erreur", "Email obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (!email.contains("@")) {
                showAlert("Erreur", "Format email invalide", Alert.AlertType.ERROR);
                return;
            }

            if (pass.isEmpty()) {
                showAlert("Erreur", "Mot de passe obligatoire", Alert.AlertType.ERROR);
                return;
            }

            if (captchaToken == null || captchaToken.isEmpty()) {
                showAlert("Erreur", "Veuillez valider le reCAPTCHA", Alert.AlertType.ERROR);
                return;
            }

            if (!captchaService.verifyCaptcha(captchaToken)) {
                showAlert("Erreur", "reCAPTCHA invalide ou expiré", Alert.AlertType.ERROR);
                resetCaptcha();
                return;
            }

            if (!userService.emailExiste(email)) {
                showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
                resetCaptcha();
                return;
            }

            if (sec.isBlocked(email)) {
                mail.sendPermanentSupportMail(email);

                showAlert(
                        "Compte bloqué",
                        "Votre compte est bloqué définitivement.\nUn email de sécurité a été envoyé avec les instructions de support.",
                        Alert.AlertType.ERROR
                );

                resetCaptcha();
                return;
            }

            if (sec.isLocked(email)) {
                String lockedUntil = sec.getLockedUntil(email);
                mail.sendTemporaryLock(email, lockedUntil);

                showAlert(
                        "Compte temporairement bloqué",
                        "Votre compte est bloqué temporairement.\nRéessayez après : " + lockedUntil,
                        Alert.AlertType.ERROR
                );

                resetCaptcha();
                return;
            }

            User user = userService.loginUser(email, pass);

            if (user != null) {
                sec.reset(email);
                SessionManager.setUser(user);

                showAlert("Succès", "Bienvenue " + user.getUsername(), Alert.AlertType.INFORMATION);
                openDashboard(user);

            } else {
                int r = sec.fail(email);

                if (r == 5) {
                    mail.sendAccountBlockedSupportMail(email, 5);

                    showAlert(
                            "Compte bloqué",
                            "3 tentatives échouées.\nVotre compte est bloqué pendant 5 minutes.\nUn email de sécurité a été envoyé.",
                            Alert.AlertType.ERROR
                    );

                } else if (r == 15) {
                    mail.sendAccountBlockedSupportMail(email, 15);

                    showAlert(
                            "Compte bloqué",
                            "Nouvelle récidive.\nVotre compte est bloqué pendant 15 minutes.\nUn email de sécurité a été envoyé.",
                            Alert.AlertType.ERROR
                    );

                } else if (r == -1) {
                    mail.sendPermanentSupportMail(email);

                    showAlert(
                            "Compte bloqué",
                            "Compte bloqué définitivement.\nUn email de sécurité a été envoyé avec les instructions de support.",
                            Alert.AlertType.ERROR
                    );

                } else {
                    mail.sendFailedAttempt(email, r);

                    showAlert(
                            "Erreur",
                            "Email ou mot de passe incorrect.\nTentative " + r + "/3",
                            Alert.AlertType.ERROR
                    );
                }

                resetCaptcha();
            }

        } catch (Exception e) {
            showAlert("Erreur système", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            resetCaptcha();
        }
    }

    @FXML
    public void loginWithGoogle() {
        try {
            User user = googleAuthService.loginWithGoogle();

            if (user == null) {
                showAlert("Erreur", "Connexion Google annulée ou échouée", Alert.AlertType.ERROR);
                return;
            }

            if ("BANNED".equalsIgnoreCase(user.getStatus())) {
                showAlert("Compte bloqué", "Votre compte est banni.", Alert.AlertType.ERROR);
                return;
            }

            SessionManager.setUser(user);

            showAlert("Succès", "Bienvenue " + user.getUsername(), Alert.AlertType.INFORMATION);
            openDashboard(user);

        } catch (Exception e) {
            showAlert("Erreur Google", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void goToSignup() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/signup.fxml"));

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la page d'inscription", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void goToForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/forgot_password.fxml"));

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réinitialisation du mot de passe");
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la page de réinitialisation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void openDashboard(User user) throws Exception {
        String role = user.getRoleType();

        String fxmlPath;
        String title;

        if ("ADMIN".equalsIgnoreCase(role)) {
            fxmlPath = "/user.fxml";
            title = "Dashboard Admin";
        } else {
            fxmlPath = "/player_home.fxml";
            title = "ArenaMind - Player Home";
        }

        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

        Stage stage = (Stage) tfEmail.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    private void resetCaptcha() {
        captchaToken = null;

        if (captchaWebView != null) {
            captchaWebView.getEngine().reload();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}