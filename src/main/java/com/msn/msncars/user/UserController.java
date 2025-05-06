package com.msn.msncars.user;

import com.msn.msncars.auth.AccountRole;
import com.msn.msncars.company.CompanyDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getBasicUserInformation(@AuthenticationPrincipal Jwt jwt) {
        UserDTO userDTO = userService.getBasicUserInformation(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<AccountRole>> getUserRoles(@AuthenticationPrincipal Jwt jwt) {
        List<AccountRole> accountRoles = userService.getUserRoles(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.OK).body(accountRoles);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<CompanyDTO>> getCompaniesUserBelongsTo(@AuthenticationPrincipal Jwt jwt) {
        List<CompanyDTO> companies = userService.getCompaniesUserBelongsTo(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.OK).body(companies);
    }

    @DeleteMapping
    public void deleteUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(jwt.getSubject());
    }
}
