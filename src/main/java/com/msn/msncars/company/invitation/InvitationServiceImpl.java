package com.msn.msncars.company.invitation;

import com.msn.msncars.auth.keycloak.KeycloakService;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyService;
import com.msn.msncars.user.UserService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final CompanyService companyService;
    private final InvitationMapper invitationMapper;
    private final Clock clock;
    private final KeycloakService keycloakService;

    public InvitationServiceImpl(InvitationRepository invitationRepository, CompanyService companyService, InvitationMapper invitationMapper, Clock clock, KeycloakService keycloakService) {
        this.invitationRepository = invitationRepository;
        this.companyService = companyService;
        this.invitationMapper = invitationMapper;
        this.clock = clock;
        this.keycloakService = keycloakService;
    }

    @Override
    public InvitationDTO invite(CreateInvitationRequest createInvitationRequest, String senderId) {
        Long senderCompanyId = createInvitationRequest.senderCompanyId();
        String recipientId = createInvitationRequest.recipientId();
        validateInvitationCreation(senderCompanyId, recipientId, senderId);
        clearCompanyInvitationsToRecipient(senderCompanyId, recipientId);
        Invitation invitation = new Invitation(
            recipientId,
            companyService.getCompany(senderCompanyId).get(),
            Instant.now(clock),
            InvitationState.PENDING,
            clock
        );
        return mapInvitationToDTO(invitationRepository.save(invitation));
    }

    @Override
    public InvitationDTO acceptInvitation(UUID invitationId, String userId) {
        Invitation invitation = validateAndGetPendingInvitation(invitationId, userId);
        invitation.accept();
        return mapInvitationToDTO(invitationRepository.save(invitation));
    }

    @Override
    public InvitationDTO declineInvitation(UUID invitationId, String userId) {
        Invitation invitation = validateAndGetPendingInvitation(invitationId, userId);
        invitation.decline();
        return mapInvitationToDTO(invitationRepository.save(invitation));
    }

    @Override
    public void deleteInvitation(UUID invitationId, String userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation does not exist"));
        if (!invitation.getSenderCompany().hasOwner(userId))
            throw new ForbiddenException("Only the invitation owner is allowed to delete it.");
        invitationRepository.deleteById(invitationId);
    }

    @Override
    public List<InvitationDTO> getInvitationsReceivedByUser(String userId) {
        List<Invitation> invitations = invitationRepository.getInvitationsByRecipientUserId(userId);
        return mapInvitationsToDTOs(invitations);
    }

    @Override
    public List<InvitationDTO> getInvitationsSentByCompany(Long companyId, String userId) {
        Optional<Company> companyOptional = companyService.getCompany(companyId);
        if (companyOptional.isEmpty())
            throw new NotFoundException("Company does not exist.");
        if (!companyOptional.get().hasMember(userId))
            throw new ForbiddenException("Only company members are allowed to view invitations sent by the company.");
        List<Invitation> invitations = invitationRepository.getInvitationsBySenderCompanyId(companyId);
        return mapInvitationsToDTOs(invitations);
    }

    private void validateInvitationCreation(Long senderCompanyId, String recipientId, String senderId) {
        Optional<Company> companyOptional = companyService.getCompany(senderCompanyId);
        if (keycloakService.getUserRepresentationById(recipientId).isEmpty())
            throw new NotFoundException("Invitation recipient does not exist.");
        else if (companyOptional.isEmpty())
            throw new NotFoundException("Sender company does not exist.");

        Company company = companyOptional.get();

        if (!company.hasOwner(senderId))
            throw new ForbiddenException("Only company owner is allowed to send invitations.");
        else if (company.hasMember(recipientId))
            throw new IllegalStateException("User already accepted invitation from this company.");
    }

    private void clearCompanyInvitationsToRecipient(Long senderCompanyId, String recipientId) {
        Optional<List<Invitation>> previousCompanyInvitations = invitationRepository.getInvitationsBySenderCompanyIdAndRecipientUserId(senderCompanyId, recipientId);
        if (previousCompanyInvitations.isEmpty())
            return;

        previousCompanyInvitations.get()
                .forEach(invitation -> invitationRepository.deleteById(invitation.getId()));
    }

    private Invitation validateAndGetPendingInvitation(UUID invitationId, String userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation does not exist."));
        if (invitation.getInvitationState() != InvitationState.PENDING)
            throw new IllegalStateException("Invitation was already " + invitation.getInvitationState().toString().toLowerCase());
        if (!invitation.getRecipientUserId().equals(userId))
            throw new ForbiddenException("You are not authorized to respond to this invitation. Only the recipient can perform this action.");

        return invitation;
    }

    private List<InvitationDTO> mapInvitationsToDTOs(List<Invitation> invitations) {
        return invitations.stream()
                .map(this::mapInvitationToDTO)
                .toList();
    }

    private InvitationDTO mapInvitationToDTO(Invitation invitation) {
        return invitationMapper.toDTO(invitation, clock.getZone());
    }
}
