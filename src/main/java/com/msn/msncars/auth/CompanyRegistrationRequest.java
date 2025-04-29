package com.msn.msncars.auth;

import com.msn.msncars.company.CompanyCreationRequest;

public record CompanyRegistrationRequest(UserRegistrationRequest userRegistrationRequest, CompanyCreationRequest companyCreationRequest) {
}
