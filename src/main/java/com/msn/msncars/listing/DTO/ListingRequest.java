package com.msn.msncars.listing.DTO;

import com.msn.msncars.car.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//This request DTO can be edited according to needs!
public record ListingRequest(
        @NotEmpty
        String ownerId,
        Long sellingCompanyId,
        @NotNull
        Long makeId,
        @NotNull
        Long modelId,
        List<Long> featuresIds,
        LocalDate expiresAt,
        Boolean revoked,
        @Min(value = 0)
        BigDecimal price,
        @Min(value = 1900)
        Integer productionYear,
        @Min(value = 0)
        Integer mileage,
        Fuel fuel,
        CarUsage carUsage,
        CarOperationalStatus carOperationalStatus,
        CarType carType,
        @Size(max = 500)
        String description
) {
}
