package com.msn.msncars.company.invitation;

public record InvitationSentByCompanyDTO(String id, String recipientUserId, String sentAt, InvitationState invitationState) {
}
