package com.msn.MSNcars.auth;

public record RegisterRequest(String username, String password, String email, String firstName, String lastName) { }
