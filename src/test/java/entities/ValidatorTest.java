package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import utils.Validator.ValidationResult;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires du Validator")
class ValidatorTest {

    // ============================================================
    // USERNAME
    // ============================================================

    @Nested
    @DisplayName("validateUsername()")
    class UsernameValidation {

        @Test
        @DisplayName("Username valide passe")
        void validUsername_passes() {
            ValidationResult r = Validator.validateUsername("Yassine_Dev");
            assertThat(r.ok).isTrue();
            assertThat(r.message).isNull();
        }

        @ParameterizedTest(name = "\"{0}\" est invalide")
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("Username vide ou espaces refusé")
        void emptyUsername_refused(String value) {
            ValidationResult r = Validator.validateUsername(value);
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("vide");
        }

        @Test
        @DisplayName("Username null refusé")
        void nullUsername_refused() {
            ValidationResult r = Validator.validateUsername(null);
            assertThat(r.ok).isFalse();
        }

        @Test
        @DisplayName("Username trop court refusé")
        void tooShortUsername_refused() {
            ValidationResult r = Validator.validateUsername("ab");
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("3 caractères");
        }

        @Test
        @DisplayName("Username trop long refusé")
        void tooLongUsername_refused() {
            ValidationResult r = Validator.validateUsername("a".repeat(21));
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("20 caractères");
        }

        @ParameterizedTest(name = "\"{0}\" contient des caractères interdits")
        @ValueSource(strings = {"hello@world", "user name", "test#1", "Yassine!", "él@"})
        @DisplayName("Username avec caractères spéciaux refusé")
        void specialChars_refused(String value) {
            ValidationResult r = Validator.validateUsername(value);
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("lettres, chiffres");
        }

        @ParameterizedTest(name = "\"{0}\" est valide")
        @ValueSource(strings = {"Oke", "ProGamer", "User_123", "test-user", "ABC", "abc-123_xyz"})
        @DisplayName("Usernames valides acceptés")
        void validUsernames_accepted(String value) {
            ValidationResult r = Validator.validateUsername(value);
            assertThat(r.ok).isTrue();
        }
    }

    // ============================================================
    // EMAIL
    // ============================================================

    @Nested
    @DisplayName("validateEmail()")
    class EmailValidation {

        @ParameterizedTest(name = "\"{0}\" est un email valide")
        @ValueSource(strings = {
                "test@arena.tn",
                "user.name@example.com",
                "user_name@example.co.uk",
                "user+tag@example.fr",
                "ABC123@test.io"
        })
        @DisplayName("Emails valides acceptés")
        void validEmails_accepted(String email) {
            ValidationResult r = Validator.validateEmail(email);
            assertThat(r.ok).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" n'est pas un email")
        @ValueSource(strings = {
                "notanemail",
                "missing@tld",
                "@nodomain.com",
                "two@@signs.com",
                "spaces in@email.com",
                "user@.com"
        })
        @DisplayName("Emails mal formés refusés")
        void invalidEmails_refused(String email) {
            ValidationResult r = Validator.validateEmail(email);
            assertThat(r.ok).isFalse();
        }

        @Test
        @DisplayName("Email vide refusé")
        void emptyEmail_refused() {
            ValidationResult r = Validator.validateEmail("");
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("vide");
        }

        @Test
        @DisplayName("Email null refusé")
        void nullEmail_refused() {
            ValidationResult r = Validator.validateEmail(null);
            assertThat(r.ok).isFalse();
        }

        @Test
        @DisplayName("Email trop long refusé (>150 chars)")
        void tooLongEmail_refused() {
            String longEmail = "a".repeat(140) + "@test.com";
            ValidationResult r = Validator.validateEmail(longEmail);
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("trop long");
        }
    }

    // ============================================================
    // TEAM NAME
    // ============================================================

    @Nested
    @DisplayName("validateTeamName()")
    class TeamNameValidation {

        @ParameterizedTest(name = "\"{0}\" est un nom valide")
        @ValueSource(strings = {"Team Alpha", "ABC", "My Team_123", "Team-Pro 2024"})
        @DisplayName("Noms valides acceptés")
        void validNames_accepted(String name) {
            ValidationResult r = Validator.validateTeamName(name);
            assertThat(r.ok).isTrue();
        }

        @Test
        @DisplayName("Nom vide refusé")
        void emptyName_refused() {
            ValidationResult r = Validator.validateTeamName("");
            assertThat(r.ok).isFalse();
        }

        @Test
        @DisplayName("Nom trop court refusé")
        void tooShortName_refused() {
            ValidationResult r = Validator.validateTeamName("AB");
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("3 caractères");
        }

        @Test
        @DisplayName("Nom trop long refusé")
        void tooLongName_refused() {
            ValidationResult r = Validator.validateTeamName("A".repeat(31));
            assertThat(r.ok).isFalse();
        }

        @ParameterizedTest(name = "\"{0}\" contient caractères spéciaux")
        @ValueSource(strings = {"Team@Pro", "Team#1", "Team!", "Team(Alpha)"})
        @DisplayName("Noms avec caractères spéciaux refusés")
        void specialChars_refused(String name) {
            ValidationResult r = Validator.validateTeamName(name);
            assertThat(r.ok).isFalse();
        }
    }

    // ============================================================
    // MAX PLAYERS
    // ============================================================

    @Nested
    @DisplayName("validateMaxPlayers()")
    class MaxPlayersValidation {

        @ParameterizedTest(name = "max = {0} valide")
        @ValueSource(ints = {2, 3, 5, 7, 10})
        @DisplayName("Valeurs entre 2 et 10 acceptées")
        void validValues_accepted(int max) {
            ValidationResult r = Validator.validateMaxPlayers(max);
            assertThat(r.ok).isTrue();
        }

        @ParameterizedTest(name = "max = {0} trop bas")
        @ValueSource(ints = {0, 1, -5})
        @DisplayName("Valeurs < 2 refusées")
        void tooLowValues_refused(int max) {
            ValidationResult r = Validator.validateMaxPlayers(max);
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("au moins 2");
        }

        @ParameterizedTest(name = "max = {0} trop haut")
        @ValueSource(ints = {11, 50, 100})
        @DisplayName("Valeurs > 10 refusées")
        void tooHighValues_refused(int max) {
            ValidationResult r = Validator.validateMaxPlayers(max);
            assertThat(r.ok).isFalse();
            assertThat(r.message).contains("10");
        }
    }

    // ============================================================
    // VALIDATIONRESULT
    // ============================================================

    @Nested
    @DisplayName("ValidationResult")
    class ValidationResultTest {

        @Test
        @DisplayName("ok() crée un résultat valide sans message")
        void ok_createsValidResult() {
            ValidationResult r = ValidationResult.ok();
            assertThat(r.ok).isTrue();
            assertThat(r.message).isNull();
        }

        @Test
        @DisplayName("error() crée un résultat invalide avec message")
        void error_createsInvalidResult() {
            ValidationResult r = ValidationResult.error("Erreur test");
            assertThat(r.ok).isFalse();
            assertThat(r.message).isEqualTo("Erreur test");
        }
    }
}