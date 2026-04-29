package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires de l'entité Invitation")
class InvitationTest {

    private Invitation invitation;

    @BeforeEach
    void setUp() {
        invitation = new Invitation();
        invitation.setId(1);
        invitation.setTeamId(1);
        invitation.setPlayerId(5);
        invitation.setSentBy(1);
        invitation.setStatus(Invitation.Status.PENDING);
        invitation.setMessage("Rejoins-nous !");
    }

    @Test
    @DisplayName("Statut initial est PENDING")
    void initialStatus_isPending() {
        assertThat(invitation.getStatus()).isEqualTo(Invitation.Status.PENDING);
    }

    @Test
    @DisplayName("Modification du statut")
    void setStatus_updatesValue() {
        invitation.setStatus(Invitation.Status.ACCEPTED);
        assertThat(invitation.getStatus()).isEqualTo(Invitation.Status.ACCEPTED);
    }

    @Test
    @DisplayName("Le message est correctement stocké")
    void message_isStored() {
        assertThat(invitation.getMessage()).isEqualTo("Rejoins-nous !");
    }

    @Test
    @DisplayName("Timestamps sont nullables au départ")
    void timestamps_areInitiallyNull() {
        assertThat(invitation.getSentAt()).isNull();
        assertThat(invitation.getRepliedAt()).isNull();
    }

    @Test
    @DisplayName("Timestamps peuvent être remplis")
    void timestamps_canBeSet() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        invitation.setSentAt(now);
        invitation.setRepliedAt(now);

        assertThat(invitation.getSentAt()).isEqualTo(now);
        assertThat(invitation.getRepliedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Champs joints (teamName, playerName, senderName)")
    void joinedFields_canBeSet() {
        invitation.setTeamName("Team Alpha");
        invitation.setPlayerName("DiamondElite1");
        invitation.setSenderName("Oke");

        assertThat(invitation.getTeamName()).isEqualTo("Team Alpha");
        assertThat(invitation.getPlayerName()).isEqualTo("DiamondElite1");
        assertThat(invitation.getSenderName()).isEqualTo("Oke");
    }

    @ParameterizedTest(name = "Statut {0} valide")
    @EnumSource(Invitation.Status.class)
    @DisplayName("Tous les statuts de l'enum sont acceptés")
    void allStatuses_areAccepted(Invitation.Status status) {
        invitation.setStatus(status);
        assertThat(invitation.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Enum Status contient PENDING, ACCEPTED, REFUSED")
    void statusEnum_hasThreeValues() {
        assertThat(Invitation.Status.values())
                .hasSize(3)
                .containsExactly(
                        Invitation.Status.PENDING,
                        Invitation.Status.ACCEPTED,
                        Invitation.Status.REFUSED
                );
    }
}