package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;
import com.msn.msncars.user.UserMapper;
import com.msn.msncars.user.UserService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, UserService userService, UserMapper userMapper, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.userService = userService;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    public CompanyDTO createCompany(CompanyCreationRequest companyCreationRequest, String ownerId) {
        Company company = new Company(
            ownerId,
            companyCreationRequest.name(),
            companyCreationRequest.address(),
            companyCreationRequest.phone(),
            companyCreationRequest.email()
        );
        company.setUsersId(Set.of(ownerId));

        Company savedCompany = companyRepository.save(company);

        return companyMapper.toDTO(savedCompany);
    }

    public Optional<Company> getCompany(Long companyId) {
        return companyRepository.findById(companyId);
    }

    @Override
    public Optional<CompanyDTO> getCompanyInfo(Long companyId) {
        Optional<Company> companyOptional = companyRepository.findById(companyId);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            return Optional.of(companyMapper.toDTO(company));
        }
        return Optional.empty();
    }

    @Override
    public List<UserDTO> getCompanyMembers(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        List<UserRepresentation> userRepresentations = company.getUsersId()
                .stream()
                .map(userService::getUserRepresentationById)
                .map(userRepresentationOptional -> userRepresentationOptional.orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return userRepresentations.stream().map(userMapper::toDTO).toList();
    }

    @Override
    public UserDTO getCompanyOwner(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
        String ownerId = company.getOwnerId();

        UserRepresentation ownerRepresentation = userService.getUserRepresentationById(ownerId).orElseThrow(() -> new NotFoundException("Owner of the company not found"));
        return userMapper.toDTO(ownerRepresentation);
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
