package com.ffms.trackable.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @GetMapping("/authorization/google")
    public ResponseEntity<?> getGoogleAuthUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", "/oauth2/authorization/google");
        response.put("message", "Redirect to this URL to initiate Google OAuth2 login");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login/success")
    public ResponseEntity<?> loginSuccess(@RequestParam String token, @RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("message", "OAuth2 login successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login/failure")
    public ResponseEntity<?> loginFailure(@RequestParam String error) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", "OAuth2 login failed");
        return ResponseEntity.badRequest().body(response);
    }
}
