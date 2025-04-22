package com.msn.msncars.invitation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;

@Mapper
public interface InvitationMapper {
    InvitationMapper INSTANCE = Mappers.getMapper(InvitationMapper.class);

    @Mapping(source = "invitation.senderCompany.id", target = "senderCompanyId")
    @Mapping(target = "creationDate", expression = "java(mapCreationDate(invitation, userTimeZone))")
    @Mapping(source = "invitation.id", target = "id")
    InvitationDTO toDTO(Invitation invitation, ZoneId userTimeZone);

    default String mapCreationDate(Invitation invitation, ZoneId userTimeZone) {
        return invitation.getFormattedDateForUser(userTimeZone);
    }
}
