package com.msn.msncars.auth;

import com.msn.msncars.auth.dto.CompanyRegistrationRequest;
import com.msn.msncars.auth.dto.CompanyRegistrationResponse;
import com.msn.msncars.auth.dto.UserRegistrationRequest;
import com.msn.msncars.auth.exception.RegistrationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with this username or email already exists",
                    content = @Content
            ),
    })
    @PostMapping("/auth/user/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
        logger.info("Received request to register a new user account.");

        String accountId = authService.registerUserAndAssignRole(userRegistrationRequest, AccountRole.USER);

        logger.info("User account has been successfully registered, accountId: {}", accountId);

        return ResponseEntity.status(HttpStatus.CREATED).body(accountId);
    }

    @Operation(summary = "Register a company account and create the company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company and account created successfully"),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with the given username or email, or a company with the provided name already exists.",
                    content = @Content
            ),
    })
    @PostMapping("/auth/company/register")
    public ResponseEntity<CompanyRegistrationResponse> registerCompany(@RequestBody CompanyRegistrationRequest companyRegistrationRequest) {
        logger.info("Received request to register a new company account.");

        CompanyRegistrationResponse companyRegistrationResponse = authService.registerCompany(companyRegistrationRequest);

        logger.info("Company has been successfully registered, userId: {}, companyId: {}",
            companyRegistrationResponse.userId(),
            companyRegistrationResponse.companyId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(companyRegistrationResponse);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<String> handleRegistrationException(RegistrationException re) {
        logger.warn("Registration exception occurred, reason: {}", re.getMessage());
        return ResponseEntity.status(re.getStatus()).body(re.getMessage());
    }
}
