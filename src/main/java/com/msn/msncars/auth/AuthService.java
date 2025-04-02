package com.msn.msncars.auth;

import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> registerUser(RegisterRequest registerRequest);
}
