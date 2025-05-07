package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDTO> getCompanyInfo(@PathVariable Long companyId) {
        Optional<CompanyDTO> companyDTO = companyService.getCompanyInfo(companyId);
        return companyDTO.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{companyId}/members")
    public ResponseEntity<List<UserDTO>> getCompanyMembers(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyMembers(companyId));
    }

    @GetMapping("/{companyId}/owner")
    public ResponseEntity<UserDTO> getCompanyOwner(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getCompanyOwner(companyId));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<CompanyDTO>> getCompaniesUserBelongsTo(@PathVariable String userId) {
        return ResponseEntity.ok(companyService.getCompaniesUserBelongsTo(userId));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long companyId, @AuthenticationPrincipal Jwt jwt) {
        companyService.deleteCompany(companyId, jwt.getSubject());
        return ResponseEntity.ok("Company successfully deleted");
    }
}
