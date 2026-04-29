package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires de l'entité Player")
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Oke", "oke@arena.tn", "lol", "Diamond II", 53);
        player.setId(1);
    }

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    @Nested
    @DisplayName("Constructeurs")
    class Constructors {

        @Test
        @DisplayName("Constructeur vide initialise teamId à 0")
        void emptyConstructor_setsTeamIdToZero() {
            Player p = new Player();
            assertThat(p.getTeamId()).isEqualTo(0);
        }

        @Test
        @DisplayName("Constructeur 5 args remplit correctement les champs")
        void fiveArgsConstructor_fillsFields() {
            Player p = new Player("ProGamer", "pro@arena.tn", "lol", "Challenger", 100);

            assertThat(p.getUsername()).isEqualTo("ProGamer");
            assertThat(p.getEmail()).isEqualTo("pro@arena.tn");
            assertThat(p.getGame()).isEqualTo("lol");
            assertThat(p.getRank()).isEqualTo("Challenger");
            assertThat(p.getLeaguePoints()).isEqualTo(100);
            assertThat(p.getPassword()).isEqualTo("password123");
            assertThat(p.getTeamId()).isEqualTo(0);
        }

        @Test
        @DisplayName("Constructeur 7 args remplit aussi le password et le teamId")
        void sevenArgsConstructor_fillsAllFields() {
            Player p = new Player("Test", "test@test.com", "secret",
                    "valorant", "Immortal", 50, 2);

            assertThat(p.getPassword()).isEqualTo("secret");
            assertThat(p.getTeamId()).isEqualTo(2);
        }

        @Test
        @DisplayName("Stats par défaut sont à 50")
        void defaultStats_areFifty() {
            Player p = new Player();

            assertThat(p.getVision()).isEqualTo(50);
            assertThat(p.getShooting()).isEqualTo(50);
            assertThat(p.getReflex()).isEqualTo(50);
            assertThat(p.getTeamplay()).isEqualTo(50);
            assertThat(p.getCommunication()).isEqualTo(50);
        }
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    @Nested
    @DisplayName("Getters et Setters")
    class GettersSetters {

        @Test
        @DisplayName("Modification username")
        void setUsername_updatesValue() {
            player.setUsername("NewName");
            assertThat(player.getUsername()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("Modification stats")
        void setStats_updatesValues() {
            player.setVision(85);
            player.setShooting(92);
            player.setReflex(78);
            player.setTeamplay(80);
            player.setCommunication(88);

            assertThat(player.getVision()).isEqualTo(85);
            assertThat(player.getShooting()).isEqualTo(92);
            assertThat(player.getReflex()).isEqualTo(78);
            assertThat(player.getTeamplay()).isEqualTo(80);
            assertThat(player.getCommunication()).isEqualTo(88);
        }

        @Test
        @DisplayName("Stats winrate, KDA et MVP")
        void setPerformanceStats_updatesValues() {
            player.setWinrate(64.5);
            player.setKda(3.2);
            player.setMvpCount(5);

            assertThat(player.getWinrate()).isEqualTo(64.5);
            assertThat(player.getKda()).isEqualTo(3.2);
            assertThat(player.getMvpCount()).isEqualTo(5);
        }
    }

    // ============================================================
    // EMAIL CONFIRMATION
    // ============================================================

    @Nested
    @DisplayName("Confirmation email")
    class EmailConfirmation {

        @Test
        @DisplayName("Sans pending email, hasPendingEmailChange() = false")
        void noPendingEmail_returnsFalse() {
            assertThat(player.hasPendingEmailChange()).isFalse();
        }

        @Test
        @DisplayName("Avec pending email, hasPendingEmailChange() = true")
        void withPendingEmail_returnsTrue() {
            player.setPendingEmail("nouveau@arena.tn");
            assertThat(player.hasPendingEmailChange()).isTrue();
        }

        @Test
        @DisplayName("Pending email vide n'est pas considéré comme en attente")
        void blankPendingEmail_returnsFalse() {
            player.setPendingEmail("");
            assertThat(player.hasPendingEmailChange()).isFalse();
        }

        @Test
        @DisplayName("Pending email espaces seulement n'est pas en attente")
        void whitespacePendingEmail_returnsFalse() {
            player.setPendingEmail("   ");
            assertThat(player.hasPendingEmailChange()).isFalse();
        }

        @Test
        @DisplayName("Email vérifié par défaut est true")
        void emailVerified_defaultsToTrue() {
            Player p = new Player();
            assertThat(p.isEmailVerified()).isTrue();
        }
    }

    // ============================================================
    // DISCORD
    // ============================================================

    @Nested
    @DisplayName("Profil Discord")
    class DiscordProfile {

        @Test
        @DisplayName("Sans Discord, hasDiscordConnected() = false")
        void noDiscord_returnsFalse() {
            assertThat(player.hasDiscordConnected()).isFalse();
        }

        @Test
        @DisplayName("Avec username Discord, hasDiscordConnected() = true")
        void withDiscord_returnsTrue() {
            player.setDiscordUsername("Yassine_Dev");
            assertThat(player.hasDiscordConnected()).isTrue();
        }

        @Test
        @DisplayName("Username vide ne compte pas comme connecté")
        void blankDiscord_returnsFalse() {
            player.setDiscordUsername("");
            assertThat(player.hasDiscordConnected()).isFalse();
        }

        @Test
        @DisplayName("Statut Discord par défaut est OFFLINE")
        void defaultDiscordStatus_isOffline() {
            Player p = new Player();
            assertThat(p.getDiscordStatus()).isEqualTo("OFFLINE");
        }

        @Test
        @DisplayName("Tous les champs Discord se mettent à jour")
        void allDiscordFields_updateCorrectly() {
            player.setDiscordUsername("Yassine_Dev");
            player.setDiscordTag("1234");
            player.setDiscordAvatarUrl("https://avatar.png");
            player.setDiscordStatus("ONLINE");
            player.setDiscordServerInvite("https://discord.gg/test");

            assertThat(player.getDiscordUsername()).isEqualTo("Yassine_Dev");
            assertThat(player.getDiscordTag()).isEqualTo("1234");
            assertThat(player.getDiscordAvatarUrl()).isEqualTo("https://avatar.png");
            assertThat(player.getDiscordStatus()).isEqualTo("ONLINE");
            assertThat(player.getDiscordServerInvite()).isEqualTo("https://discord.gg/test");
        }
    }

    // ============================================================
    // CHAMPIONS FAVORIS (parsing CSV)
    // ============================================================

    @Nested
    @DisplayName("Champions favoris")
    class FavoriteChampions {

        @Test
        @DisplayName("Sans favoris, la liste est vide")
        void noFavorites_returnsEmptyList() {
            List<String> favs = player.getFavoriteChampionsList();
            assertThat(favs).isEmpty();
        }

        @Test
        @DisplayName("Favoris null retourne liste vide (pas null)")
        void nullFavorites_returnsEmptyList() {
            player.setFavoriteChampions(null);
            List<String> favs = player.getFavoriteChampionsList();
            assertThat(favs).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Favoris vide retourne liste vide")
        void blankFavorites_returnsEmptyList() {
            player.setFavoriteChampions("");
            List<String> favs = player.getFavoriteChampionsList();
            assertThat(favs).isEmpty();
        }

        @Test
        @DisplayName("Un seul favori parsé correctement")
        void singleFavorite_parsedCorrectly() {
            player.setFavoriteChampions("Yasuo");
            List<String> favs = player.getFavoriteChampionsList();
            assertThat(favs).hasSize(1).containsExactly("Yasuo");
        }

        @Test
        @DisplayName("Trois favoris CSV parsés correctement")
        void threeFavorites_parsedCorrectly() {
            player.setFavoriteChampions("Yasuo,Ahri,Zed");
            List<String> favs = player.getFavoriteChampionsList();

            assertThat(favs)
                    .hasSize(3)
                    .containsExactly("Yasuo", "Ahri", "Zed");
        }

        @Test
        @DisplayName("Liste favoris est modifiable indépendamment du CSV")
        void favoritesList_isIndependent() {
            player.setFavoriteChampions("Yasuo,Ahri");
            List<String> favs = player.getFavoriteChampionsList();

            favs.add("Zed");
            // L'ajout dans la liste ne doit PAS modifier le CSV original
            List<String> favsAgain = player.getFavoriteChampionsList();
            assertThat(favsAgain).hasSize(2);
        }
    }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Nested
    @DisplayName("toString()")
    class ToString {

        @Test
        @DisplayName("Format : username — rank")
        void toString_returnsFormattedString() {
            assertThat(player.toString()).isEqualTo("Oke — Diamond II");
        }

        @Test
        @DisplayName("Sans rank, affiche Unranked")
        void toString_withoutRank_showsUnranked() {
            Player p = new Player();
            p.setUsername("Newbie");
            p.setRank(null);

            assertThat(p.toString()).isEqualTo("Newbie — Unranked");
        }
    }

    // ============================================================
    // TESTS PARAMÉTRÉS (plusieurs cas en une fois)
    // ============================================================

    @Nested
    @DisplayName("Tests paramétrés")
    class Parameterized {

        @ParameterizedTest(name = "League points = {0} doit être accepté")
        @ValueSource(ints = {0, 50, 100, 500, 1000, 9999})
        void leaguePoints_acceptsVariousValues(int lp) {
            player.setLeaguePoints(lp);
            assertThat(player.getLeaguePoints()).isEqualTo(lp);
        }

        @ParameterizedTest(name = "Stat = {0} doit être acceptée")
        @ValueSource(ints = {0, 25, 50, 75, 100})
        void stats_acceptValuesFrom0to100(int statValue) {
            player.setVision(statValue);
            player.setShooting(statValue);
            player.setReflex(statValue);
            player.setTeamplay(statValue);
            player.setCommunication(statValue);

            assertThat(player.getVision()).isEqualTo(statValue);
            assertThat(player.getShooting()).isEqualTo(statValue);
            assertThat(player.getReflex()).isEqualTo(statValue);
            assertThat(player.getTeamplay()).isEqualTo(statValue);
            assertThat(player.getCommunication()).isEqualTo(statValue);
        }

        @ParameterizedTest(name = "{0} (rank: {1}, lp: {2})")
        @CsvSource({
                "Oke,             Diamond II,     53",
                "ProGamer,        Challenger,     100",
                "BronzeWarrior,   Bronze I,       15",
                "SilverStar,      Silver II,      34"
        })
        void multiplePlayerProfiles(String username, String rank, int lp) {
            Player p = new Player(username, "test@arena.tn", "lol", rank, lp);

            assertThat(p.getUsername()).isEqualTo(username);
            assertThat(p.getRank()).isEqualTo(rank);
            assertThat(p.getLeaguePoints()).isEqualTo(lp);
        }
    }
}