package com.fruit.market.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> result = new HashMap<>();
        if (auth != null && auth.isAuthenticated()) {
            result.put("username", auth.getName());
            result.put("roles", auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList());
        }
        return result;
    }
}
