package com.microservices.auth_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/auth")
public class AuthServiceController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Auth Service!";
    }    

}
