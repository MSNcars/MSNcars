package com.msn.msncars.listing;

import com.msn.msncars.car.ModelMapper;
import com.msn.msncars.company.CompanyMapper;
import com.msn.msncars.listing.DTO.ListingResponse;
import com.msn.msncars.listing.DTO.ListingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {CompanyMapper.class, ModelMapper.class}, componentModel = "spring")
public interface ListingMapper {
    ListingMapper INSTANCE = Mappers.getMapper(ListingMapper.class);

    Listing fromDTO(ListingRequest listingRequest);
    ListingResponse toDTO(Listing listing);
}
