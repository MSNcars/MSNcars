package com.msn.msncars.listing;

import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.DTO.ListingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(componentModel = "spring")
public interface ListingMapper {
    Listing fromDTO(ListingRequest listingRequest);
    @Mapping(target = "id", source = "listing.id")
    @Mapping(target = "createdAt", expression = "java(map(listing.getCreatedAt(), userTimeZone))")
    @Mapping(target = "expiresAt", expression = "java(map(listing.getExpiresAt(), userTimeZone))")
    ListingResponse toDTO(Listing listing, ZoneId userTimeZone);

    default ZonedDateTime map(Instant instant, ZoneId userTimeZone) {
        return ZonedDateTime.ofInstant(instant, userTimeZone);
    }
}
