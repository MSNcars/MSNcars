package com.msn.MSNcars.auth;

import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> registerUser(RegisterRequest registerRequest);
}
