package com.msn.msncars;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
    Demo controller to test accessing endpoints based on user role
*/

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint(){
        return "Hello from public endpoint";
    }

    @GetMapping("/secure")
    public String secureEndpoint(@AuthenticationPrincipal Jwt authenticationPrincipal){
        return String.format(
                "Hello %s with userId: %s, from secure endpoint",
                authenticationPrincipal.getClaimAsString("preferred_username"),
                authenticationPrincipal.getClaimAsString("sub")
        );
    }

    /*
        Credentials for sample account with user permissions:
        login: user
        password: user
     */
    @GetMapping("/user")
    public String userEndpoint(){
        return "Hello from user endpoint";
    }

    /*
        Credentials for sample account with company permissions:
        login: company
        password: company
     */
    @GetMapping("/company")
    public String companyEndpoint(){
        return "Hello from company endpoint";
    }

    /*
        Credentials for sample account with admin permissions:
        login: admin
        password: admin
    */
    @GetMapping("/admin")
    public String adminEndpoint(){
        return "Hello from admin endpoint";
    }
}
