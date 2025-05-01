package com.msn.msncars.invitation;

import java.util.List;
import java.util.UUID;

public interface InvitationService {
    InvitationDTO invite(CreateInvitationRequest createInvitationRequest, String senderId);
    InvitationDTO acceptInvitation(UUID invitationId, String userId);
    InvitationDTO declineInvitation(UUID invitationId, String userId);
    void deleteInvitation(UUID invitationId, String userId);
    List<InvitationDTO> getInvitationsReceivedByUser(String userId);
    List<InvitationDTO> getInvitationsSentByCompany(Long companyId, String userId);
}
