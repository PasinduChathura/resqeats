package com.ffms.resqeats.controller.food;

import com.ffms.resqeats.dto.food.*;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.food.SecretBoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/secret-boxes")
@RequiredArgsConstructor
public class SecretBoxController {

    private final SecretBoxService secretBoxService;

    @PostMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<SecretBoxResponse> createSecretBox(
            @Valid @RequestBody CreateSecretBoxRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SecretBoxResponse response = secretBoxService.createSecretBox(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{secretBoxId}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<SecretBoxResponse> updateSecretBox(
            @PathVariable Long secretBoxId,
            @Valid @RequestBody CreateSecretBoxRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SecretBoxResponse response = secretBoxService.updateSecretBox(secretBoxId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{secretBoxId}")
    public ResponseEntity<SecretBoxResponse> getSecretBoxById(@PathVariable Long secretBoxId) {
        SecretBoxResponse response = secretBoxService.getSecretBoxById(secretBoxId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<SecretBoxResponse>> getSecretBoxesByShop(@PathVariable Long shopId) {
        List<SecretBoxResponse> boxes = secretBoxService.getSecretBoxesByShop(shopId);
        return ResponseEntity.ok(boxes);
    }

    @GetMapping("/shop/{shopId}/available")
    public ResponseEntity<List<SecretBoxResponse>> getAvailableBoxesByShop(@PathVariable Long shopId) {
        List<SecretBoxResponse> boxes = secretBoxService.getAvailableBoxesByShop(shopId);
        return ResponseEntity.ok(boxes);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<SecretBoxResponse>> getAllAvailableBoxes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SecretBoxResponse> boxes = secretBoxService.getAllAvailableBoxes(pageable);
        return ResponseEntity.ok(boxes);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<SecretBoxResponse>> getNearbyAvailableBoxes(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "5.0") Double radius) {
        List<SecretBoxResponse> boxes = secretBoxService.getNearbyAvailableBoxes(lat, lng, radius);
        return ResponseEntity.ok(boxes);
    }

    @PutMapping("/{secretBoxId}/quantity")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<SecretBoxResponse> updateBoxQuantity(
            @PathVariable Long secretBoxId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SecretBoxResponse response = secretBoxService.updateBoxQuantity(secretBoxId, quantity, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{secretBoxId}/deactivate")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> deactivateSecretBox(
            @PathVariable Long secretBoxId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        secretBoxService.deactivateSecretBox(secretBoxId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{secretBoxId}/activate")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> activateSecretBox(
            @PathVariable Long secretBoxId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        secretBoxService.activateSecretBox(secretBoxId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{secretBoxId}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> deleteSecretBox(
            @PathVariable Long secretBoxId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        secretBoxService.deleteSecretBox(secretBoxId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
