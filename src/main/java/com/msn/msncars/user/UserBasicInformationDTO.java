package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;

import java.util.List;

public record UserBasicInformationDTO(String id, String firstName, String lastName, String username, String email, List<AccountRole> accountRoles) {
}
