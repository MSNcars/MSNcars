package com.msn.msncars.invitation;

public record InvitationDTO(String id, Long senderCompanyId, String creationDate, InvitationState invitationState) {
}
