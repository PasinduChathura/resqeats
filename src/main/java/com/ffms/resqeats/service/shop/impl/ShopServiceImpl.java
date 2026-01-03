package com.ffms.resqeats.service.shop.impl;

import com.ffms.resqeats.common.logging.AppLogger;
import com.ffms.resqeats.dto.shop.*;
import com.ffms.resqeats.enums.shop.ShopCategory;
import com.ffms.resqeats.enums.shop.ShopStatus;
import com.ffms.resqeats.exception.shop.ShopException;
import com.ffms.resqeats.models.shop.Shop;
import com.ffms.resqeats.models.shop.ShopOperatingDay;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.shop.ShopOperatingDayRepository;
import com.ffms.resqeats.repository.shop.ShopRepository;
import com.ffms.resqeats.repository.usermgt.UserRepository;
import com.ffms.resqeats.service.notification.NotificationService;
import com.ffms.resqeats.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShopServiceImpl implements ShopService {

    private final AppLogger appLogger = AppLogger.of(log);

    private final ShopRepository shopRepository;
    private final ShopOperatingDayRepository operatingDayRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public ShopResponse createShop(CreateShopRequest request, Long ownerId) {
        appLogger.logStart("CREATE", "Shop", ownerId);
        
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    appLogger.logError("CREATE", "Shop", ownerId, "Owner not found");
                    return ShopException.ownerNotFound(ownerId);
                });

        Shop shop = Shop.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .status(ShopStatus.PENDING_APPROVAL)
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .pickupStartTime(request.getPickupStartTime())
                .pickupEndTime(request.getPickupEndTime())
                .owner(owner)
                .isOpen(false)
                .build();

        shop = shopRepository.save(shop);

        if (request.getOperatingDays() != null && !request.getOperatingDays().isEmpty()) {
            Shop finalShop = shop;
            List<ShopOperatingDay> operatingDays = request.getOperatingDays().stream()
                    .map(dayRequest -> ShopOperatingDay.builder()
                            .dayOfWeek(dayRequest.getDayOfWeek())
                            .openingTime(dayRequest.getOpeningTime())
                            .closingTime(dayRequest.getClosingTime())
                            .pickupStartTime(dayRequest.getPickupStartTime())
                            .pickupEndTime(dayRequest.getPickupEndTime())
                            .isClosed(dayRequest.getIsClosed() != null ? dayRequest.getIsClosed() : false)
                            .shop(finalShop)
                            .build())
                    .collect(Collectors.toList());
            operatingDayRepository.saveAll(operatingDays);
            shop.getOperatingDays().addAll(operatingDays);
        }

        appLogger.logSuccess("CREATE", "Shop", shop.getId(), 
                String.format("Shop '%s' created by owner %d", shop.getName(), ownerId));
        return mapToShopResponse(shop, null, null);
    }

    @Override
    public ShopResponse updateShop(Long shopId, UpdateShopRequest request, Long ownerId) {
        appLogger.logStart("UPDATE", "Shop", shopId);
        
        Shop shop = shopRepository.findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() -> {
                    appLogger.logWarning("UPDATE", "Shop", shopId, "Not found or access denied");
                    return ShopException.accessDenied(shopId);
                });

        if (request.getName() != null) shop.setName(request.getName());
        if (request.getDescription() != null) shop.setDescription(request.getDescription());
        if (request.getAddress() != null) shop.setAddress(request.getAddress());
        if (request.getCity() != null) shop.setCity(request.getCity());
        if (request.getPostalCode() != null) shop.setPostalCode(request.getPostalCode());
        if (request.getLatitude() != null) shop.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) shop.setLongitude(request.getLongitude());
        if (request.getPhone() != null) shop.setPhone(request.getPhone());
        if (request.getEmail() != null) shop.setEmail(request.getEmail());
        if (request.getImageUrl() != null) shop.setImageUrl(request.getImageUrl());
        if (request.getCategory() != null) shop.setCategory(request.getCategory());
        if (request.getOpeningTime() != null) shop.setOpeningTime(request.getOpeningTime());
        if (request.getClosingTime() != null) shop.setClosingTime(request.getClosingTime());
        if (request.getPickupStartTime() != null) shop.setPickupStartTime(request.getPickupStartTime());
        if (request.getPickupEndTime() != null) shop.setPickupEndTime(request.getPickupEndTime());

        if (request.getOperatingDays() != null) {
            operatingDayRepository.deleteByShopId(shopId);
            shop.getOperatingDays().clear();

            List<ShopOperatingDay> operatingDays = request.getOperatingDays().stream()
                    .map(dayRequest -> ShopOperatingDay.builder()
                            .dayOfWeek(dayRequest.getDayOfWeek())
                            .openingTime(dayRequest.getOpeningTime())
                            .closingTime(dayRequest.getClosingTime())
                            .pickupStartTime(dayRequest.getPickupStartTime())
                            .pickupEndTime(dayRequest.getPickupEndTime())
                            .isClosed(dayRequest.getIsClosed() != null ? dayRequest.getIsClosed() : false)
                            .shop(shop)
                            .build())
                    .collect(Collectors.toList());
            operatingDayRepository.saveAll(operatingDays);
            shop.getOperatingDays().addAll(operatingDays);
        }

        Shop updatedShop = shopRepository.save(shop);
        log.info("Shop updated successfully with id: {}", updatedShop.getId());
        return mapToShopResponse(updatedShop, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopById(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException("Shop not found with id: " + shopId));
        return mapToShopResponse(shop, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopByIdForOwner(Long shopId, Long ownerId) {
        Shop shop = shopRepository.findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() -> new ShopException("Shop not found or you don't have permission to view it"));
        return mapToShopResponse(shop, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getShopsByOwner(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId).stream()
                .map(shop -> mapToShopResponse(shop, null, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getAllShops(ShopStatus status, ShopCategory category, Pageable pageable) {
        Page<Shop> shops;
        if (status != null && category != null) {
            shops = shopRepository.findByStatusAndCategory(status, category, pageable);
        } else if (status != null) {
            shops = shopRepository.findByStatus(status, pageable);
        } else if (category != null) {
            shops = shopRepository.findByCategory(category, pageable);
        } else {
            shops = shopRepository.findAll(pageable);
        }
        return shops.map(shop -> mapToShopResponse(shop, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getNearbyShops(BigDecimal latitude, BigDecimal longitude, Double radiusKm, String category) {
        List<Shop> shops;
        if (category != null && !category.isEmpty()) {
            shops = shopRepository.findNearbyShopsByCategory(latitude, longitude, radiusKm, category);
        } else {
            shops = shopRepository.findNearbyShops(latitude, longitude, radiusKm);
        }

        return shops.stream()
                .map(shop -> mapToShopResponse(shop, latitude, longitude))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getNearbyShopsWithPagination(BigDecimal latitude, BigDecimal longitude, Double radiusKm, Pageable pageable) {
        Page<Shop> shops = shopRepository.findNearbyShopsWithPagination(latitude, longitude, radiusKm, pageable);
        List<ShopResponse> responses = shops.getContent().stream()
                .map(shop -> mapToShopResponse(shop, latitude, longitude))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, shops.getTotalElements());
    }

    @Override
    public ShopResponse approveShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException("Shop not found with id: " + shopId));

        if (shop.getStatus() != ShopStatus.PENDING_APPROVAL) {
            throw new ShopException("Shop is not in pending approval status");
        }

        shop.setStatus(ShopStatus.APPROVED);
        Shop approvedShop = shopRepository.save(shop);
        log.info("Shop approved with id: {}", approvedShop.getId());
        
        // Notify shop owner about approval
        notificationService.notifyShopApproved(approvedShop);
        
        return mapToShopResponse(approvedShop, null, null);
    }

    @Override
    public ShopResponse rejectShop(Long shopId, String reason) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException("Shop not found with id: " + shopId));

        if (shop.getStatus() != ShopStatus.PENDING_APPROVAL) {
            throw new ShopException("Shop is not in pending approval status");
        }

        shop.setStatus(ShopStatus.REJECTED);
        shop = shopRepository.save(shop);
        log.info("Shop rejected with id: {} for reason: {}", shop.getId(), reason);
        
        // Notify shop owner about rejection
        notificationService.notifyShopRejected(shop, reason);
        
        return mapToShopResponse(shop, null, null);
    }

    @Override
    public ShopResponse suspendShop(Long shopId, String reason) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException("Shop not found with id: " + shopId));

        shop.setStatus(ShopStatus.SUSPENDED);
        shop = shopRepository.save(shop);
        log.info("Shop suspended with id: {} for reason: {}", shop.getId(), reason);
        
        // Notify shop owner about suspension
        notificationService.notifyShopSuspended(shop, reason);
        
        return mapToShopResponse(shop, null, null);
    }

    @Override
    public ShopResponse toggleShopOpenStatus(Long shopId, Long ownerId) {
        Shop shop = shopRepository.findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() -> new ShopException("Shop not found or you don't have permission"));

        if (shop.getStatus() != ShopStatus.APPROVED) {
            throw new ShopException("Cannot toggle open status for non-approved shop");
        }

        shop.setIsOpen(!shop.getIsOpen());
        shop = shopRepository.save(shop);
        log.info("Shop {} is now {}", shop.getId(), shop.getIsOpen() ? "open" : "closed");
        return mapToShopResponse(shop, null, null);
    }

    @Override
    public void deleteShop(Long shopId, Long ownerId) {
        Shop shop = shopRepository.findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() -> new ShopException("Shop not found or you don't have permission to delete it"));

        shopRepository.delete(shop);
        log.info("Shop deleted with id: {}", shopId);
    }

    @Override
    public Double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private ShopResponse mapToShopResponse(Shop shop, BigDecimal userLat, BigDecimal userLon) {
        Double distance = null;
        if (userLat != null && userLon != null) {
            distance = calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
            distance = Math.round(distance * 100.0) / 100.0; // Round to 2 decimal places
        }

        List<OperatingDayResponse> operatingDays = new ArrayList<>();
        if (shop.getOperatingDays() != null) {
            operatingDays = shop.getOperatingDays().stream()
                    .map(day -> OperatingDayResponse.builder()
                            .id(day.getId())
                            .dayOfWeek(day.getDayOfWeek())
                            .openingTime(day.getOpeningTime())
                            .closingTime(day.getClosingTime())
                            .pickupStartTime(day.getPickupStartTime())
                            .pickupEndTime(day.getPickupEndTime())
                            .isClosed(day.getIsClosed())
                            .build())
                    .collect(Collectors.toList());
        }

        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .address(shop.getAddress())
                .city(shop.getCity())
                .postalCode(shop.getPostalCode())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .imageUrl(shop.getImageUrl())
                .category(shop.getCategory())
                .status(shop.getStatus())
                .openingTime(shop.getOpeningTime())
                .closingTime(shop.getClosingTime())
                .pickupStartTime(shop.getPickupStartTime())
                .pickupEndTime(shop.getPickupEndTime())
                .averageRating(shop.getAverageRating())
                .totalRatings(shop.getTotalRatings())
                .isOpen(shop.getIsOpen())
                .ownerId(shop.getOwner().getId())
                .ownerName(shop.getOwner().getFirstName() + " " + shop.getOwner().getLastName())
                .distanceKm(distance)
                .operatingDays(operatingDays)
                .build();
    }
}
