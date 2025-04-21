package com.msn.msncars.car.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AllModelsApiResponse(
        @JsonProperty("Count") Long count,
        @JsonProperty("Message") String message,
        @JsonProperty("SearchCriteria") String searchCriteria,
        @JsonProperty("Results") List<VehicleInformation> models
) {
}
