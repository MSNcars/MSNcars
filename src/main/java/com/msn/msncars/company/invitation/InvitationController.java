package com.msn.msncars.company.invitation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/invitations")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    private final Logger logger = LoggerFactory.getLogger(InvitationController.class);

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Operation(summary = "Create a new invitation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invitation created successfully"),
            @ApiResponse(responseCode = "403",
                    description = "Only company owner is allowed to send invitation",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404",
                    description = "Sender company or invitation recipient does not exist",
                    content = @Content
            ),
            @ApiResponse(responseCode = "409",
                    description = "User already accepted the invitation and cannot be invited again",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<InvitationSentByCompanyDTO> invite(@RequestBody CreateInvitationRequest createInvitationRequest, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to create invitation by user with id: {}", userId);

        var invitationDTO = invitationService.invite(createInvitationRequest, userId);

        logger.info("Invitation created by user with id: {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(invitationDTO);
    }

    @Operation(summary = "Accept invitation received by requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
            @ApiResponse(responseCode = "403", description = "Only the recipient of this invitation can respond to it", content = @Content),
            @ApiResponse(responseCode = "404", description = "Invitation does not exist", content = @Content),
            @ApiResponse(responseCode = "409", description = "User has already responded to this invitation", content = @Content),
    })
    @PostMapping("/{id}/accept")
    public ResponseEntity<InvitationReceivedByUserDTO> acceptInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to accept invitation with id {} by user with id: {}", id, userId);

        var invitationDTO = invitationService.acceptInvitation(id, userId);

        logger.info("Invitation with id {} accepted by user with id: {}", id, userId);

        return ResponseEntity.ok(invitationDTO);
    }

    @Operation(summary = "Decline invitation received by requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation declined successfully"),
            @ApiResponse(responseCode = "403", description = "Only the recipient of this invitation can respond to it", content = @Content),
            @ApiResponse(responseCode = "404", description = "Invitation does not exist", content = @Content),
            @ApiResponse(responseCode = "409", description = "User has already responded to this invitation", content = @Content),
    })
    @PostMapping("/{id}/decline")
    public ResponseEntity<InvitationReceivedByUserDTO> declineInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to decline invitation with id {} by user with id: {}", id, userId);

        var invitationDTO = invitationService.declineInvitation(id, userId);

        logger.info("Invitation with id {} declined by user with id: {}", id, userId);

        return ResponseEntity.ok(invitationDTO);
    }

    @Operation(summary = "Delete invitation of requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invitation deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only the invitation owner is allowed to delete it", content = @Content),
            @ApiResponse(responseCode = "404", description = "Invitation does not exist", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to delete invitation with id {} by user with id: {}", id, userId);

        invitationService.deleteInvitation(id, userId);

        logger.info("Invitation with id {} deleted by user with id: {}", id, userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get invitations received by requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations received by user returned successfully"),
    })
    @GetMapping("/user/received")
    public ResponseEntity<List<InvitationReceivedByUserDTO>> getInvitationsReceivedByUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to get invitations received by user with id: {}", userId);

        var invitationDTOs = invitationService.getInvitationsReceivedByUser(userId);

        logger.info("Returning invitations received by user with id: {}", userId);

        return ResponseEntity.ok(invitationDTOs);
    }

    @Operation(summary = "Get invitations sent by company of requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations sent by company returned successfully"),
            @ApiResponse(responseCode = "403", description = "Only company members are allowed to view invitations sent by the company", content = @Content),
            @ApiResponse(responseCode = "404", description = "Company does not exist", content = @Content)
    })
    @GetMapping("/company/{companyId}/sent")
    public ResponseEntity<List<InvitationSentByCompanyDTO>> getInvitationsSentByCompany(@PathVariable Long companyId, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to get invitations sent by company with id: {}. Request sent by user with id {}", companyId, userId);

        var invitationDTOs = invitationService.getInvitationsSentByCompany(companyId, userId);

        logger.info("Returning invitations sent by company with id: {}", companyId);

        return ResponseEntity.ok(invitationDTOs);
    }

}
