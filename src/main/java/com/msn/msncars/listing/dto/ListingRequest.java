package com.msn.msncars.listing.dto;

import com.msn.msncars.car.CarOperationalStatus;
import com.msn.msncars.car.CarType;
import com.msn.msncars.car.CarUsage;
import com.msn.msncars.car.Fuel;
import com.msn.msncars.listing.OwnerType;
import com.msn.msncars.listing.ValidityPeriod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ListingRequest(
        String ownerId,
        @NotNull
        OwnerType ownerType,
        @NotNull
        Long modelId,
        List<Long> featuresIds,
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
        String description,
        ValidityPeriod validityPeriod
) {
}
