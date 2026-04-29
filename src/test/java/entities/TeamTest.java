package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires de l'entité Team")
class TeamTest {

    private Team team;

    @BeforeEach
    void setUp() {
        team = new Team("Team Alpha", "lol", 5, 3, 2845, null, null);
        team.setId(1);
        team.setCaptainId(1);
    }

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    @Nested
    @DisplayName("Constructeurs")
    class Constructors {

        @Test
        @DisplayName("Constructeur vide initialise les valeurs par défaut")
        void emptyConstructor_setsDefaults() {
            Team t = new Team();

            assertThat(t.getMaxPlayers()).isEqualTo(5);
            assertThat(t.getCurrentPlayers()).isEqualTo(0);
            assertThat(t.getPowerScore()).isEqualTo(0);
        }

        @Test
        @DisplayName("Constructeur (name, game) : équipe par défaut à 0/5")
        void twoArgsConstructor_setsDefaults() {
            Team t = new Team("Team Beta", "valorant");

            assertThat(t.getName()).isEqualTo("Team Beta");
            assertThat(t.getGame()).isEqualTo("valorant");
            assertThat(t.getMaxPlayers()).isEqualTo(5);
            assertThat(t.getCurrentPlayers()).isEqualTo(0);
        }

        @Test
        @DisplayName("Constructeur 7 args remplit tous les champs")
        void sevenArgsConstructor_fillsAllFields() {
            Team t = new Team("Team Beta", "valorant", 6, 2, 1500, "logo.png", "banner.jpg");

            assertThat(t.getName()).isEqualTo("Team Beta");
            assertThat(t.getMaxPlayers()).isEqualTo(6);
            assertThat(t.getCurrentPlayers()).isEqualTo(2);
            assertThat(t.getPowerScore()).isEqualTo(1500);
            assertThat(t.getLogo()).isEqualTo("logo.png");
            assertThat(t.getBanner()).isEqualTo("banner.jpg");
        }
    }

    // ============================================================
    // RÈGLES MÉTIER
    // ============================================================

    @Nested
    @DisplayName("Règles métier")
    class BusinessRules {

        @Test
        @DisplayName("Équipe avec 3/5 joueurs n'est PAS pleine")
        void notFullTeam_isFullReturnsFalse() {
            assertThat(team.isFull()).isFalse();
        }

        @Test
        @DisplayName("Équipe avec 5/5 joueurs EST pleine")
        void fullTeam_isFullReturnsTrue() {
            team.setCurrentPlayers(5);
            assertThat(team.isFull()).isTrue();
        }

        @Test
        @DisplayName("Équipe avec 3/5 joueurs PEUT recruter")
        void notFullTeam_canRecruitReturnsTrue() {
            assertThat(team.canRecruit()).isTrue();
        }

        @Test
        @DisplayName("Équipe pleine NE peut PAS recruter")
        void fullTeam_canRecruitReturnsFalse() {
            team.setCurrentPlayers(5);
            assertThat(team.canRecruit()).isFalse();
        }

        @Test
        @DisplayName("isCaptain() retourne true pour le bon ID")
        void isCaptain_returnsTrueForCaptainId() {
            assertThat(team.isCaptain(1)).isTrue();
        }

        @Test
        @DisplayName("isCaptain() retourne false pour un autre ID")
        void isCaptain_returnsFalseForOtherId() {
            assertThat(team.isCaptain(99)).isFalse();
        }

        @Test
        @DisplayName("Équipe sans capitaine : isCaptain() = false pour tous")
        void noCaptain_isCaptainAlwaysFalse() {
            Team t = new Team("Test", "lol");
            assertThat(t.isCaptain(1)).isFalse();
            assertThat(t.isCaptain(99)).isFalse();
        }
    }

    // ============================================================
    // DISCORD WEBHOOK
    // ============================================================

    @Nested
    @DisplayName("Discord Webhook")
    class DiscordWebhook {

        @Test
        @DisplayName("Sans webhook configuré, getter retourne null")
        void noWebhook_getterReturnsNull() {
            assertThat(team.getDiscordWebhookUrl()).isNull();
        }

        @Test
        @DisplayName("Webhook configuré, getter retourne l'URL")
        void withWebhook_getterReturnsUrl() {
            String url = "https://discord.com/api/webhooks/12345/abc";
            team.setDiscordWebhookUrl(url);

            assertThat(team.getDiscordWebhookUrl()).isEqualTo(url);
        }
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    @Nested
    @DisplayName("Getters et Setters")
    class GettersSetters {

        @Test
        @DisplayName("Modification du nom")
        void setName_updatesValue() {
            team.setName("Team Alpha Pro");
            assertThat(team.getName()).isEqualTo("Team Alpha Pro");
        }

        @Test
        @DisplayName("Augmentation du power score")
        void setPowerScore_updatesValue() {
            team.setPowerScore(3500);
            assertThat(team.getPowerScore()).isEqualTo(3500);
        }

        @Test
        @DisplayName("Modification du capitaine")
        void setCaptainId_updatesValue() {
            team.setCaptainId(2);
            assertThat(team.getCaptainId()).isEqualTo(2);
            assertThat(team.isCaptain(2)).isTrue();
            assertThat(team.isCaptain(1)).isFalse();
        }
    }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Test
    @DisplayName("toString() — format : nom (jeu)")
    void toString_returnsFormattedString() {
        assertThat(team.toString()).isEqualTo("Team Alpha (lol)");
    }

    // ============================================================
    // PARAMÉTRÉS
    // ============================================================

    @ParameterizedTest(name = "Équipe {0}/{1} : isFull={2}, canRecruit={3}")
    @CsvSource({
            "0, 5, false, true",
            "1, 5, false, true",
            "4, 5, false, true",
            "5, 5, true,  false",
            "5, 6, false, true",
            "6, 6, true,  false",
            "0, 1, false, true",
            "1, 1, true,  false"
    })
    @DisplayName("Différentes capacités d'équipe")
    void teamCapacity_variousScenarios(int current, int max, boolean isFull, boolean canRecruit) {
        Team t = new Team();
        t.setCurrentPlayers(current);
        t.setMaxPlayers(max);

        assertThat(t.isFull()).isEqualTo(isFull);
        assertThat(t.canRecruit()).isEqualTo(canRecruit);
    }
}