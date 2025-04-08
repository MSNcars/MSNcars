package com.msn.msncars.listing;

import com.msn.msncars.car.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//This response DTO can be edited according to needs!
public record ListingResponse (
        Long id,
        String ownerId,
        String sellingCompanyName,
        String makeName,
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
