package com.msn.msncars.company.invitation;

public record InvitationDTO(String id, Long senderCompanyId, String creationDate, InvitationState invitationState) {
}
