package com.msn.msncars.usercompany;

import com.msn.msncars.company.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public UserCompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    public List<CompanyDTO> getCompaniesUserBelongsTo(String userId) {
        List<Company> companies = companyRepository.findByMembersContaining(userId);
        return companies.stream()
                .map(companyMapper::toDTO)
                .toList();
    }

    public void cleanupCompaniesUserBelongsTo(String userId) {
        List<Company> companiesUserBelongsTo = companyRepository.findByMembersContaining(userId);
        for (var company: companiesUserBelongsTo) {
            if (company.hasOwner(userId)) {
                companyRepository.deleteById(company.getId());
            }
            else if (company.hasMember(userId)) {
                company.removeMember(userId);
                companyRepository.saveAndFlush(company);
            }
        }
    }
}
