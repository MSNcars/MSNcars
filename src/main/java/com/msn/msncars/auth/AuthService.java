package com.msn.msncars.auth;

import com.msn.msncars.auth.dto.CompanyRegistrationRequest;
import com.msn.msncars.auth.dto.CompanyRegistrationResponse;
import com.msn.msncars.auth.dto.UserRegistrationRequest;

public interface AuthService {
    String registerUserAndAssignRole(UserRegistrationRequest userRegistrationRequest, AccountRole accountRole);
    CompanyRegistrationResponse registerCompany(CompanyRegistrationRequest companyRegistrationRequest);
}
