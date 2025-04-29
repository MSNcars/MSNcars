package com.msn.msncars.invitation;

public record InvitationDTO(String id, String senderUserId, Long senderCompanyId, String creationDate, InvitationState invitationState) {
}
