package com.msn.msncars.auth;

public interface AuthService {
    String registerUserAndAssignRole(UserRegistrationRequest userRegistrationRequest, AccountRole accountRole);
    CompanyRegistrationResponse registerCompany(CompanyRegistrationRequest companyRegistrationRequest);
}
