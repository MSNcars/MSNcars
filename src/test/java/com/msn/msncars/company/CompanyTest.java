package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;
import com.msn.msncars.user.UserService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompanyTest {
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyService companyService;

    @Test
    void createCompany_WhenRequestCorrect_ShouldCallSaveExactlyOnce() {
        // given
        CompanyCreationRequest companyCreationRequest = new CompanyCreationRequest(
                "companyName",
                "companyAddress",
                "companyPhone",
                "companyEmail"
        );

        // when
        companyService.createCompany(companyCreationRequest, "1");

        // then
        Mockito.verify(companyRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void getCompanyInfo_WhenCompanyFound_ShouldReturnCompanyDTO() {
        // given
        Company company = new Company(
            "1",
            "companyName",
            "companyAddress",
            "companyPhone",
            "companyEmail"
        );
        company.setId(1L);
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));

        // when
        Optional<CompanyDTO> optionalCompanyDTO = companyService.getCompanyInfo(1L);

        // then
        assertTrue(optionalCompanyDTO.isPresent());
        CompanyDTO companyDTO = optionalCompanyDTO.get();
        assertEquals(company.getName(), companyDTO.name());
        assertEquals(company.getAddress(), companyDTO.address());
        assertEquals(company.getPhone(), companyDTO.phone());
        assertEquals(company.getEmail(), companyDTO.email());
    }

    @Test
    void getCompanyInfo_WhenCompanyNotFound_ShouldReturnEmptyOptional() {
        // given
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when
        Optional<CompanyDTO> optionalCompanyDTO = companyService.getCompanyInfo(1L);

        // then
        assertTrue(optionalCompanyDTO.isEmpty());
    }

    @Test
    void getCompanyMembers_WhenCompanyMembersFound_ShouldReturnListOfUsersDTOs() {
        // given
        Set<String> usersId = Set.of("2", "3", "4");
        Company company = new Company();
        company.setUsersId(usersId);
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));
        Mockito.when(userService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(new UserRepresentation()));

        // when
        List<UserDTO> userDTOs = companyService.getCompanyMembers(1L);

        // then
        assertEquals(usersId.size(), userDTOs.size());
    }

    @Test
    void getCompanyMembers_WhenCompanyDoesNotHaveMembers_ShouldReturnEmptyList() {
        // given
        Company company = new Company();
        company.setUsersId(Collections.emptySet());
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));

        // when
        List<UserDTO> userDTOs = companyService.getCompanyMembers(1L);

        // then
        assertTrue(userDTOs.isEmpty());
    }

    @Test
    void getCompanyMembers_WhenCompanyDoesNotExist_ShouldThrowNotFoundException() {
        // given
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> companyService.getCompanyMembers(1L));
    }

    @Test
    void getCompanyOwner_WhenOwnerFound_ShouldReturnUserDTO() {
        // given
        Company company = new Company();
        UserRepresentation ownerRepresentation = new UserRepresentation();
        ownerRepresentation.setId("1");
        ownerRepresentation.setUsername("ownerName");
        ownerRepresentation.setFirstName("ownerFirstName");
        ownerRepresentation.setLastName("ownerLastName");
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));
        Mockito.when(userService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.of(ownerRepresentation));

        // when
        UserDTO userDTO = companyService.getCompanyOwner(1L);

        // then
        assertNotNull(userDTO);
        assertEquals(ownerRepresentation.getId(), userDTO.id());
        assertEquals(ownerRepresentation.getUsername(), userDTO.username());
        assertEquals(ownerRepresentation.getFirstName(), userDTO.firstName());
        assertEquals(ownerRepresentation.getLastName(), userDTO.lastName());
    }

    @Test
    void getCompanyOwner_WhenOwnerNotFound_ShouldThrowNotFoundException() {
        // given
        Company company = new Company();
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));
        Mockito.when(userService.getUserRepresentationById(Mockito.any())).thenReturn(Optional.empty());

        // when
        assertThrows(NotFoundException.class, () -> companyService.getCompanyOwner(1L));
    }

    @Test
    void getCompanyOwner_WhenCompanyDoesNotExist_ShouldThrowNotFoundException() {
        // given
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> companyService.getCompanyOwner(1L));
    }

    @Test
    void deleteCompany_WhenCompanyDoesNotExist_ShouldThrowNotFoundException() {
        // given
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> companyService.deleteCompany(1L, "1"));
    }

    @Test
    void deleteCompany_WhenCalledByNotOwner_ShouldThrowForbiddenException() {
        // given
        Company company = new Company();
        company.setOwnerId("1");
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));

        // when & then
        assertThrows(ForbiddenException.class, () -> companyService.deleteCompany(1L, "2"));
    }

    @Test
    void deleteCompany_WhenOwnerDeletesExistingCompany_ShouldDeleteCompany() {
        // given
        Company company = new Company();
        company.setOwnerId("1");
        Mockito.when(companyRepository.findById(Mockito.any())).thenReturn(Optional.of(company));

        // when
        companyService.deleteCompany(1L, "1");

        // then
        Mockito.verify(companyRepository, Mockito.times(1)).deleteById(1L);
        Mockito.verify(companyRepository, Mockito.times(1)).deleteById(Mockito.any());
    }
}
