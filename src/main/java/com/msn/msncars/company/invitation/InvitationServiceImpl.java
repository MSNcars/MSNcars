package com.msn.msncars.company.invitation;

import com.msn.msncars.auth.keycloak.KeycloakService;
import com.msn.msncars.company.Company;
import com.msn.msncars.company.CompanyService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(InvitationServiceImpl.class);

    public InvitationServiceImpl(InvitationRepository invitationRepository, CompanyService companyService, InvitationMapper invitationMapper, Clock clock, KeycloakService keycloakService) {
        this.invitationRepository = invitationRepository;
        this.companyService = companyService;
        this.invitationMapper = invitationMapper;
        this.clock = clock;
        this.keycloakService = keycloakService;
    }

    @Override
    public InvitationDTO invite(CreateInvitationRequest createInvitationRequest, String senderId) {
        logger.debug("Entering invite method for senderId: {}", senderId);
        Long senderCompanyId = createInvitationRequest.senderCompanyId();
        String recipientId = createInvitationRequest.recipientId();
        validateInvitationCreation(senderCompanyId, recipientId, senderId);

        logger.debug("Invitation creation validated for senderCompanyId: {}, recipientId: {}, senderId: {}",
            senderCompanyId,
            recipientId,
            senderId
        );

        clearCompanyInvitationsToRecipient(senderCompanyId, recipientId);

        logger.debug("Previous invitations for the recipient with id: {} cleared.", recipientId);

        Invitation invitation = new Invitation(
            recipientId,
            companyService.getCompany(senderCompanyId).get(),
            Instant.now(clock),
            InvitationState.PENDING,
            clock
        );

        logger.debug("Invitation created. Returning mapped invitation.");

        return mapInvitationToDTO(invitationRepository.save(invitation));
    }

    @Override
    public InvitationDTO acceptInvitation(UUID invitationId, String userId) {
        logger.debug("Entering acceptInvitation method for invitationId: {}, userId: {}", invitationId, userId);

        Invitation invitation = validateAndGetPendingInvitation(invitationId, userId);

        logPendingInvitationValidatedAndRetrieved();

        invitation.accept();

        logger.debug("Invitation with id: {} accepted. Returning mapped invitation.", invitationId);

        return mapInvitationToDTO(invitationRepository.save(invitation));
    }

    @Override
    public InvitationDTO declineInvitation(UUID invitationId, String userId) {
        logger.debug("Entering declineInvitation method for invitationId: {}, userId: {}", invitationId, userId);

        Invitation invitation = validateAndGetPendingInvitation(invitationId, userId);

        logPendingInvitationValidatedAndRetrieved();

        invitation.decline();

        logger.debug("Invitation with id: {} declined. Returning mapped invitation.", invitationId);

        return mapInvitationToDTO(invitationRepository.save(invitation));
    }

    @Override
    public void deleteInvitation(UUID invitationId, String userId) {
        logger.debug("Entering deleteInvitation method for invitationId: {}, userId: {}", invitationId, userId);

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation does not exist"));

        logger.debug("Invitation with id: {} fetched from database.", invitationId);

        if (!invitation.getSenderCompany().hasOwner(userId))
            throw new ForbiddenException("Only the invitation owner is allowed to delete it.");

        logger.debug("Invitation ownership validated with requesting user.");

        invitationRepository.deleteById(invitationId);

        logger.debug("Invitation with id: {} deleted.", invitationId);
    }

    @Override
    public List<InvitationDTO> getInvitationsReceivedByUser(String userId) {
        logger.debug("Entering getInvitationsReceivedByUser method for userId: {}", userId);

        List<Invitation> invitations = invitationRepository.getInvitationsByRecipientUserId(userId);

        logger.debug("Invitations ({}) for userId: {} fetched. Returning mapped invitations.", invitations.size(), userId);

        return mapInvitationsToDTOs(invitations);
    }

    @Override
    public List<InvitationDTO> getInvitationsSentByCompany(Long companyId, String userId) {
        logger.debug("Entering getInvitationsSentByCompany method for userId: {}, companyId: {}", userId, companyId);
        Optional<Company> companyOptional = companyService.getCompany(companyId);
        if (companyOptional.isEmpty())
            throw new NotFoundException("Company does not exist.");
        if (!companyOptional.get().hasMember(userId))
            throw new ForbiddenException("Only company members are allowed to view invitations sent by the company.");

        logger.debug("Company existence and invitation request validated.");

        List<Invitation> invitations = invitationRepository.getInvitationsBySenderCompanyId(companyId);

        logger.debug("Invitations ({}) fetched. Returning mapped invitations.", invitations.size());

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

    private void logPendingInvitationValidatedAndRetrieved() {
        logger.debug("Pending invitation validated and retrieved.");
    }
}
