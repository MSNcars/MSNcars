package com.msn.msncars.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserBasicInformationDTO> getBasicUserInformation(@AuthenticationPrincipal Jwt jwt) {
        UserBasicInformationDTO userBasicInformationDTO = userService.getUserBasicInformation(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.OK).body(userBasicInformationDTO);
    }

    @DeleteMapping
    public void deleteUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(jwt.getSubject());
    }
}
