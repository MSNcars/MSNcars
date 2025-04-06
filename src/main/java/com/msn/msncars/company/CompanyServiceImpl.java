package com.msn.msncars.company;

import com.msn.msncars.keycloak.KeycloakConfig;
import com.msn.msncars.user.UserDTO;
import com.msn.msncars.user.UserMapper;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final Keycloak keycloakAPI;
    private final KeycloakConfig keycloakConfig;

    public CompanyServiceImpl(CompanyRepository companyRepository, Keycloak keycloakAPI, KeycloakConfig keycloakConfig) {
        this.companyRepository = companyRepository;
        this.keycloakAPI = keycloakAPI;
        this.keycloakConfig = keycloakConfig;
    }

    @Override
    public Company createCompany(CreateCompanyRequest createCompanyRequest, String ownerId) {
        Company company = new Company(
            ownerId,
            createCompanyRequest.name(),
            createCompanyRequest.address(),
            createCompanyRequest.phone(),
            createCompanyRequest.email()
        );

        return companyRepository.save(company);
    }

    @Override
    public Optional<CompanyDTO> getCompanyInfo(Long companyId) {
        Optional<Company> companyOptional = companyRepository.findById(companyId);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            return Optional.of(new CompanyDTO(company.getName(), company.getAddress(), company.getPhone(), company.getEmail()));
        }
        return Optional.empty();
    }

    @Override
    public List<UserDTO> getCompanyMembers(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        List<UserRepresentation> userRepresentations = company.getUsersId()
                .stream()
                .map(userId -> keycloakAPI.realm(keycloakConfig.getRealm()).users().get(userId).toRepresentation())
                .toList();

        return userRepresentations.stream().map(UserMapper::toDTO).toList();
    }

    @Override
    public UserDTO getCompanyOwner(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        String ownerId = company.getOwnerId();
        UserRepresentation ownerRepresentation = keycloakAPI.realm(keycloakConfig.getRealm())
                .users()
                .get(ownerId)
                .toRepresentation();
        return UserMapper.toDTO(ownerRepresentation);
    }

    @Override
    public void deleteCompany(Long companyId, String userId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->  new NotFoundException("This company cannot be deleted because it does not exist"));

        String ownerId = company.getOwnerId();
        if (!ownerId.equals(userId))
            throw new ForbiddenException("You are not allowed to delete this company. Only owner can delete this company.");

        companyRepository.deleteById(companyId);
    }
}
