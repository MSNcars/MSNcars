package com.msn.msncars.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<List<Invitation>> getInvitationsBySenderCompanyIdAndRecipientUserId(Long senderCompanyId, String recipientUserId);
    List<Invitation> getInvitationsByRecipientUserId(String id);
    List<Invitation> getInvitationsBySenderUserId(String id);
    List<Invitation> getInvitationsBySenderCompanyId(Long companyId);
}
