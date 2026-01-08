package com.ffms.resqeats.outlet.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.geo.service.GeoService;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.outlet.dto.CreateOutletRequest;
import com.ffms.resqeats.outlet.dto.OutletDto;
import com.ffms.resqeats.outlet.dto.OutletFilterDto;
import com.ffms.resqeats.outlet.dto.OutletListResponseDto;
import com.ffms.resqeats.outlet.dto.UpdateOutletRequest;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.entity.OutletHours;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import com.ffms.resqeats.outlet.repository.OutletHoursRepository;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.outlet.specification.OutletSpecification;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.repository.UserRepository;
import com.ffms.resqeats.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing outlet operations and lifecycle.
 *
 * <p>This service handles all outlet-related business operations including creation,
 * updates, status management, operating hours configuration, and geo-location features.
 * Implements business rules per SRS Section 6.5.</p>
 *
 * <p>Business Rules:</p>
 * <ul>
 *   <li>BR-018: Merchant must be approved before creating outlets</li>
 *   <li>BR-020: Outlet can only be managed by merchant owner or outlet staff</li>
 *   <li>BR-021: Operating hours must be set before outlet goes active</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutletService {

    private final OutletRepository outletRepository;
    private final OutletHoursRepository outletHoursRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final GeoService geoService;
    private final WebSocketService webSocketService;

    /**
     * Creates a new outlet for a merchant.
     *
     * <p>Creates an outlet with PENDING_APPROVAL status. Operating hours can be
     * optionally provided during creation. The outlet is automatically added to
     * the geo-location index for proximity searches.</p>
     *
     * @param merchantId the unique identifier of the merchant
     * @param request the outlet creation request containing outlet details
     * @param userId the unique identifier of the user performing the operation
     * @return the created outlet as a DTO
     * @throws BusinessException with code MERCH_004 if merchant is not found
     * @throws BusinessException with code OUTLET_001 if merchant is not approved (BR-018)
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletDto createOutlet(UUID merchantId, CreateOutletRequest request, UUID userId) {
        log.info("Creating outlet for merchant: {} by user: {}", merchantId, userId);
        log.debug("Outlet creation request details - name: {}, city: {}", request.getName(), request.getCity());

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> {
                    log.error("Merchant not found with ID: {}", merchantId);
                    return new BusinessException("MERCH_004", "Merchant not found");
                });

        if (merchant.getStatus() != MerchantStatus.APPROVED) {
            log.warn("Attempt to create outlet for non-approved merchant: {} with status: {}", 
                    merchantId, merchant.getStatus());
            throw new BusinessException("OUTLET_001", "Merchant must be approved before creating outlets");
        }

        if (!merchant.getOwnerUserId().equals(userId)) {
            log.warn("Unauthorized outlet creation attempt - user: {} for merchant: {} owned by: {}", 
                    userId, merchantId, merchant.getOwnerUserId());
            throw new BusinessException("AUTH_003", "Not authorized to create outlets for this merchant");
        }

        Outlet outlet = Outlet.builder()
                .merchantId(merchantId)
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
                .status(OutletStatus.PENDING_APPROVAL)
                .build();

        outlet = outletRepository.save(outlet);
        log.debug("Outlet entity saved with ID: {}", outlet.getId());

        if (request.getOperatingHours() != null) {
            log.debug("Creating {} operating hours entries for outlet: {}", 
                    request.getOperatingHours().size(), outlet.getId());
            for (CreateOutletRequest.OperatingHoursRequest hours : request.getOperatingHours()) {
                OutletHours outletHours = OutletHours.builder()
                        .outletId(outlet.getId())
                        .dayOfWeek(hours.getDayOfWeek())
                        .openTime(hours.getOpenTime())
                        .closeTime(hours.getCloseTime())
                        .isClosed(hours.getIsClosed() != null ? hours.getIsClosed() : false)
                        .build();
                outletHoursRepository.save(outletHours);
            }
        }

        geoService.addOutletToGeoIndex(outlet);
        log.debug("Outlet added to geo index at coordinates: [{}, {}]", 
                outlet.getLatitude(), outlet.getLongitude());

        log.info("Outlet created successfully - outletId: {}, merchantId: {}, name: {}", 
                outlet.getId(), merchantId, outlet.getName());
        return toDto(outlet);
    }

    /**
     * Updates an existing outlet's information.
     *
     * <p>Allows partial updates - only non-null fields in the request will be updated.
     * If geo-coordinates are updated, the outlet's position in the geo-index is also refreshed.</p>
     *
     * @param outletId the unique identifier of the outlet to update
     * @param request the update request containing fields to modify
     * @param userId the unique identifier of the user performing the operation
     * @return the updated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletDto updateOutlet(UUID outletId, UpdateOutletRequest request, UUID userId) {
        log.info("Updating outlet: {} by user: {}", outletId, userId);
        
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet, userId);

        log.debug("Applying updates to outlet: {}", outletId);
        if (request.getName() != null) outlet.setName(request.getName());
        if (request.getDescription() != null) outlet.setDescription(request.getDescription());
        if (request.getAddress() != null) outlet.setAddress(request.getAddress());
        if (request.getCity() != null) outlet.setCity(request.getCity());
        if (request.getPostalCode() != null) outlet.setPostalCode(request.getPostalCode());
        if (request.getPhone() != null) outlet.setPhone(request.getPhone());
        if (request.getEmail() != null) outlet.setEmail(request.getEmail());
        if (request.getImageUrl() != null) outlet.setImageUrl(request.getImageUrl());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            log.debug("Updating geo-coordinates for outlet: {} to [{}, {}]", 
                    outletId, request.getLatitude(), request.getLongitude());
            outlet.setLatitude(request.getLatitude());
            outlet.setLongitude(request.getLongitude());
            geoService.addOutletToGeoIndex(outlet);
        }

        outlet = outletRepository.save(outlet);
        log.info("Outlet updated successfully: {}", outletId);
        return toDto(outlet);
    }

    /**
     * Sets or updates the operating hours for an outlet.
     *
     * <p>Replaces all existing operating hours with the provided schedule.
     * Operating hours must be set before an outlet can be activated per BR-021.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @param hoursRequest the list of operating hours for each day of the week
     * @param userId the unique identifier of the user performing the operation
     * @return the updated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletDto setOperatingHours(UUID outletId, List<CreateOutletRequest.OperatingHoursRequest> hoursRequest, 
                                        UUID userId) {
        log.info("Setting operating hours for outlet: {} by user: {}", outletId, userId);
        
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet, userId);

        log.debug("Deleting existing operating hours for outlet: {}", outletId);
        outletHoursRepository.deleteByOutletId(outletId);

        log.debug("Creating {} new operating hours entries for outlet: {}", hoursRequest.size(), outletId);
        for (CreateOutletRequest.OperatingHoursRequest hours : hoursRequest) {
            OutletHours outletHours = OutletHours.builder()
                    .outletId(outletId)
                    .dayOfWeek(hours.getDayOfWeek())
                    .openTime(hours.getOpenTime())
                    .closeTime(hours.getCloseTime())
                    .isClosed(hours.getIsClosed() != null ? hours.getIsClosed() : false)
                    .build();
            outletHoursRepository.save(outletHours);
        }

        log.info("Operating hours set successfully for outlet: {}", outletId);
        return toDto(outlet);
    }

    /**
     * Activates an outlet, making it visible to customers.
     *
     * <p>Changes outlet status to ACTIVE. Per BR-021, operating hours must be
     * configured before activation is allowed.</p>
     *
     * @param outletId the unique identifier of the outlet to activate
     * @param userId the unique identifier of the user performing the operation
     * @return the activated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     * @throws BusinessException with code OUTLET_002 if operating hours are not set
     */
    @Transactional
    public OutletDto activateOutlet(UUID outletId, UUID userId) {
        log.info("Activating outlet: {} by user: {}", outletId, userId);
        
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet, userId);

        List<OutletHours> hours = outletHoursRepository.findByOutletId(outletId);
        if (hours.isEmpty()) {
            log.warn("Activation failed for outlet: {} - operating hours not set", outletId);
            throw new BusinessException("OUTLET_002", "Set operating hours before activating outlet");
        }

        outlet.setStatus(OutletStatus.ACTIVE);
        outlet = outletRepository.save(outlet);

        log.info("Outlet activated successfully: {}", outletId);
        return toDto(outlet);
    }

    /**
     * Deactivates an outlet, removing it from customer visibility.
     *
     * <p>Changes outlet status to DEACTIVATED and broadcasts the status change
     * via WebSocket to notify connected clients.</p>
     *
     * @param outletId the unique identifier of the outlet to deactivate
     * @param userId the unique identifier of the user performing the operation
     * @return the deactivated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletDto deactivateOutlet(UUID outletId, UUID userId) {
        log.info("Deactivating outlet: {} by user: {}", outletId, userId);
        
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet, userId);

        outlet.setStatus(OutletStatus.DEACTIVATED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.debug("WebSocket notification sent for outlet deactivation: {}", outletId);
        
        log.info("Outlet deactivated successfully: {}", outletId);
        return toDto(outlet);
    }

    /**
     * Temporarily closes an outlet.
     *
     * <p>Changes outlet status to TEMPORARILY_CLOSED and broadcasts the status
     * change via WebSocket. The outlet can be reopened later using {@link #reopenOutlet}.</p>
     *
     * @param outletId the unique identifier of the outlet to temporarily close
     * @param userId the unique identifier of the user performing the operation
     * @return the temporarily closed outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletDto temporarilyCloseOutlet(UUID outletId, UUID userId) {
        log.info("Temporarily closing outlet: {} by user: {}", outletId, userId);
        
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet, userId);

        outlet.setStatus(OutletStatus.TEMPORARILY_CLOSED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.debug("WebSocket notification sent for outlet temporary closure: {}", outletId);
        
        log.info("Outlet temporarily closed successfully: {}", outletId);
        return toDto(outlet);
    }

    /**
     * Reopens a temporarily closed outlet.
     *
     * <p>Changes outlet status from TEMPORARILY_CLOSED back to ACTIVE and broadcasts
     * the status change via WebSocket to notify connected clients.</p>
     *
     * @param outletId the unique identifier of the outlet to reopen
     * @param userId the unique identifier of the user performing the operation
     * @return the reopened outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     * @throws BusinessException with code OUTLET_003 if outlet is not temporarily closed
     */
    @Transactional
    public OutletDto reopenOutlet(UUID outletId, UUID userId) {
        log.info("Reopening outlet: {} by user: {}", outletId, userId);
        
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet, userId);

        if (outlet.getStatus() != OutletStatus.TEMPORARILY_CLOSED) {
            log.warn("Reopen failed for outlet: {} - current status is {} (expected TEMPORARILY_CLOSED)", 
                    outletId, outlet.getStatus());
            throw new BusinessException("OUTLET_003", "Outlet is not temporarily closed");
        }

        outlet.setStatus(OutletStatus.ACTIVE);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, true);
        log.debug("WebSocket notification sent for outlet reopen: {}", outletId);
        
        log.info("Outlet reopened successfully: {}", outletId);
        return toDto(outlet);
    }

    /**
     * Checks if an outlet is currently open based on its operating hours.
     *
     * <p>Evaluates the outlet's status and operating hours for the current day
     * and time to determine if the outlet is accepting orders.</p>
     *
     * @param outletId the unique identifier of the outlet to check
     * @return true if the outlet is active and within operating hours, false otherwise
     */
    public boolean isCurrentlyOpen(UUID outletId) {
        log.debug("Checking if outlet is currently open: {}", outletId);
        
        Outlet outlet = outletRepository.findById(outletId).orElse(null);
        if (outlet == null) {
            log.debug("Outlet not found: {}", outletId);
            return false;
        }
        
        if (outlet.getStatus() != OutletStatus.ACTIVE) {
            log.debug("Outlet {} is not active, current status: {}", outletId, outlet.getStatus());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue() % 7;
        LocalTime currentTime = now.toLocalTime();

        OutletHours hours = outletHoursRepository.findByOutletIdAndDayOfWeek(outletId, dayOfWeek)
                .orElse(null);

        if (hours == null || Boolean.TRUE.equals(hours.getIsClosed())) {
            log.debug("Outlet {} is closed for day: {}", outletId, dayOfWeek);
            return false;
        }

        boolean isOpen = hours.getOpenTime() != null && hours.getCloseTime() != null &&
               !currentTime.isBefore(hours.getOpenTime()) && currentTime.isBefore(hours.getCloseTime());
        
        log.debug("Outlet {} open status: {} (current time: {}, hours: {}-{})", 
                outletId, isOpen, currentTime, hours.getOpenTime(), hours.getCloseTime());
        return isOpen;
    }

    /**
     * Retrieves an outlet by its unique identifier.
     *
     * @param outletId the unique identifier of the outlet
     * @return the outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     */
    public OutletDto getOutlet(UUID outletId) {
        log.info("Retrieving outlet: {}", outletId);
        OutletDto outlet = toDto(getOutletOrThrow(outletId));
        log.debug("Outlet retrieved successfully: {}", outletId);
        return outlet;
    }

    /**
     * Retrieves all outlets belonging to a specific merchant.
     *
     * @param merchantId the unique identifier of the merchant
     * @return a list of outlets as DTOs
     */
    public List<OutletDto> getOutletsByMerchant(UUID merchantId) {
        log.info("Retrieving outlets for merchant: {}", merchantId);
        List<OutletDto> outlets = outletRepository.findByMerchantId(merchantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        log.info("Retrieved {} outlets for merchant: {}", outlets.size(), merchantId);
        return outlets;
    }

    /**
     * Finds outlets near a specified geographic location.
     *
     * <p>Validates the provided coordinates and radius before performing
     * the geo-spatial search. Returns outlets sorted by distance.</p>
     *
     * @param latitude the latitude of the search center (-90 to 90)
     * @param longitude the longitude of the search center (-180 to 180)
     * @param radiusKm the search radius in kilometers (0 to 1000), nullable
     * @return a list of nearby outlets with distance information
     * @throws BusinessException with code GEO_001 if latitude is invalid
     * @throws BusinessException with code GEO_002 if longitude is invalid
     * @throws BusinessException with code GEO_003 if radius is invalid
     */
    public List<GeoService.NearbyOutlet> getNearbyOutlets(double latitude, double longitude, Double radiusKm) {
        log.info("Finding nearby outlets at coordinates: [{}, {}] within radius: {} km", 
                latitude, longitude, radiusKm);

        if (latitude < -90.0 || latitude > 90.0) {
            log.warn("Invalid latitude provided: {}", latitude);
            throw new BusinessException("GEO_001", "Invalid latitude. Must be between -90 and 90");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            log.warn("Invalid longitude provided: {}", longitude);
            throw new BusinessException("GEO_002", "Invalid longitude. Must be between -180 and 180");
        }
        if (radiusKm != null && (radiusKm < 0 || radiusKm > 1000)) {
            log.warn("Invalid radius provided: {} km", radiusKm);
            throw new BusinessException("GEO_003", "Invalid radius. Must be between 0 and 1000 km");
        }
        
        List<GeoService.NearbyOutlet> nearbyOutlets = geoService.findNearbyOutlets(latitude, longitude, radiusKm);
        log.info("Found {} nearby outlets at [{}, {}]", nearbyOutlets.size(), latitude, longitude);
        return nearbyOutlets;
    }

    /**
     * Retrieves all outlets with comprehensive filtering.
     *
     * @param filter the filter criteria
     * @param pageable the pagination parameters
     * @return a page of filtered outlets as list response DTOs
     */
    public Page<OutletListResponseDto> getAllOutlets(OutletFilterDto filter, Pageable pageable) {
        log.info("Retrieving all outlets with filter: {}, page: {}, size: {}", 
                filter, pageable.getPageNumber(), pageable.getPageSize());
        Page<OutletListResponseDto> outlets = outletRepository.findAll(OutletSpecification.filterBy(filter), pageable)
                .map(this::toListDto);
        log.info("Retrieved {} outlets", outlets.getTotalElements());
        return outlets;
    }

    /**
     * Retrieves a paginated list of active outlets for customer discovery.
     *
     * @param pageable the pagination parameters
     * @return a page of active outlets as list response DTOs
     */
    public Page<OutletListResponseDto> getActiveOutlets(Pageable pageable) {
        log.info("Retrieving active outlets, page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<OutletListResponseDto> outlets = outletRepository.findByStatus(OutletStatus.ACTIVE, pageable)
                .map(this::toListDto);
        log.info("Retrieved {} active outlets (total: {})", 
                outlets.getNumberOfElements(), outlets.getTotalElements());
        return outlets;
    }

    /**
     * Searches outlets by name using a case-insensitive partial match.
     *
     * @param query the search query string
     * @param pageable the pagination parameters
     * @return a page of matching outlets as list response DTOs
     */
    public Page<OutletListResponseDto> searchOutlets(String query, Pageable pageable) {
        log.info("Searching outlets with query: '{}', page: {}, size: {}", 
                query, pageable.getPageNumber(), pageable.getPageSize());
        Page<OutletListResponseDto> results = outletRepository.findByNameContainingIgnoreCase(query, pageable).map(this::toListDto);
        log.info("Search completed - found {} results for query: '{}'", results.getTotalElements(), query);
        return results;
    }

    /**
     * Retrieves an outlet entity by ID or throws an exception if not found.
     *
     * @param outletId the unique identifier of the outlet
     * @return the outlet entity
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     */
    private Outlet getOutletOrThrow(UUID outletId) {
        return outletRepository.findById(outletId)
                .orElseThrow(() -> {
                    log.error("Outlet not found with ID: {}", outletId);
                    return new BusinessException("OUTLET_004", "Outlet not found");
                });
    }

    /**
     * Validates that a user has permission to access and manage an outlet.
     *
     * <p>Access is granted if the user is:</p>
     * <ul>
     *   <li>An admin user</li>
     *   <li>The merchant owner of the outlet</li>
     *   <li>An outlet user assigned to this specific outlet</li>
     * </ul>
     *
     * @param outlet the outlet to validate access for
     * @param userId the unique identifier of the user requesting access
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    private void validateOutletAccess(Outlet outlet, UUID userId) {
        log.debug("Validating outlet access for user: {} on outlet: {}", userId, outlet.getId());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new BusinessException("AUTH_003", "User not found");
                });

        if (user.getRole() == UserRole.ADMIN) {
            log.debug("Admin access granted for user: {}", userId);
            return;
        }

        Merchant merchant = merchantRepository.findById(outlet.getMerchantId()).orElse(null);
        if (merchant != null && merchant.getOwnerUserId().equals(userId)) {
            log.debug("Merchant owner access granted for user: {}", userId);
            return;
        }

        if (user.getRole() == UserRole.OUTLET_USER && outlet.getId().equals(user.getOutletId())) {
            log.debug("Outlet user access granted for user: {}", userId);
            return;
        }

        log.warn("Access denied for user: {} on outlet: {} - role: {}", userId, outlet.getId(), user.getRole());
        throw new BusinessException("AUTH_003", "Not authorized to manage this outlet");
    }

    /**
     * Converts an Outlet entity to its DTO representation.
     *
     * <p>Includes operating hours and calculates current open status.</p>
     *
     * @param outlet the outlet entity to convert
     * @return the outlet as a DTO with all relevant information
     */
    private OutletDto toDto(Outlet outlet) {
        log.debug("Converting outlet entity to DTO: {}", outlet.getId());
        
        List<OutletHours> hours = outletHoursRepository.findByOutletId(outlet.getId());

        return OutletDto.builder()
                .id(outlet.getId())
                .merchantId(outlet.getMerchantId())
                .name(outlet.getName())
                .description(outlet.getDescription())
                .address(outlet.getAddress())
                .city(outlet.getCity())
                .postalCode(outlet.getPostalCode())
                .latitude(outlet.getLatitude())
                .longitude(outlet.getLongitude())
                .phone(outlet.getPhone())
                .email(outlet.getEmail())
                .imageUrl(outlet.getImageUrl())
                .status(outlet.getStatus())
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .totalRatings(outlet.getTotalRatings())
                .operatingHours(hours.stream()
                        .map(h -> OutletDto.OperatingHoursDto.builder()
                                .dayOfWeek(h.getDayOfWeek())
                                .openTime(h.getOpenTime())
                                .closeTime(h.getCloseTime())
                                .isClosed(h.getIsClosed())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(outlet.getCreatedAt())
                .build();
    }

    /**
     * Converts an Outlet entity to its list response DTO representation.
     *
     * <p>Includes merchant association data for list display.</p>
     *
     * @param outlet the outlet entity to convert
     * @return the outlet as a list response DTO with merchant information
     */
    private OutletListResponseDto toListDto(Outlet outlet) {
        log.debug("Converting outlet entity to list DTO: {}", outlet.getId());
        
        OutletListResponseDto.OutletListResponseDtoBuilder builder = OutletListResponseDto.builder()
                .id(outlet.getId())
                .merchantId(outlet.getMerchantId())
                .name(outlet.getName())
                .address(outlet.getAddress())
                .city(outlet.getCity())
                .postalCode(outlet.getPostalCode())
                .latitude(outlet.getLatitude())
                .longitude(outlet.getLongitude())
                .phone(outlet.getPhone())
                .imageUrl(outlet.getImageUrl())
                .status(outlet.getStatus())
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .totalRatings(outlet.getTotalRatings())
                .createdAt(outlet.getCreatedAt());

        // Add merchant association data
        if (outlet.getMerchantId() != null) {
            merchantRepository.findById(outlet.getMerchantId()).ifPresent(merchant -> {
                builder.merchantName(merchant.getName())
                       .merchantLogoUrl(merchant.getLogoUrl())
                       .merchantCategory(merchant.getCategory() != null ? merchant.getCategory().name() : null);
            });
        }

        return builder.build();
    }
}
