package com.msn.msncars.invitation;

import com.msn.msncars.auth.keycloak.KeycloakService;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyService;
import com.msn.msncars.company.invitation.*;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InvitationServiceTest {
    @Autowired
    private InvitationService invitationService;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private KeycloakService keycloakService;

    @MockitoBean
    private InvitationRepository invitationRepository;

    @MockitoBean
    Clock clock;

    Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T23:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        Mockito.when(clock.getZone()).thenReturn(fixedClock.getZone());
        Mockito.when(clock.instant()).thenReturn(fixedClock.instant());
    }

    @Test
    void invite_WhenInvitedUserDoesNotExist_ShouldThrowNotFoundException() {
        // given
        CreateInvitationRequest createInvitationRequest = new CreateInvitationRequest("1", 1L);
        Mockito.when(keycloakService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.empty());

        // when & then
        NotFoundException nfe = assertThrows(NotFoundException.class, () -> invitationService.invite(createInvitationRequest, "2"));
        assertTrue(nfe.getMessage().toLowerCase().contains("invitation"));
    }

    @Test
    void invite_WhenCompanyDoesNotExist_ShouldThrowNotFoundException() {
        // given
        CreateInvitationRequest createInvitationRequest = new CreateInvitationRequest("1", 1L);
        Mockito.when(keycloakService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(new UserRepresentation()));
        Mockito.when(companyService.getCompany(1L)).thenReturn(Optional.empty());

        // when & then
        NotFoundException nfe = assertThrows(NotFoundException.class, () -> invitationService.invite(createInvitationRequest, "2"));
        assertTrue(nfe.getMessage().toLowerCase().contains("company"));
    }

    @Test
    void invite_WhenUserHasNotAcceptedInvitation_ShouldRemovePreviousInvitation() {
        // given
        CreateInvitationRequest createInvitationRequest = new CreateInvitationRequest("1", 1L);
        String ownerId = "2";
        Company company = new Company();
        company.setOwnerId(ownerId);
        company.setMembers(Set.of(ownerId));
        Invitation savedInvitation = new Invitation();
        savedInvitation.setCreationDateTime(fixedClock.instant());
        Invitation invitation = new Invitation();
        invitation.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        invitation.setInvitationState(InvitationState.PENDING);
        invitation.setCreationDateTime(fixedClock.instant());

        Mockito.when(keycloakService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(new UserRepresentation()));
        Mockito.when(companyService.getCompany(1L)).thenReturn(Optional.of(company));
        Mockito.when(invitationRepository.getInvitationsBySenderCompanyIdAndRecipientUserId(Mockito.any(), Mockito.any())).thenReturn(Optional.of(List.of(invitation)));
        Mockito.when(invitationRepository.save(Mockito.any(Invitation.class))).thenReturn(savedInvitation);

        // when
        invitationService.invite(createInvitationRequest, ownerId);

        // then
        Mockito.verify(invitationRepository, Mockito.times(1)).deleteById(Mockito.any());
    }

    @Test
    void invite_WhenUserAlreadyAcceptedCompanyInvitation_ShouldThrowIllegalStateException() {
        // given
        CreateInvitationRequest createInvitationRequest = new CreateInvitationRequest("1", 1L);
        String ownerId = "2";
        Company company = new Company();
        company.setOwnerId(ownerId);
        company.setMembers(Set.of("1", ownerId));

        Mockito.when(keycloakService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(new UserRepresentation()));
        Mockito.when(companyService.getCompany(1L)).thenReturn(Optional.of(company));

        // when & then
        assertThrows(IllegalStateException.class, () -> invitationService.invite(createInvitationRequest, ownerId));
        Mockito.verify(invitationRepository, Mockito.times(0)).deleteById(Mockito.any());
        Mockito.verify(invitationRepository, Mockito.times(0)).save(Mockito.any(Invitation.class));
    }

    @Test
    void invite_WhenRequestedByNonOwner_ShouldThrowForbiddenException() {
        // given
        Company company = new Company();
        company.setOwnerId("3");
        company.setMembers(Set.of("2", "3"));
        CreateInvitationRequest createInvitationRequest = new CreateInvitationRequest("1", 1L);
        Mockito.when(keycloakService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(new UserRepresentation()));
        Mockito.when(companyService.getCompany(1L)).thenReturn(Optional.of(company));

        // when & then
        assertThrows(ForbiddenException.class, () -> invitationService.invite(createInvitationRequest, "4"));
    }

    @Test
    void invite_WhenAllDependenciesRespondCorrectly_ShouldSaveInvitation() {
        // given
        CreateInvitationRequest createInvitationRequest = new CreateInvitationRequest("1", 1L);
        String ownerId = "2";
        Company company = new Company();
        company.setOwnerId(ownerId);
        company.setMembers(Set.of(ownerId));
        Invitation invitation = new Invitation();
        invitation.setCreationDateTime(fixedClock.instant());
        Mockito.when(keycloakService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(new UserRepresentation()));
        Mockito.when(companyService.getCompany(1L)).thenReturn(Optional.of(company));
        Mockito.when(invitationRepository.save(Mockito.any(Invitation.class))).thenReturn(invitation);

        // when
        invitationService.invite(createInvitationRequest, ownerId);

        // then
        Mockito.verify(invitationRepository, Mockito.times(1)).save(Mockito.any(Invitation.class));
    }

    @Test
    void acceptInvitation_WhenInvitationDoesNotExist_ShouldThrowNotFoundException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> invitationService.acceptInvitation(invitationId, "1"));
    }

    @Test
    void acceptInvitation_WhenInvitationAlreadyAccepted_ShouldThrowIllegalStateException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation();
        invitation.setInvitationState(InvitationState.ACCEPTED);
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // when & then
        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invitationService.acceptInvitation(invitationId, "1"));
        assertTrue(ise.getMessage().toLowerCase().contains("accepted"));
    }

    @Test
    void acceptInvitation_WhenInvitationAlreadyRejected_ShouldThrowIllegalStateException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation();
        invitation.setInvitationState(InvitationState.DECLINED);
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // when & then
        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invitationService.acceptInvitation(invitationId, "1"));
        assertTrue(ise.getMessage().toLowerCase().contains("declined"));
    }

    @Test
    void acceptInvitation_WhenRequestedByNonRecipient_ShouldThrowForbiddenException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation();
        invitation.setInvitationState(InvitationState.PENDING);
        invitation.setRecipientUserId("1");
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // when & then
        assertThrows(ForbiddenException.class, () -> invitationService.acceptInvitation(invitationId, "2"));
    }

    @Test
    void acceptInvitation_WhenAllDependenciesRespondCorrectly_ShouldAddMemberToCompany() {
        // given
        String recipientUserId = "1";
        Company company = new Company();
        company.setId(1L);
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation(
            recipientUserId,
            company,
            fixedClock.instant(),
            InvitationState.PENDING,
            fixedClock
        );
        invitation.setInvitationState(InvitationState.PENDING);
        invitation.setRecipientUserId("1");
        invitation.setSenderCompany(company);
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        Mockito.when(invitationRepository.save(Mockito.any(Invitation.class))).thenReturn(invitation);
        Mockito.when(companyService.getCompany(Mockito.any())).thenReturn(Optional.of(company));

        // when
        invitationService.acceptInvitation(invitationId, recipientUserId);

        // then
        assertTrue(company.hasMember(recipientUserId));
        Mockito.verify(invitationRepository, Mockito.times(1)).save(Mockito.any(Invitation.class));
    }

    @Test
    void declineInvitation_WhenInvitationDoesNotExist_ShouldThrowNotFoundException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> invitationService.declineInvitation(invitationId, "1"));
    }

    @Test
    void declineInvitation_WhenInvitationAlreadyAccepted_ShouldThrowIllegalStateException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation();
        invitation.setInvitationState(InvitationState.ACCEPTED);
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // when & then
        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invitationService.declineInvitation(invitationId, "1"));
        assertTrue(ise.getMessage().toLowerCase().contains("accepted"));
    }

    @Test
    void declineInvitation_WhenInvitationAlreadyRejected_ShouldThrowIllegalStateException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation();
        invitation.setInvitationState(InvitationState.DECLINED);
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // when & then
        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invitationService.declineInvitation(invitationId, "1"));
        assertTrue(ise.getMessage().toLowerCase().contains("declined"));
    }

    @Test
    void declineInvitation_WhenRequestedByNonRecipient_ShouldThrowForbiddenException() {
        // given
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation();
        invitation.setInvitationState(InvitationState.PENDING);
        invitation.setRecipientUserId("1");
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // when & then
        assertThrows(ForbiddenException.class, () -> invitationService.declineInvitation(invitationId, "2"));
    }

    @Test
    void declineInvitation_WhenAllDependenciesRespondCorrectly_ShouldAddMemberToCompany() {
        // given
        String recipientUserId = "1";
        Company company = new Company();
        company.setId(1L);
        UUID invitationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Invitation invitation = new Invitation(
            recipientUserId,
            company,
            fixedClock.instant(),
            InvitationState.PENDING,
            fixedClock
        );
        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        Mockito.when(invitationRepository.save(Mockito.any(Invitation.class))).thenReturn(invitation);

        // when
        invitationService.declineInvitation(invitationId, recipientUserId);

        // then
        Mockito.verify(invitationRepository, Mockito.times(1)).save(Mockito.any(Invitation.class));
    }

    @Test
    void getInvitationsReceivedByUser_WhenNoInvitations_ShouldReturnEmptyList() {
        // given
        Mockito.when(invitationRepository.getInvitationsByRecipientUserId(Mockito.any())).thenReturn(List.of());

        // when
        List<InvitationReceivedByUserDTO> invitations = invitationService.getInvitationsReceivedByUser("1");

        // then
        assertTrue(invitations.isEmpty());
    }

    @Test
    void getInvitationsReceivedByUser_WhenInvitationsExist_ShouldReturnInvitationsDTOList() {
        // given
        Company company1 = new Company();
        Company company2 = new Company();
        company1.setId(1L);
        company2.setId(2L);
        List<Invitation> invitations = List.of(
            new Invitation("1", company1, Instant.now(fixedClock), InvitationState.ACCEPTED, fixedClock),
            new Invitation("1", company2, Instant.now(fixedClock), InvitationState.PENDING, fixedClock)
        );
        Mockito.when(invitationRepository.getInvitationsByRecipientUserId(Mockito.any())).thenReturn(invitations);

        // when
        List<InvitationReceivedByUserDTO> invitationsDTO = invitationService.getInvitationsReceivedByUser("1");

        // then
        assertEquals(2, invitationsDTO.size());
        assertEquals(invitations.getFirst().getInvitationState(), invitationsDTO.getFirst().invitationState());
        assertEquals(invitations.getLast().getSenderCompany().getId(), invitationsDTO.getLast().senderCompanyId());
        assertEquals(invitations.getLast().getFormattedDateForUser(fixedClock.getZone()), invitationsDTO.getLast().receivedAt());
    }

    @Test
    void getInvitationsSentByCompany_WhenCompanyDoesNotExist_ShouldThrowNotFoundException() {
        // given
        Mockito.when(companyService.getCompany(Mockito.any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> invitationService.getInvitationsSentByCompany(1L, "1"));
    }

    @Test
    void getInvitationsSentByCompany_WhenRequestedByNonMember_ShouldThrowForbiddenException() {
        // given
        Company company = new Company();
        company.setMembers(Set.of("1", "2"));
        Mockito.when(companyService.getCompany(Mockito.any())).thenReturn(Optional.of(company));

        // when & then
        assertThrows(ForbiddenException.class, () -> invitationService.getInvitationsSentByCompany(1L, "3"));
    }

    @Test
    void getInvitationsSentByCompany_WhenNoInvitations_ShouldReturnEmptyList() {
        // given
        Company company = new Company();
        company.setMembers(Set.of("1", "2", "3"));
        Mockito.when(companyService.getCompany(Mockito.any())).thenReturn(Optional.of(company));
        Mockito.when(invitationRepository.getInvitationsBySenderCompanyId(Mockito.any())).thenReturn(List.of());

        // when
        List<InvitationSentByCompanyDTO> invitationDTOS = invitationService.getInvitationsSentByCompany(1L, "1");

        // then
        assertTrue(invitationDTOS.isEmpty());
    }

    @Test
    void getInvitationsSentByCompany_WhenInvitationsExist_ShouldReturnInvitationsDTOList() {
        // given
        Company company = new Company();
        company.setId(1L);
        company.setMembers(Set.of("2", "4", "6"));
        List<Invitation> invitations = List.of(
                new Invitation("1", company, Instant.now(fixedClock), InvitationState.DECLINED, fixedClock),
                new Invitation("3", company, Instant.now(fixedClock), InvitationState.PENDING, fixedClock),
                new Invitation("5", company, Instant.now(fixedClock), InvitationState.ACCEPTED, fixedClock)
        );
        Mockito.when(companyService.getCompany(Mockito.any())).thenReturn(Optional.of(company));
        Mockito.when(invitationRepository.getInvitationsBySenderCompanyId(Mockito.any())).thenReturn(invitations);

        // when
        List<InvitationSentByCompanyDTO> invitationDTOS = invitationService.getInvitationsSentByCompany(1L, "6");

        // then
        assertEquals(3, invitationDTOS.size());
        assertEquals(invitations.getFirst().getInvitationState(), invitationDTOS.getFirst().invitationState());
        assertEquals("5", invitationDTOS.getLast().recipientUserId());
    }
}
