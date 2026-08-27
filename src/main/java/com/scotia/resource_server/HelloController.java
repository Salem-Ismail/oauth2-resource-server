package com.scotia.resource_server;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Resource server is up.";
    }

}