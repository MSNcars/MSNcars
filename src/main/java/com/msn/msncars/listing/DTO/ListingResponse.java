package com.msn.msncars.listing.DTO;

import com.msn.msncars.car.*;
import com.msn.msncars.company.CompanyDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record ListingResponse (
        Long id,
        String ownerId,
        CompanyDTO sellingCompany,
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
