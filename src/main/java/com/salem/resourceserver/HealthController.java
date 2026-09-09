package com.salem.resourceserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // get default status endpoint
    @GetMapping("/public/health")
    public HealthStatus getHealth(){
        return new HealthStatus("UP");
    }
}
