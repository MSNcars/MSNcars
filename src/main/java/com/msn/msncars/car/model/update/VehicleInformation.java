package com.msn.msncars.car.model.update;

import com.fasterxml.jackson.annotation.JsonProperty;
public record VehicleInformation(
        @JsonProperty("Make_ID") Long makeID,
        @JsonProperty("Make_Name") String makeName,
        @JsonProperty("Model_ID") Long modelID,
        @JsonProperty("Model_Name") String modelName
) {
}
