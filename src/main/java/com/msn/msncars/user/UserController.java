package com.msn.msncars.user;

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

@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get basic information about requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User basic information found"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UserBasicInformationDTO> getBasicUserInformation(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to get basic user information for user with id: {}", userId);

        UserBasicInformationDTO userBasicInformationDTO = userService.getUserBasicInformation(userId);

        logger.info("Basic user information retrieved successfully for user with id: {}", userId);

        return ResponseEntity.status(HttpStatus.OK).body(userBasicInformationDTO);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping
    public void deleteUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        logger.info("Received request to delete user with id: {}", userId);

        userService.deleteUser(userId);

        logger.info("User with id {} successfully deleted.", userId);
    }

    @Operation(summary = "Block user")
    @PatchMapping("/{userId}/block")
    public void blockUser(@AuthenticationPrincipal Jwt jwt, @PathVariable("userId") String userId) {
        logger.info("Received request to block user {}, requested by {}", userId, jwt.getSubject());

        userService.blockUser(userId);

        logger.info("User {} successfully blocked.", userId);
    }
}
