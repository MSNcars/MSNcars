package com.msn.msncars.company.invitation;

public record InvitationReceivedByUserDTO(String id, Long senderCompanyId, String receivedAt, InvitationState invitationState) {
}
