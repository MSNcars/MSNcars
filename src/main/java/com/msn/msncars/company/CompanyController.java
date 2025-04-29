package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/company")
    public ResponseEntity<Company> createCompany(@RequestBody CompanyCreationRequest companyCreationRequest, @AuthenticationPrincipal Jwt jwt) {
        var createdCompany = companyService.createCompany(companyCreationRequest, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<CompanyDTO> getCompanyInfo(@PathVariable Long companyId) {
        Optional<CompanyDTO> companyDTO = companyService.getCompanyInfo(companyId);
        return companyDTO.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/company/{companyId}/members")
    public ResponseEntity<List<UserDTO>> getCompanyMembers(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyMembers(companyId));
    }

    @GetMapping("/company/{companyId}/owner")
    public ResponseEntity<UserDTO> getCompanyOwner(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyOwner(companyId));
    }

    @DeleteMapping("/company/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long companyId, @AuthenticationPrincipal Jwt jwt) {
        companyService.deleteCompany(companyId, jwt.getSubject());
        return ResponseEntity.ok("Company successfully deleted");
    }
}
