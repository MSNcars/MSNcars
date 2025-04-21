package com.msn.msncars.listing;

import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.DTO.ListingRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListingMapper {
    Listing fromDTO(ListingRequest listingRequest);
    ListingResponse toDTO(Listing listing);
}
