package com.msn.msncars.auth;

public enum AccountRole {
    USER,
    ADMIN,
    COMPANY;

    public String getName() {
        return this.name().toLowerCase();
    }
}
