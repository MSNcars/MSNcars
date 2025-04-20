package com.msn.msncars.listing.DTO;

import com.msn.msncars.car.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ListingResponse (
        Long id,
        String ownerId,
        String sellingCompanyName,
        String modelName,
        List<Feature> features,
        LocalDate createdAt,
        LocalDate expiresAt,
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
