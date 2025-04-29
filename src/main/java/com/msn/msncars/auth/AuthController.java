package com.msn.msncars.auth;

import com.msn.msncars.auth.exception.RegistrationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/user/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        String accountId = authService.registerUserAndAssignRole(userRegistrationRequest, AccountRole.USER);
        return ResponseEntity.ok(accountId);
    }

    @PostMapping("/auth/company/register")
    public ResponseEntity<CompanyRegistrationResponse> registerCompany(@RequestBody CompanyRegistrationRequest companyRegistrationRequest) {
        CompanyRegistrationResponse companyRegistrationResponse = authService.registerCompany(companyRegistrationRequest);
        return ResponseEntity.ok(companyRegistrationResponse);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<String> handleRegistrationException(RegistrationException re) {
        return ResponseEntity.status(re.getStatus()).body(re.getMessage());
    }
}
