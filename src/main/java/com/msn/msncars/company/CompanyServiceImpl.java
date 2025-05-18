package com.msn.msncars.company;

import com.msn.msncars.auth.keycloak.KeycloakService;
import com.msn.msncars.user.UserDTO;
import com.msn.msncars.user.UserMapper;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    private final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

    public CompanyServiceImpl(CompanyRepository companyRepository, KeycloakService keycloakService, UserMapper userMapper, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.keycloakService = keycloakService;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    public CompanyDTO createCompany(CompanyCreationRequest companyCreationRequest, String ownerId) {
        logger.debug("Entering createCompany with ownerId: {}", ownerId);

        Company company = new Company(
            ownerId,
            companyCreationRequest.name(),
            companyCreationRequest.address(),
            companyCreationRequest.phone(),
            companyCreationRequest.email()
        );

        logger.debug("Company created for ownerId: {}", ownerId);

        company.setMembers(Set.of(ownerId));

        logger.debug("Owner added as company member.");

        Company savedCompany = companyRepository.save(company);

        logger.debug("Company saved in database. Returning mapped company.");

        return companyMapper.toDTO(savedCompany);
    }

    public Optional<Company> getCompany(Long companyId) {
        logger.debug("Entering getCompany with companyId: {}", companyId);

        Optional<Company> companyOptional = companyRepository.findById(companyId);

        logger.debug("Company optional fetched from database for companyId: {}. Returning company optional.", companyId);

        return companyOptional;
    }

    @Override
    public Optional<CompanyDTO> getCompanyInfo(Long companyId) {
        logger.debug("Entering getCompanyInfo with companyId: {}", companyId);

        Optional<Company> companyOptional = companyRepository.findById(companyId);

        logger.debug("Company optional fetched from database.");

        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();

            logger.debug("Company with id {} exist. Returning mapped Company.", companyId);

            return Optional.of(companyMapper.toDTO(company));
        }

        logger.debug("Company with id {} does not exist. Returning empty optional.", companyId);

        return Optional.empty();
    }

    @Override
    public List<UserDTO> getCompanyMembers(Long companyId) {
        logger.debug("Entering getCompanyMembers with companyId: {}", companyId);

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));

        logger.debug("Company with id {} found in database. Getting company members.", companyId);

        List<UserRepresentation> userRepresentations = company.getMembers()
                .stream()
                .map(keycloakService::getUserRepresentationById)
                .map(userRepresentationOptional -> userRepresentationOptional.orElse(null))
                .filter(Objects::nonNull)
                .toList();

        logger.debug("Company members fetched from keycloak. Returning mapped company members.");

        return userRepresentations.stream().map(userMapper::toUserDTO).toList();
    }

    @Override
    public UserDTO getCompanyOwner(Long companyId) {
        logger.debug("Entering getCompanyOwner with companyId: {}", companyId);

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));

        logger.debug("Company fetched from database.");

        String ownerId = company.getOwnerId();

        UserRepresentation ownerRepresentation = keycloakService.getUserRepresentationById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner of the company not found"));

        logger.debug("Owner fetched from keycloak. Returning mapped owner.");

        return userMapper.toUserDTO(ownerRepresentation);
    }

    @Override
    public List<CompanyDTO> getCompaniesUserBelongsTo(String userId) {
        logger.debug("Entering getCompaniesUserBelongsTo with userId: {}", userId);

        if (keycloakService.getUserRepresentationById(userId).isEmpty())
            throw new NotFoundException("User does not exist.");

        logger.debug("User existence validated for user with id: {}. Fetching companies.", userId);

        List<Company> companies = companyRepository.findByMembersContaining(userId);

        logger.debug("List of companies fetched from database. Returning mapped companies.");

        return companies.stream()
                .map(companyMapper::toDTO)
                .toList();
    }

    @Override
    public void deleteCompany(Long companyId, String userId) {
        logger.debug("Entering deleteCompany with companyId: {}, userId: {}", companyId, userId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->  new NotFoundException("This company cannot be deleted because it does not exist"));

        logger.debug("Company found in database. Validating if requesting user is owner of the company.");

        String ownerId = company.getOwnerId();
        if (!ownerId.equals(userId))
            throw new ForbiddenException("You are not allowed to delete this company. Only owner can delete this company.");

        logger.debug("Requesting user ownership validated. Deleting company with id: {}", companyId);

        companyRepository.deleteById(companyId);

        logger.debug("Company with id {} deleted from database.", companyId);
    }

    @Override
    @Transactional
    public void cleanupCompaniesOfRemovedUser(String userId) {
        List<Company> userCompanies = companyRepository.findByMembersContaining(userId);
        for (var company: userCompanies) {
            if (company.hasOwner(userId)) {
                companyRepository.deleteById(company.getId());
            } else if (company.hasMember(userId)) {
                company.removeMember(userId);
                companyRepository.save(company);
            }
        }
    }
}
