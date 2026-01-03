package com.ffms.resqeats.service.shop;

import com.ffms.resqeats.dto.shop.*;
import com.ffms.resqeats.enums.shop.ShopCategory;
import com.ffms.resqeats.enums.shop.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ShopService {

    ShopResponse createShop(CreateShopRequest request, Long ownerId);

    ShopResponse updateShop(Long shopId, UpdateShopRequest request, Long ownerId);

    ShopResponse getShopById(Long shopId);

    ShopResponse getShopByIdForOwner(Long shopId, Long ownerId);

    List<ShopResponse> getShopsByOwner(Long ownerId);

    Page<ShopResponse> getAllShops(ShopStatus status, ShopCategory category, Pageable pageable);

    List<ShopResponse> getNearbyShops(BigDecimal latitude, BigDecimal longitude, Double radiusKm, String category);

    Page<ShopResponse> getNearbyShopsWithPagination(BigDecimal latitude, BigDecimal longitude, Double radiusKm, Pageable pageable);

    ShopResponse approveShop(Long shopId);

    ShopResponse rejectShop(Long shopId, String reason);

    ShopResponse suspendShop(Long shopId, String reason);

    ShopResponse toggleShopOpenStatus(Long shopId, Long ownerId);

    void deleteShop(Long shopId, Long ownerId);

    Double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2);
}
