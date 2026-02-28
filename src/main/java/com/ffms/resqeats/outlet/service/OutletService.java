package com.ffms.resqeats.outlet.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.geo.service.GeoService;
import com.ffms.resqeats.item.repository.OutletItemRepository;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.outlet.dto.CreateOutletRequest;
import com.ffms.resqeats.outlet.dto.OutletFilterDto;
import com.ffms.resqeats.outlet.dto.UpdateOutletRequest;
import com.ffms.resqeats.outlet.dto.admin.*;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import com.ffms.resqeats.outlet.dto.customer.OutletCustomerDTO;
import com.ffms.resqeats.outlet.dto.merchant.OutletMerchantDetailDTO;
import com.ffms.resqeats.outlet.dto.merchant.OutletMerchantListDTO;
import com.ffms.resqeats.outlet.dto.outlet.OutletSelfDto;
import com.ffms.resqeats.outlet.dto.outlet.UpdateMyOutletRequest;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.entity.OutletHours;
import com.ffms.resqeats.outlet.enums.OutletAvailabilityStatus;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import com.ffms.resqeats.outlet.repository.OutletHoursRepository;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.outlet.specification.OutletSpecification;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 *   <li>BR-019: ADMIN can create outlets for any merchant, MERCHANT only for their own</li>
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
    private final OutletItemRepository outletItemRepository;
    private final GeoService geoService;
    private final WebSocketService webSocketService;

    // =====================
    // Admin Commands
    // =====================

    /**
     * Creates an outlet for any merchant (Admin only).
     *
     * @param request the admin create outlet request
     * @return the created outlet DTO
     */
    @Transactional
    public OutletAdminDetailDTO createOutletAdmin(AdminCreateOutletRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin creating outlet for merchant: {} by admin: {}", request.getMerchantId(), context.getUserId());

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BusinessException("MERCH_004", "Merchant not found"));

        if (merchant.getStatus() != MerchantStatus.APPROVED) {
            throw new BusinessException("OUTLET_001", "Merchant must be approved before creating outlets");
        }

        Outlet outlet = Outlet.builder()
                .merchantId(request.getMerchantId())
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

        if (request.getOperatingHours() != null) {
            for (OperatingHoursDto hours : request.getOperatingHours()) {
                OutletHours outletHours = OutletHours.builder()
                        .outletId(outlet.getId())
                        .dayOfWeek(mapDayOfWeek(hours.getDayOfWeek()))
                        .openTime(hours.getOpenTime())
                        .closeTime(hours.getCloseTime())
                        .isClosed(hours.getIsClosed() != null ? hours.getIsClosed() : false)
                        .build();
                outletHoursRepository.save(outletHours);
            }
        }

        geoService.addOutletToGeoIndex(outlet);
        log.info("Admin created outlet: {} for merchant: {}", outlet.getId(), request.getMerchantId());
        return toDtoAdminDetail(outlet);
    }

    /**
     * Updates an outlet (Admin only - no ownership check).
     */
    @Transactional
    public OutletAdminDetailDTO updateOutletAdmin(Long outletId, AdminUpdateOutletRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin updating outlet: {} by admin: {}", outletId, context.getUserId());
        Outlet outlet = getOutletOrThrow(outletId);

        applyOutletUpdates(outlet, request);
        outlet = outletRepository.save(outlet);

        if (request.getOperatingHours() != null) {
            replaceOperatingHours(outletId, request.getOperatingHours());
        }
        log.info("Admin updated outlet: {}", outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Approves an outlet (Admin only).
     */
    @Transactional
    public OutletAdminDetailDTO approveOutlet(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin approving outlet: {} by admin: {}", outletId, context.getUserId());
        Outlet outlet = getOutletOrThrow(outletId);

        if (outlet.getStatus() != OutletStatus.PENDING_APPROVAL) {
            throw new BusinessException("OUTLET_005", "Outlet is not pending approval");
        }

        outlet.setStatus(OutletStatus.ACTIVE);
        outlet.setAvailabilityStatus(OutletAvailabilityStatus.OPEN);
        outlet = outletRepository.save(outlet);
        log.info("Outlet approved: {}", outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Suspends an outlet (Admin only).
     */
    @Transactional
    public OutletAdminDetailDTO suspendOutlet(Long outletId, String reason) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin suspending outlet: {} by admin: {}", outletId, context.getUserId());
        Outlet outlet = getOutletOrThrow(outletId);

        outlet.setStatus(OutletStatus.SUSPENDED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.info("Outlet suspended: {} - reason: {}", outletId, reason);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Activates an outlet (Admin only - bypasses hours check).
     */
    @Transactional
    public OutletAdminDetailDTO activateOutletAdmin(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin activating outlet: {} by admin: {}", outletId, context.getUserId());
        Outlet outlet = getOutletOrThrow(outletId);

        outlet.setStatus(OutletStatus.ACTIVE);
        outlet.setAvailabilityStatus(OutletAvailabilityStatus.OPEN);
        outlet = outletRepository.save(outlet);
        log.info("Admin activated outlet: {}", outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Deactivates an outlet (Admin only).
     */
    @Transactional
    public OutletAdminDetailDTO deactivateOutletAdmin(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin deactivating outlet: {} by admin: {}", outletId, context.getUserId());
        Outlet outlet = getOutletOrThrow(outletId);

        outlet.setStatus(OutletStatus.DISABLED);
        outlet.setAvailabilityStatus(OutletAvailabilityStatus.CLOSED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.info("Admin deactivated outlet: {}", outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Manually closes an outlet (Admin only) - stops accepting new orders.
     *
     * <p>This does not change the outlet lifecycle status. It toggles the operational
     * availability flag used alongside operating hours to compute {@code is_open}.</p>
     */
    @Transactional
    public OutletAdminDetailDTO closeOutletAdmin(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin closing outlet: {} by admin: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        if (outlet.getStatus() != OutletStatus.ACTIVE) {
            throw new BusinessException("OUTLET_003", "Only active outlets can be closed");
        }

        outlet.setAvailabilityStatus(OutletAvailabilityStatus.CLOSED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.info("Admin closed outlet: {}", outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Manually opens an outlet (Admin only) - resumes accepting new orders.
     */
    @Transactional
    public OutletAdminDetailDTO openOutletAdmin(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin opening outlet: {} by admin: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        if (outlet.getStatus() != OutletStatus.ACTIVE) {
            throw new BusinessException("OUTLET_003", "Only active outlets can be opened");
        }

        outlet.setAvailabilityStatus(OutletAvailabilityStatus.OPEN);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, isCurrentlyOpen(outletId));
        log.info("Admin opened outlet: {}", outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Get outlet full details for merchant.
     */
    public OutletMerchantDetailDTO getOutletMerchantDetail(Long outletId) {
        log.info("Merchant retrieving outlet details: {}", outletId);
        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);
        return toDtoMerchantDetail(outlet);
    }

    /**
     * Get outlet full details for admin.
     */
    public OutletAdminDetailDTO getOutletAdminDetail(Long outletId) {
        log.info("Admin retrieving outlet details: {}", outletId);
        Outlet outlet = getOutletOrThrow(outletId);
        return toDtoAdminDetail(outlet);
    }

    /**
     * Deletes an outlet.
     */
    @Transactional
    public void deleteOutlet(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Deleting outlet: {} by user: {}", outletId, context.getUserId());
        Outlet outlet = getOutletOrThrow(outletId);

        // Admin can delete any outlet, merchant/outlet users can only delete within their scope
        if (!context.hasGlobalAccess()) {
            if (context.getRole() == UserRole.MERCHANT_USER) {
                if (context.getMerchantId() == null || !outlet.getMerchantId().equals(context.getMerchantId())) {
                    throw new BusinessException("AUTH_003", "Not authorized to delete this outlet");
                }
            } else if (context.getRole() == UserRole.OUTLET_USER) {
                if (context.getOutletId() == null || !outlet.getId().equals(context.getOutletId())) {
                    throw new BusinessException("AUTH_003", "Not authorized to delete this outlet");
                }
            } else {
                throw new BusinessException("AUTH_003", "Not authorized to delete this outlet");
            }
        }

        geoService.removeOutletFromGeoIndex(outletId);
        outletHoursRepository.deleteByOutletId(outletId);
        outletRepository.delete(outlet);
        log.info("Outlet deleted: {}", outletId);
    }

    /**
     * Filter outlets for admin (full data).
     */
    public Page<OutletAdminListDTO> filterOutletsAdmin(OutletFilterDto filter, Pageable pageable) {
        log.info("Admin filtering outlets with filter: {}", filter);
        return outletRepository.findAll(OutletSpecification.filterBy(filter), pageable)
                .map(this::toAdminListDto);
    }

    /**
     * Lookup outlets for dropdown (Admin).
     */
    public Stream<OutletLookupDto> lookupOutlets(String query, Long merchantId) {
        log.info("Admin lookup outlets - query: {}, merchantId: {}", query, merchantId);
        List<Outlet> outlets = outletRepository.searchOutlets(merchantId, query);
        return outlets.stream().map(this::toLookupDto);
    }

    // =====================
    // Merchant Commands
    // =====================

    /**
     * Creates an outlet for the current user's merchant.
     */
    @Transactional
    public OutletMerchantDetailDTO createOutletForCurrentUser(CreateOutletRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Merchant creating outlet by user: {}", context.getUserId());

        if (context.getMerchantId() == null) {
            throw new BusinessException("MERCH_004", "User is not assigned to any merchant");
        }

        Merchant merchant = merchantRepository.findById(context.getMerchantId())
                .orElseThrow(() -> new BusinessException("MERCH_004", "Merchant not found"));

        if (merchant.getStatus() != MerchantStatus.APPROVED) {
            throw new BusinessException("OUTLET_001", "Merchant must be approved before creating outlets");
        }

        Outlet outlet = Outlet.builder()
                .merchantId(context.getMerchantId())
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

        if (request.getOperatingHours() != null) {
            for (OperatingHoursDto hours : request.getOperatingHours()) {
                OutletHours outletHours = OutletHours.builder()
                        .outletId(outlet.getId())
                        .dayOfWeek(mapDayOfWeek(hours.getDayOfWeek()))
                        .openTime(hours.getOpenTime())
                        .closeTime(hours.getCloseTime())
                        .isClosed(hours.getIsClosed() != null ? hours.getIsClosed() : false)
                        .build();
                outletHoursRepository.save(outletHours);
            }
        }

        geoService.addOutletToGeoIndex(outlet);
        log.info("Merchant created outlet: {} for merchant: {}", outlet.getId(), context.getMerchantId());
        return toDtoMerchantDetail(outlet);
    }

    /**
     * Paginated version: Gets outlets for the current user's merchant.
     */
    public Page<OutletMerchantListDTO> getOutletsByCurrentUser(Pageable pageable) {
        var context = SecurityContextHolder.getContext();
        log.info("Getting paged outlets for user: {} page: {}", context.getUserId(), pageable);

        if (context.getMerchantId() == null) {
            throw new BusinessException("MERCH_004", "User is not assigned to any merchant");
        }

        Page<Outlet> page = outletRepository.findByMerchantId(context.getMerchantId(), pageable);
        return page.map(this::toDtoMerchantList);
    }

    /**
     * Lookup outlets for the current user's merchant.
     */
    public Stream<OutletLookupDto> lookupOutletsByCurrentUser(String query) {
        var context = SecurityContextHolder.getContext();
        log.info("Lookup outlets for user: {}, query: {}", context.getUserId(), query);

        if (context.getMerchantId() == null) {
            throw new BusinessException("MERCH_004", "User is not assigned to any merchant");
        }

        List<Outlet> outlets;
        if (query != null && !query.isBlank()) {
            outlets = outletRepository.findByMerchantIdAndNameContainingIgnoreCase(context.getMerchantId(), query);
        } else {
            outlets = outletRepository.findByMerchantId(context.getMerchantId());
        }

        return outlets.stream().map(this::toLookupDto);
    }

    // =====================
    // Outlet User Self-Service
    // =====================

    /**
     * Gets the current outlet user's assigned outlet.
     *
     * @return the outlet as a self DTO
     * @throws BusinessException with code OUTLET_004 if user is not assigned to an outlet
     */
    public OutletSelfDto getMyOutlet() {
        var context = SecurityContextHolder.getContext();
        log.info("Outlet user getting their outlet: {}", context.getUserId());

        if (context.getOutletId() == null) {
            throw new BusinessException("OUTLET_004", "User is not assigned to any outlet");
        }

        Outlet outlet = getOutletOrThrow(context.getOutletId());
        return toDtoSelf(outlet);
    }

    /**
     * Updates the current outlet user's assigned outlet with limited fields.
     * Outlet users can only update: description, phone, email, image_url, operating_hours.
     *
     * @param request the update request with limited fields
     * @return the updated outlet as a self DTO
     * @throws BusinessException with code OUTLET_004 if user is not assigned to an outlet
     */
    @Transactional
    public OutletSelfDto updateMyOutlet(UpdateMyOutletRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Outlet user updating their outlet: {}", context.getUserId());

        if (context.getOutletId() == null) {
            throw new BusinessException("OUTLET_004", "User is not assigned to any outlet");
        }

        Outlet outlet = getOutletOrThrow(context.getOutletId());

        // Apply only the allowed updates for outlet users
        if (request.getDescription() != null) outlet.setDescription(request.getDescription());
        if (request.getPhone() != null) outlet.setPhone(request.getPhone());
        if (request.getEmail() != null) outlet.setEmail(request.getEmail());
        if (request.getImageUrl() != null) outlet.setImageUrl(request.getImageUrl());

        outlet = outletRepository.save(outlet);

        // Update operating hours if provided
        if (request.getOperatingHours() != null) {
            replaceOperatingHours(outlet.getId(), request.getOperatingHours());
        }

        log.info("Outlet user updated outlet: {}", outlet.getId());
        return toDtoSelf(outlet);
    }

    // =====================
    // Public Queries
    // =====================

    /**
     * Gets outlet details for customers (public - limited data).
     */
    public OutletCustomerDTO getOutletCustomerById(Long outletId) {
        log.info("Getting outlet customer view: {}", outletId);
        Outlet outlet = getOutletOrThrow(outletId);

        // Only return active outlets for public access
        if (outlet.getStatus() != OutletStatus.ACTIVE) {
            throw new BusinessException("OUTLET_004", "Outlet not found");
        }

        return toDtoCustomer(outlet);
    }

    /**
     * Search outlets for public (limited data, only active outlets).
     */
    public Page<OutletCustomerDTO> searchOutletsPublic(String query, String city, Pageable pageable) {
        log.info("Public search outlets - query: {}, city: {}", query, city);

        OutletFilterDto filter = OutletFilterDto.builder()
                .status(OutletStatus.ACTIVE)
                .search(query)
                .city(city)
                .build();

        return outletRepository.findAll(OutletSpecification.filterBy(filter), pageable)
                .map(this::toDtoCustomer);
    }

    /**
     * Updates an existing outlet's information.
     *
     * <p>Allows partial updates - only non-null fields in the request will be updated.
     * If geo-coordinates are updated, the outlet's position in the geo-index is also refreshed.</p>
     *
     * @param outletId the unique identifier of the outlet to update
     * @param request  the update request containing fields to modify
     * @return the updated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletMerchantDetailDTO updateOutlet(Long outletId, UpdateOutletRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Updating outlet: {} by user: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);

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

        if (request.getOperatingHours() != null) {
            replaceOperatingHours(outletId, request.getOperatingHours());
        }
        log.info("Outlet updated successfully: {}", outletId);
        return toDtoMerchantDetail(outlet);
    }

    /**
     * Sets or updates the operating hours for an outlet.
     *
     * <p>Replaces all existing operating hours with the provided schedule.
     * Operating hours must be set before an outlet can be activated per BR-021.</p>
     *
     * @param outletId     the unique identifier of the outlet
     * @param hoursRequest the list of operating hours for each day of the week
     * @return the updated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletMerchantDetailDTO setOperatingHours(Long outletId, List<OperatingHoursDto> hoursRequest) {
        var context = SecurityContextHolder.getContext();
        log.info("Setting operating hours for outlet: {} by user: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);

        replaceOperatingHours(outletId, hoursRequest);

        log.info("Operating hours set successfully for outlet: {}", outletId);
        return toDtoMerchantDetail(outlet);
    }

    private void replaceOperatingHours(Long outletId, List<OperatingHoursDto> hoursRequest) {
        if (hoursRequest == null) return;

        log.debug("Replacing operating hours for outlet: {} ({} entries)", outletId, hoursRequest.size());
        outletHoursRepository.deleteByOutletId(outletId);

        List<OutletHours> newHours = hoursRequest.stream()
                .map(hours -> OutletHours.builder()
                        .outletId(outletId)
                        .dayOfWeek(mapDayOfWeek(hours.getDayOfWeek()))
                        .openTime(hours.getOpenTime())
                        .closeTime(hours.getCloseTime())
                        .isClosed(hours.getIsClosed() != null ? hours.getIsClosed() : false)
                        .build())
                .collect(Collectors.toList());

        outletHoursRepository.saveAll(newHours);
    }

    /**
     * Activates an outlet, making it visible to customers.
     *
     * <p>Changes outlet status to ACTIVE. Per BR-021, operating hours must be
     * configured before activation is allowed.</p>
     *
     * @param outletId the unique identifier of the outlet to activate
     * @return the activated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     * @throws BusinessException with code OUTLET_002 if operating hours are not set
     */
    @Transactional
    public OutletMerchantDetailDTO activateOutlet(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Activating outlet: {} by user: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);

        List<OutletHours> hours = outletHoursRepository.findByOutletId(outletId);
        if (hours.isEmpty()) {
            log.warn("Activation failed for outlet: {} - operating hours not set", outletId);
            throw new BusinessException("OUTLET_002", "Set operating hours before activating outlet");
        }

        outlet.setStatus(OutletStatus.ACTIVE);
        outlet.setAvailabilityStatus(OutletAvailabilityStatus.OPEN);
        outlet = outletRepository.save(outlet);

        log.info("Outlet activated successfully: {}", outletId);
        return toDtoMerchantDetail(outlet);
    }

    /**
     * Deactivates an outlet, removing it from customer visibility.
     *
     * <p>Changes outlet status to INACTIVE and broadcasts the status change
     * via WebSocket to notify connected clients.</p>
     *
     * @param outletId the unique identifier of the outlet to deactivate
     * @return the deactivated outlet as a DTO
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    @Transactional
    public OutletMerchantDetailDTO deactivateOutlet(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Deactivating outlet: {} by user: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);

        outlet.setStatus(OutletStatus.DISABLED);
        outlet.setAvailabilityStatus(OutletAvailabilityStatus.CLOSED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.debug("WebSocket notification sent for outlet deactivation: {}", outletId);

        log.info("Outlet deactivated successfully: {}", outletId);
        return toDtoMerchantDetail(outlet);
    }

    /**
     * Manually closes an outlet (stop accepting new orders).
     *
     * <p>This does not change the outlet lifecycle status. It toggles the operational
     * availability flag used alongside operating hours to compute {@code is_open}.</p>
     */
    @Transactional
    public OutletMerchantDetailDTO temporarilyCloseOutlet(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Temporarily closing outlet: {} by user: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);

        if (outlet.getStatus() != OutletStatus.ACTIVE) {
            throw new BusinessException("OUTLET_003", "Only active outlets can be closed");
        }

        outlet.setAvailabilityStatus(OutletAvailabilityStatus.CLOSED);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, false);
        log.debug("WebSocket notification sent for outlet temporary closure: {}", outletId);

        log.info("Outlet closed successfully: {}", outletId);
        return toDtoMerchantDetail(outlet);
    }

    /**
     * Manually opens an outlet (resume accepting new orders).
     */
    @Transactional
    public OutletMerchantDetailDTO reopenOutlet(Long outletId) {
        var context = SecurityContextHolder.getContext();
        log.info("Reopening outlet: {} by user: {}", outletId, context.getUserId());

        Outlet outlet = getOutletOrThrow(outletId);
        validateOutletAccess(outlet);

        if (outlet.getStatus() != OutletStatus.ACTIVE) {
            throw new BusinessException("OUTLET_003", "Only active outlets can be opened");
        }

        outlet.setAvailabilityStatus(OutletAvailabilityStatus.OPEN);
        outlet = outletRepository.save(outlet);

        webSocketService.broadcastOutletStatusChange(outletId, isCurrentlyOpen(outletId));
        log.debug("WebSocket notification sent for outlet reopen: {}", outletId);

        log.info("Outlet opened successfully: {}", outletId);
        return toDtoMerchantDetail(outlet);
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
    public boolean isCurrentlyOpen(Long outletId) {
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

        if (outlet.getAvailabilityStatus() != OutletAvailabilityStatus.OPEN) {
            log.debug("Outlet {} is manually closed, availability status: {}", outletId, outlet.getAvailabilityStatus());
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
     * Finds outlets near a specified geographic location.
     *
     * <p>Validates the provided coordinates and radius before performing
     * the geo-spatial search. Returns outlets sorted by distance.</p>
     *
     * @param latitude  the latitude of the search center (-90 to 90)
     * @param longitude the longitude of the search center (-180 to 180)
     * @param radiusKm  the search radius in kilometers (0 to 1000), nullable
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
     * Retrieves an outlet entity by ID or throws an exception if not found.
     *
     * @param outletId the unique identifier of the outlet
     * @return the outlet entity
     * @throws BusinessException with code OUTLET_004 if outlet is not found
     */
    private Outlet getOutletOrThrow(Long outletId) {
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
     * @throws BusinessException with code AUTH_003 if user is not authorized
     */
    private void validateOutletAccess(Outlet outlet) {
        var context = SecurityContextHolder.getContext();
        log.debug("Validating outlet access for user: {} on outlet: {}", context.getUserId(), outlet.getId());

        if (context.hasGlobalAccess() || context.isAdmin()) {
            return;
        }

        if (context.getRole() == UserRole.MERCHANT_USER) {
            if (context.getMerchantId() != null && outlet.getMerchantId().equals(context.getMerchantId())) {
                return;
            }
        }

        if (context.getRole() == UserRole.OUTLET_USER) {
            if (context.getOutletId() != null && outlet.getId().equals(context.getOutletId())) {
                return;
            }
        }

        throw new BusinessException("AUTH_003", "Not authorized to manage this outlet");
    }

    /**
     * Apply updates to outlet from request.
     */
    private void applyOutletUpdates(Outlet outlet, AdminUpdateOutletRequest request) {
        if (request.getName() != null) outlet.setName(request.getName());
        if (request.getDescription() != null) outlet.setDescription(request.getDescription());
        if (request.getAddress() != null) outlet.setAddress(request.getAddress());
        if (request.getCity() != null) outlet.setCity(request.getCity());
        if (request.getPostalCode() != null) outlet.setPostalCode(request.getPostalCode());
        if (request.getPhone() != null) outlet.setPhone(request.getPhone());
        if (request.getEmail() != null) outlet.setEmail(request.getEmail());
        if (request.getImageUrl() != null) outlet.setImageUrl(request.getImageUrl());
        if (request.getMerchantId() != null) outlet.setMerchantId(request.getMerchantId());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            outlet.setLatitude(request.getLatitude());
            outlet.setLongitude(request.getLongitude());
            geoService.addOutletToGeoIndex(outlet);
        }
    }

    /**
     * Map `java.time.DayOfWeek` (MONDAY..SUNDAY) to stored integer 0=Sunday..6=Saturday
     */
    private Integer mapDayOfWeek(DayOfWeek dow) {
        if (dow == null) return null;
        // DayOfWeek.getValue(): 1=Monday .. 7=Sunday. Map Sunday(7)->0, others keep value.
        return dow.getValue() % 7;
    }

    private List<OperatingHoursDto> toOperatingHoursDtos(Long outletId) {
        List<OutletHours> hours = outletHoursRepository.findByOutletId(outletId);
        return hours.stream()
                .map(h -> OperatingHoursDto.builder()
                        .dayOfWeek(mapIntToDayOfWeek(h.getDayOfWeek()))
                        .openTime(h.getOpenTime())
                        .closeTime(h.getCloseTime())
                        .isClosed(h.getIsClosed())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map stored integer 0=Sunday..6=Saturday back to java.time.DayOfWeek
     */
    private DayOfWeek mapIntToDayOfWeek(Integer stored) {
        if (stored == null) return null;
        if (stored == 0) return DayOfWeek.SUNDAY;
        return DayOfWeek.of(stored);
    }

    private long getItemCount(Long outletId) {
        return outletItemRepository.countByOutletId(outletId);
    }

    private OutletAdminListDTO toAdminListDto(Outlet outlet) {
        Merchant merchant = merchantRepository.findById(outlet.getMerchantId()).orElse(null);
        return OutletAdminListDTO.builder()
                .id(outlet.getId())
                .name(outlet.getName())
                .phone(outlet.getPhone())
                .merchantName(merchant != null ? merchant.getName() : null)
                .merchantLogoUrl(merchant != null ? merchant.getLogoUrl() : null)
                .status(outlet.getStatus())
                .availabilityStatus(outlet.getAvailabilityStatus())
                .address(outlet.getAddress())
                .itemCount(getItemCount(outlet.getId()))
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .build();
    }

    private OutletAdminDetailDTO toDtoAdminDetail(Outlet outlet) {
        return OutletAdminDetailDTO.builder()
                .id(outlet.getId())
                .name(outlet.getName())
                .description(outlet.getDescription())
                .address(outlet.getAddress())
                .city(outlet.getCity())
                .latitude(outlet.getLatitude())
                .longitude(outlet.getLongitude())
                .phone(outlet.getPhone())
                .status(outlet.getStatus())
                .availabilityStatus(outlet.getAvailabilityStatus())
                .merchantId(outlet.getMerchantId())
                .postalCode(outlet.getPostalCode())
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .totalRatings(outlet.getTotalRatings())
                .operatingHours(toOperatingHoursDtos(outlet.getId()))
                .build();
    }

    private OutletMerchantListDTO toDtoMerchantList(Outlet outlet) {
        return OutletMerchantListDTO.builder()
                .id(outlet.getId())
                .name(outlet.getName())
                .phone(outlet.getPhone())
                .status(outlet.getStatus())
                .availabilityStatus(outlet.getAvailabilityStatus())
                .address(outlet.getAddress())
                .itemCount(getItemCount(outlet.getId()))
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .build();
    }

    private OutletMerchantDetailDTO toDtoMerchantDetail(Outlet outlet) {
        return OutletMerchantDetailDTO.builder()
                .id(outlet.getId())
                .name(outlet.getName())
                .description(outlet.getDescription())
                .address(outlet.getAddress())
                .city(outlet.getCity())
                .latitude(outlet.getLatitude())
                .longitude(outlet.getLongitude())
                .phone(outlet.getPhone())
                .status(outlet.getStatus())
                .availabilityStatus(outlet.getAvailabilityStatus())
                .postalCode(outlet.getPostalCode())
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .totalRatings(outlet.getTotalRatings())
                .operatingHours(toOperatingHoursDtos(outlet.getId()))
                .build();
    }

    private OutletCustomerDTO toDtoCustomer(Outlet outlet) {
        Merchant merchant = merchantRepository.findById(outlet.getMerchantId()).orElse(null);
        return OutletCustomerDTO.builder()
                .name(outlet.getName())
                .phone(outlet.getPhone())
                .address(outlet.getAddress())
                .merchantName(merchant != null ? merchant.getName() : null)
                .merchantLogoUrl(merchant != null ? merchant.getLogoUrl() : null)
                .availabilityStatus(outlet.getAvailabilityStatus())
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .build();
    }

    private OutletSelfDto toDtoSelf(Outlet outlet) {
        return OutletSelfDto.builder()
                .id(outlet.getId())
                .name(outlet.getName())
                .description(outlet.getDescription())
                .address(outlet.getAddress())
                .city(outlet.getCity())
                .postalCode(outlet.getPostalCode())
                .latitude(outlet.getLatitude())
                .longitude(outlet.getLongitude())
                .phone(outlet.getPhone())
                .email(outlet.getEmail())
                .status(outlet.getStatus())
                .availabilityStatus(outlet.getAvailabilityStatus())
                .imageUrl(outlet.getImageUrl())
                .isOpen(isCurrentlyOpen(outlet.getId()))
                .averageRating(outlet.getAverageRating())
                .totalRatings(outlet.getTotalRatings())
                .operatingHours(toOperatingHoursDtos(outlet.getId()))
                .build();
    }

    /**
     * Converts an Outlet entity to its lookup DTO representation.
     */
    private OutletLookupDto toLookupDto(Outlet outlet) {
        log.debug("Converting outlet entity to lookup DTO: {}", outlet.getId());
        return OutletLookupDto.builder()
                .id(outlet.getId())
                .name(outlet.getName())
                .build();
    }

}
