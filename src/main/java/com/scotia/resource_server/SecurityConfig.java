package com.scotia.resource_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// This class contains the security rules for the application.
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Creates a custom object (SecurityFilterChain) and tells Spring to manage and use it.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Defines which requests are public and which requests need authentication.
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Configures the application to use JWT tokens for authentication.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );

        // Builds the security configuration and gives it back to Spring.
        return http.build();
    }
}