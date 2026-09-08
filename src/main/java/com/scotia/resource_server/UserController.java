package com.scotia.resource_server;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {
    // get status endpoint
    @GetMapping("/users/{userId}/status/{code}")
    @PreAuthorize("hasAuthority('SCOPE_read:status')")
    public UserStatus getUserStatus(@PathVariable String userId, @PathVariable String code) {
        AccountStatus status = switch (code){
            case "A" -> AccountStatus.ACTIVE;
            case "L" -> AccountStatus.LOCKED;
            case "S" -> AccountStatus.SUSPENDED;
            default -> AccountStatus.UNKNOWN;
        };
        return new UserStatus(userId, status);
    }

    // login endpoint
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {
        return ("Login attempt received for user: " + req.username());
    }
}