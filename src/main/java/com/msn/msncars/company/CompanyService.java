package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    Company createCompany(CreateCompanyRequest createCompanyRequest, String ownerId);
    Optional<CompanyDTO> getCompanyInfo(Long companyId);
    List<UserDTO> getCompanyMembers(Long companyId);
    UserDTO getCompanyOwner(Long companyId);
    void deleteCompany(Long companyId, String userId);
}
