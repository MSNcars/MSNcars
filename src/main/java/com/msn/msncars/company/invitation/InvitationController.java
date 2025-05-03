package com.msn.msncars.company.invitation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<InvitationDTO> invite(@RequestBody CreateInvitationRequest createInvitationRequest, @AuthenticationPrincipal Jwt jwt) {
        var invitationDTO = invitationService.invite(createInvitationRequest, jwt.getSubject());
        return ResponseEntity.ok(invitationDTO);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<InvitationDTO> acceptInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var invitationDTO = invitationService.acceptInvitation(id, jwt.getSubject());
        return ResponseEntity.ok(invitationDTO);
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<InvitationDTO> declineInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var invitationDTO = invitationService.declineInvitation(id, jwt.getSubject());
        return ResponseEntity.ok(invitationDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        invitationService.deleteInvitation(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/received")
    public ResponseEntity<List<InvitationDTO>> getInvitationsReceivedByUser(@AuthenticationPrincipal Jwt jwt) {
        var invitationDTOs = invitationService.getInvitationsReceivedByUser(jwt.getSubject());
        return ResponseEntity.ok(invitationDTOs);
    }

    @GetMapping("/company/{companyId}/sent")
    public ResponseEntity<List<InvitationDTO>> getInvitationsSentByCompany(@PathVariable Long companyId, @AuthenticationPrincipal Jwt jwt) {
        var invitationDTOs = invitationService.getInvitationsSentByCompany(companyId, jwt.getSubject());
        return ResponseEntity.ok(invitationDTOs);
    }

}
