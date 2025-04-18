package com.msn.msncars.listing;

import com.msn.msncars.car.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//This request DTO can be edited according to needs!
public record ListingRequest(
        String ownerId,
        Long sellingCompanyId,
        Long makeId,
        Long modelId,
        List<Long> featuresIds,
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
