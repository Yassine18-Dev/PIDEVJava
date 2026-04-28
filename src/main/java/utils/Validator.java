package utils;

import java.util.regex.Pattern;

public class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private static final Pattern USERNAME_PATTERN  = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final Pattern TEAM_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 _-]{3,30}$");

    public static class ValidationResult {
        public final boolean ok;
        public final String  message;

        private ValidationResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        public static ValidationResult ok()                  { return new ValidationResult(true, null); }
        public static ValidationResult error(String message) { return new ValidationResult(false, message); }
    }

    // ============================================================
    // VALIDATIONS JOUEUR
    // ============================================================

    public static ValidationResult validateUsername(String username) {
        if (username == null || username.isBlank())
            return ValidationResult.error("⚠ Le pseudo ne peut pas être vide.");
        String trimmed = username.trim();
        if (trimmed.length() < 3)
            return ValidationResult.error("⚠ Le pseudo doit contenir au moins 3 caractères.");
        if (trimmed.length() > 20)
            return ValidationResult.error("⚠ Le pseudo ne peut pas dépasser 20 caractères.");
        if (!USERNAME_PATTERN.matcher(trimmed).matches())
            return ValidationResult.error("⚠ Le pseudo ne peut contenir que des lettres, chiffres, _ et -.");
        return ValidationResult.ok();
    }

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.isBlank())
            return ValidationResult.error("⚠ L'email ne peut pas être vide.");
        String trimmed = email.trim();
        if (trimmed.length() > 150)
            return ValidationResult.error("⚠ L'email est trop long (max 150 caractères).");
        if (!EMAIL_PATTERN.matcher(trimmed).matches())
            return ValidationResult.error("⚠ Format d'email invalide. Exemple : nom@domaine.com");
        return ValidationResult.ok();
    }

    // ============================================================
    // VALIDATIONS ÉQUIPE
    // ============================================================

    public static ValidationResult validateTeamName(String name) {
        if (name == null || name.isBlank())
            return ValidationResult.error("⚠ Le nom de l'équipe ne peut pas être vide.");
        String trimmed = name.trim();
        if (trimmed.length() < 3)
            return ValidationResult.error("⚠ Le nom doit contenir au moins 3 caractères.");
        if (trimmed.length() > 30)
            return ValidationResult.error("⚠ Le nom ne peut pas dépasser 30 caractères.");
        if (!TEAM_NAME_PATTERN.matcher(trimmed).matches())
            return ValidationResult.error("⚠ Le nom ne peut contenir que des lettres, chiffres, espaces, _ et -.");
        return ValidationResult.ok();
    }

    public static ValidationResult validateMaxPlayers(int max) {
        if (max < 2)  return ValidationResult.error("⚠ L'équipe doit avoir au moins 2 joueurs maximum.");
        if (max > 10) return ValidationResult.error("⚠ L'équipe ne peut pas dépasser 10 joueurs.");
        return ValidationResult.ok();
    }
}