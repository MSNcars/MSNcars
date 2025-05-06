package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;
import com.msn.msncars.company.CompanyDTO;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO getBasicUserInformation(String userId);
    List<AccountRole> getUserRoles(String userId);
    Optional<UserRepresentation> getUserRepresentationById(String userId);
    List<CompanyDTO> getCompaniesUserBelongsTo(String userId);
    void deleteUser(String userId);
}
