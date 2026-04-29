package entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires de l'entité TeamRequest")
class TeamRequestTest {

    @Test
    @DisplayName("Constructeur vide met le statut à PENDING")
    void emptyConstructor_setsStatusToPending() {
        TeamRequest r = new TeamRequest();
        assertThat(r.getStatus()).isEqualTo(TeamRequest.Status.PENDING);
    }

    @Test
    @DisplayName("Constructeur 3 args remplit teamId, playerId, message")
    void threeArgsConstructor_fillsFields() {
        TeamRequest r = new TeamRequest(1, 5, "Bonjour, je veux rejoindre !");

        assertThat(r.getTeamId()).isEqualTo(1);
        assertThat(r.getPlayerId()).isEqualTo(5);
        assertThat(r.getMessage()).isEqualTo("Bonjour, je veux rejoindre !");
        assertThat(r.getStatus()).isEqualTo(TeamRequest.Status.PENDING);
    }

    @Test
    @DisplayName("Modification du statut vers ACCEPTED")
    void setStatus_toAccepted() {
        TeamRequest r = new TeamRequest();
        r.setStatus(TeamRequest.Status.ACCEPTED);
        assertThat(r.getStatus()).isEqualTo(TeamRequest.Status.ACCEPTED);
    }

    @Test
    @DisplayName("Modification du statut vers REFUSED")
    void setStatus_toRefused() {
        TeamRequest r = new TeamRequest();
        r.setStatus(TeamRequest.Status.REFUSED);
        assertThat(r.getStatus()).isEqualTo(TeamRequest.Status.REFUSED);
    }

    @Test
    @DisplayName("Champs joints — teamName, playerName, playerRank, playerLP")
    void joinedFields_canBeSet() {
        TeamRequest r = new TeamRequest();
        r.setTeamName("Team Alpha");
        r.setTeamGame("lol");
        r.setPlayerName("DiamondElite1");
        r.setPlayerRank("Diamond II");
        r.setPlayerLP(60);

        assertThat(r.getTeamName()).isEqualTo("Team Alpha");
        assertThat(r.getTeamGame()).isEqualTo("lol");
        assertThat(r.getPlayerName()).isEqualTo("DiamondElite1");
        assertThat(r.getPlayerRank()).isEqualTo("Diamond II");
        assertThat(r.getPlayerLP()).isEqualTo(60);
    }

    @Test
    @DisplayName("Enum Status contient PENDING, ACCEPTED, REFUSED")
    void statusEnum_hasThreeValues() {
        assertThat(TeamRequest.Status.values())
                .hasSize(3)
                .containsExactly(
                        TeamRequest.Status.PENDING,
                        TeamRequest.Status.ACCEPTED,
                        TeamRequest.Status.REFUSED
                );
    }
}