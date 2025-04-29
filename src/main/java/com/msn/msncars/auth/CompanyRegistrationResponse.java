package com.msn.msncars.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyRegistrationResponse {
    @JsonProperty
    String userId;
    @JsonProperty
    Long companyId;

    public CompanyRegistrationResponse(String userId, Long companyId) {
        this.userId = userId;
        this.companyId = companyId;
    }
}
