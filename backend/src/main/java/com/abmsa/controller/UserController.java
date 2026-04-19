package com.abmsa.controller;

import com.abmsa.common.Result;
import com.abmsa.dto.LoginRequest;
import com.abmsa.dto.RegisterRequest;
import com.abmsa.entity.User;
import com.abmsa.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request.getUsername(), request.getPassword(), request.getEmail());
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(Authentication authentication) {
        return userService.getUserInfo(authentication.getName());
    }
}
