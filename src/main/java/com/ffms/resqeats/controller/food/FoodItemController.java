package com.ffms.resqeats.controller.food;

import com.ffms.resqeats.dto.food.*;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.food.FoodItemService;
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

import java.util.List;

@RestController
@RequestMapping("/food-items")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;

    @PostMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<FoodItemResponse> createFoodItem(
            @Valid @RequestBody CreateFoodItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FoodItemResponse response = foodItemService.createFoodItem(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{foodItemId}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<FoodItemResponse> updateFoodItem(
            @PathVariable Long foodItemId,
            @Valid @RequestBody CreateFoodItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FoodItemResponse response = foodItemService.updateFoodItem(foodItemId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{foodItemId}")
    public ResponseEntity<FoodItemResponse> getFoodItemById(@PathVariable Long foodItemId) {
        FoodItemResponse response = foodItemService.getFoodItemById(foodItemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<FoodItemResponse>> getFoodItemsByShop(@PathVariable Long shopId) {
        List<FoodItemResponse> items = foodItemService.getFoodItemsByShop(shopId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/shop/{shopId}/page")
    public ResponseEntity<Page<FoodItemResponse>> getFoodItemsByShopWithPagination(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FoodItemResponse> items = foodItemService.getFoodItemsByShop(shopId, pageable);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{foodItemId}")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> deleteFoodItem(
            @PathVariable Long foodItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        foodItemService.deleteFoodItem(foodItemId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{foodItemId}/deactivate")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> deactivateFoodItem(
            @PathVariable Long foodItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        foodItemService.deactivateFoodItem(foodItemId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{foodItemId}/activate")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> activateFoodItem(
            @PathVariable Long foodItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        foodItemService.activateFoodItem(foodItemId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
