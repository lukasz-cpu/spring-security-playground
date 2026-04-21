package com.spring_authentication.demo;

import java.security.Principal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/anonymous/hello")
    public String anonymousHello(@RequestParam String name, Principal principal) {
        return "hello, " + name + " :caller=" + caller(principal);
    }

    @GetMapping("/api/authn/hello")
    @Operation(description = "sample authenticated GET",
            security = @SecurityRequirement(name = "basicAuth"))
    public String authenticatedHello(
            @RequestParam(name = "name", defaultValue = "you", required = false) String name,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        return "hello, " + name + " :caller=" + user.getUsername();
    }

    private String caller(Principal principal) {
        return principal == null ? "(null)" : principal.getName();
    }
}
