package com.salem.resourceserver;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt; //added
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class UserController {
    // get status endpoint
    @GetMapping("/users/{userId}/status/{code}")
    @PreAuthorize("hasAuthority('SCOPE_read:status')")
    public UserStatus getUserStatus(
            @PathVariable String userId,
            @PathVariable String code,
            @AuthenticationPrincipal Jwt jwt) {  // injects the validated token

        // read the acting_user claim from the token
        String actingUser = jwt.getClaimAsString("https://resource-server-api/acting_user");

        // ownership check (can this caller access this user's data)
        if (!userId.equals(actingUser)) {
            throw new AccessDeniedException("You can only access your own status");
        }

        AccountStatus status = switch (code) {
            case "A" -> AccountStatus.ACTIVE;
            case "L" -> AccountStatus.LOCKED;
            case "S" -> AccountStatus.SUSPENDED;
            default -> AccountStatus.UNKNOWN;
        };
        return new UserStatus(userId, status);
    }
}