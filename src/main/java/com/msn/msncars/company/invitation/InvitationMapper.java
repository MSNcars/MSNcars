package com.msn.msncars.company.invitation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface InvitationMapper {
    @Mapping(target = "receivedAt", expression = "java(mapCreationDate(invitation, userTimeZone))")
    @Mapping(source = "invitation.senderCompany.id", target = "senderCompanyId")
    @Mapping(source = "invitation.id", target = "id")
    InvitationReceivedByUserDTO toInvitationReceivedByUserDTO(Invitation invitation, ZoneId userTimeZone);

    @Mapping(target = "sentAt", expression = "java(mapCreationDate(invitation, userTimeZone))")
    @Mapping(source = "invitation.id", target = "id")
    InvitationSentByCompanyDTO toInvitationSentByCompanyDTO(Invitation invitation, ZoneId userTimeZone);

    default String mapCreationDate(Invitation invitation, ZoneId userTimeZone) {
        return invitation.getFormattedDateForUser(userTimeZone);
    }
}
