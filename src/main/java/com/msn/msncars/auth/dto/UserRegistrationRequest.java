package com.msn.msncars.auth.dto;

public record UserRegistrationRequest(String username, String password, String email, String firstName, String lastName) { }
