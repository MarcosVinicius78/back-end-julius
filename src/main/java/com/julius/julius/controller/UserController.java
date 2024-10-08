package com.julius.julius.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {
    
    @GetMapping("/user")
    public ResponseEntity<?> getUser(Authentication authentication) {
        return ResponseEntity.ok().build();
    }
}
