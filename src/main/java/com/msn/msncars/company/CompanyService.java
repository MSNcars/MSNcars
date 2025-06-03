package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    CompanyDTO createCompany(CompanyCreationRequest companyCreationRequest, String ownerId);
    Optional<Company> getCompany(Long companyId);
    CompanyDTO getCompanyInfo(Long companyId);
    List<UserDTO> getCompanyMembers(Long companyId);
    UserDTO getCompanyOwner(Long companyId);
    List<CompanyDTO> getCompaniesUserBelongsTo(String userId);
    void cleanupCompaniesOfRemovedUser(String userId);
    void deleteCompany(Long companyId, String userId);
}
