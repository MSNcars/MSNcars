package com.msn.msncars.company.invitation;

import java.util.List;
import java.util.UUID;

public interface InvitationService {
    InvitationSentByCompanyDTO invite(CreateInvitationRequest createInvitationRequest, String senderId);
    InvitationReceivedByUserDTO acceptInvitation(UUID invitationId, String userId);
    InvitationReceivedByUserDTO declineInvitation(UUID invitationId, String userId);
    void deleteInvitation(UUID invitationId, String userId);
    List<InvitationReceivedByUserDTO> getInvitationsReceivedByUser(String userId);
    List<InvitationSentByCompanyDTO> getInvitationsSentByCompany(Long companyId, String userId);
}
