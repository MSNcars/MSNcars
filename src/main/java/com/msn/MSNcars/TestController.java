package com.msn.MSNcars;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint(){
        return "Hello from public endpoint";
    }

    @GetMapping("/secure")
    public String secureEndpoint(){
        return "Hello from secure endpoint";
    }
}
