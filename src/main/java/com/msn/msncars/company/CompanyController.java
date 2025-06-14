package com.msn.msncars.company;

import com.msn.msncars.user.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;
    private final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "Get information about company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company information fetched successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cannot fetch company information: company not found.",
                    content = @Content
            ),
    })
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDTO> getCompanyInfo(@PathVariable Long companyId) {
        logger.info("Received request to get company info for company with id {}", companyId);

        CompanyDTO companyDTO = companyService.getCompanyInfo(companyId);

        logger.info("Returning company info for company with id {}", companyId);

        return ResponseEntity.ok(companyDTO);
    }

    @Operation(summary = "Get company members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company members found successfully"),
            @ApiResponse(responseCode = "404", description = "Company does not exist", content = @Content),
    })
    @GetMapping("/{companyId}/members")
    public ResponseEntity<List<UserDTO>> getCompanyMembers(@PathVariable Long companyId) {
        logger.info("Received request to get company members for company with id {}", companyId);

        List<UserDTO> companyMembers = companyService.getCompanyMembers(companyId);

        logger.info("Returning company members for company with id {}", companyId);

        return ResponseEntity.ok(companyMembers);
    }

    @Operation(summary = "Get company owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company owner found successfully"),
            @ApiResponse(responseCode = "404", description = "Company or owner not found", content = @Content)
    })
    @GetMapping("/{companyId}/owner")
    public ResponseEntity<UserDTO> getCompanyOwner(@PathVariable Long companyId) {
        logger.info("Received request to get company owner for company with id {}", companyId);

        UserDTO companyOwner = companyService.getCompanyOwner(companyId);

        logger.info("Returning company owner for company with id {}", companyId);

        return ResponseEntity.ok(companyOwner);
    }

    @Operation(summary = "Get companies where the user is a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Companies found successfully"),
            @ApiResponse(responseCode = "404", description = "User does not exist", content = @Content)
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<CompanyDTO>> getCompaniesUserBelongsTo(@PathVariable String userId) {
        logger.info("Received request to get companies user belongs to for user with id {}", userId);

        List<CompanyDTO> companyDTOs = companyService.getCompaniesUserBelongsTo(userId);

        logger.info("Returning companies user belongs to for user with id {}", userId);

        return ResponseEntity.ok(companyDTOs);
    }

    @Operation(summary = "Delete company", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company deleted successfully"),
            @ApiResponse(responseCode = "403",
                    description = "User is not authorized to delete the company. Only the company owner can delete it.",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Company does not exist", content = @Content)
    })
    @DeleteMapping("/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long companyId, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to delete company with id {} by user with id {}", companyId, userId);

        companyService.deleteCompany(companyId, userId);

        logger.info("Company with id {} successfully deleted by user with id {}", companyId, userId);

        return ResponseEntity.ok("Company successfully deleted");
    }
}
