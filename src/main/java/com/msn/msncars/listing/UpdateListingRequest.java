package com.msn.msncars.listing;

import com.msn.msncars.car.CarOperationalStatus;
import com.msn.msncars.car.CarType;
import com.msn.msncars.car.CarUsage;
import com.msn.msncars.car.Fuel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateListingRequest(
        Long id,
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
