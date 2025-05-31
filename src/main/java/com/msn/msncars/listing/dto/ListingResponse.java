package com.msn.msncars.listing.dto;

import com.msn.msncars.car.*;
import com.msn.msncars.car.model.ModelDTO;
import com.msn.msncars.listing.OwnerType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record ListingResponse (
        Long id,
        String ownerId,
        OwnerType ownerType,
        ModelDTO model,
        List<Feature> features,
        ZonedDateTime createdAt,
        ZonedDateTime expiresAt,
        Boolean revoked,
        BigDecimal price,
        Integer productionYear,
        Integer mileage,
        Fuel fuel,
        CarUsage carUsage,
        CarOperationalStatus carOperationalStatus,
        CarType carType,
        String description
) {

}
