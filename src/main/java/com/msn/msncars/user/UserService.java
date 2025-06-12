package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;

import java.util.List;

public interface UserService {
    UserBasicInformationDTO getUserBasicInformation(String userId);
    List<AccountRole> getAccountRoles(String userId);
    void deleteUser(String userId);
    void blockUser(String userEmail);
}
