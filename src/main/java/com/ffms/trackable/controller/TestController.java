package com.ffms.trackable.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @PostMapping("/success")
    public @ResponseBody Map<String, Object> userAccess(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        response.put("status", true);
        response.put("code", 0);
        response.put("message", "Success");

        data.put("type", "Single");
        data.put("count", 1);

        response.put("data", data);
        return response;
    }

    @PostMapping("/error")
    public @ResponseBody Map<String, Object> userAccess1(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        response.put("status", false);
        response.put("code", 9003);
        response.put("message", "Something went wrong");

        response.put("data", data);
        return response;
    }

    @GetMapping("/mod")
    @PreAuthorize("hasPermission(#id, 'mod', 'update')")
    public String moderatorAccess() {
        return "Moderator Board.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}
