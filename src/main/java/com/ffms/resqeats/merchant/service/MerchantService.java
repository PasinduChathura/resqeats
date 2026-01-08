package com.ffms.resqeats.merchant.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.merchant.dto.CreateMerchantRequest;
import com.ffms.resqeats.merchant.dto.MerchantDto;
import com.ffms.resqeats.merchant.dto.MerchantFilterDto;
import com.ffms.resqeats.merchant.dto.MerchantListResponseDto;
import com.ffms.resqeats.merchant.dto.UpdateMerchantRequest;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.merchant.specification.MerchantSpecification;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class for managing merchant operations per SRS Section 6.4.
 *
 * <p>This service handles all merchant-related business logic including registration,
 * approval workflow, status management, and merchant data operations. It enforces
 * business rules for merchant lifecycle management.</p>
 *
 * <p>Business Rules:</p>
 * <ul>
 *   <li>BR-017: Only ADMIN can approve/reject merchants</li>
 *   <li>BR-018: Merchant must be approved before creating outlets</li>
 *   <li>BR-019: Merchant status changes require audit trail</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    /**
     * Registers a new merchant with pending approval status.
     *
     * <p>Creates a new merchant account and updates the owner's user role to MERCHANT.
     * The merchant will be in PENDING status until approved by an administrator.</p>
     *
     * @param request the merchant registration request containing merchant details
     * @param ownerId the UUID of the user who will own the merchant account
     * @return the created merchant as a DTO
     * @throws BusinessException with code MERCH_001 if user already owns a merchant
     * @throws BusinessException with code MERCH_002 if registration number already exists
     */
    @Transactional
    public MerchantDto registerMerchant(CreateMerchantRequest request, UUID ownerId) {
        log.info("Registering new merchant for owner: {}, name: {}", ownerId, request.getName());

        if (merchantRepository.existsByOwnerUserId(ownerId)) {
            log.warn("Merchant registration failed - user {} already owns a merchant account", ownerId);
            throw new BusinessException("MERCH_001", "User already owns a merchant account");
        }

        if (request.getRegistrationNo() != null && merchantRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            log.warn("Merchant registration failed - registration number {} already exists", request.getRegistrationNo());
            throw new BusinessException("MERCH_002", "Business registration number already exists");
        }

        log.debug("Building merchant entity for owner: {}", ownerId);
        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .legalName(request.getLegalName())
                .description(request.getDescription())
                .category(request.getCategory())
                .registrationNo(request.getRegistrationNo())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .website(request.getWebsite())
                .logoUrl(request.getLogoUrl())
                .ownerUserId(ownerId)
                .status(MerchantStatus.PENDING)
                .build();

        final Merchant savedMerchant = merchantRepository.save(merchant);
        log.debug("Merchant entity saved with ID: {}", savedMerchant.getId());

        userRepository.findById(ownerId).ifPresent(user -> {
            user.setRole(UserRole.MERCHANT);
            user.setMerchantId(savedMerchant.getId());
            userRepository.save(user);
            log.debug("Updated user {} role to MERCHANT", ownerId);
        });

        log.info("Merchant registered successfully - ID: {}, owner: {}, status: PENDING", 
                savedMerchant.getId(), ownerId);
        return toDto(savedMerchant);
    }

    /**
     * Approves a merchant registration (ADMIN only).
     *
     * <p>Changes the merchant status from PENDING to APPROVED and records
     * the approval timestamp and approving administrator. Per BR-017, only
     * administrators can perform this operation.</p>
     *
     * @param merchantId the UUID of the merchant to approve
     * @param adminId the UUID of the administrator performing the approval
     * @return the updated merchant as a DTO
     * @throws BusinessException with code MERCH_003 if merchant is not in PENDING status
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    @Transactional
    public MerchantDto approveMerchant(UUID merchantId, UUID adminId) {
        log.info("Approving merchant: {} by admin: {}", merchantId, adminId);
        Merchant merchant = getMerchantOrThrow(merchantId);

        if (merchant.getStatus() != MerchantStatus.PENDING) {
            log.warn("Cannot approve merchant {} - invalid status: {}", merchantId, merchant.getStatus());
            throw new BusinessException("MERCH_003", 
                    "Cannot approve merchant. Current status: " + merchant.getStatus());
        }

        merchant.setStatus(MerchantStatus.APPROVED);
        merchant.setApprovedAt(LocalDateTime.now());
        merchant.setApprovedBy(adminId);
        merchant = merchantRepository.save(merchant);

        log.info("Merchant approved successfully - ID: {}, admin: {}", merchantId, adminId);
        return toDto(merchant);
    }

    /**
     * Rejects a merchant registration (ADMIN only).
     *
     * <p>Changes the merchant status from PENDING to REJECTED and records
     * the rejection timestamp and reason. Per BR-017, only administrators
     * can perform this operation.</p>
     *
     * @param merchantId the UUID of the merchant to reject
     * @param adminId the UUID of the administrator performing the rejection
     * @param reason the reason for rejection
     * @return the updated merchant as a DTO
     * @throws BusinessException with code MERCH_003 if merchant is not in PENDING status
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    @Transactional
    public MerchantDto rejectMerchant(UUID merchantId, UUID adminId, String reason) {
        log.info("Rejecting merchant: {} by admin: {}", merchantId, adminId);
        Merchant merchant = getMerchantOrThrow(merchantId);

        if (merchant.getStatus() != MerchantStatus.PENDING) {
            log.warn("Cannot reject merchant {} - invalid status: {}", merchantId, merchant.getStatus());
            throw new BusinessException("MERCH_003", 
                    "Cannot reject merchant. Current status: " + merchant.getStatus());
        }

        merchant.setStatus(MerchantStatus.REJECTED);
        merchant.setRejectedAt(LocalDateTime.now());
        merchant.setRejectionReason(reason);
        merchant = merchantRepository.save(merchant);

        log.info("Merchant rejected successfully - ID: {}, admin: {}, reason: {}", 
                merchantId, adminId, reason);
        return toDto(merchant);
    }

    /**
     * Suspends an approved merchant (ADMIN only).
     *
     * <p>Changes the merchant status from APPROVED to SUSPENDED and records
     * the suspension timestamp and reason. Suspended merchants cannot operate
     * until reactivated.</p>
     *
     * @param merchantId the UUID of the merchant to suspend
     * @param adminId the UUID of the administrator performing the suspension
     * @param reason the reason for suspension
     * @return the updated merchant as a DTO
     * @throws BusinessException with code MERCH_003 if merchant is not in APPROVED status
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    @Transactional
    public MerchantDto suspendMerchant(UUID merchantId, UUID adminId, String reason) {
        log.info("Suspending merchant: {} by admin: {}", merchantId, adminId);
        Merchant merchant = getMerchantOrThrow(merchantId);

        if (merchant.getStatus() != MerchantStatus.APPROVED) {
            log.warn("Cannot suspend merchant {} - invalid status: {}", merchantId, merchant.getStatus());
            throw new BusinessException("MERCH_003", 
                    "Cannot suspend merchant. Current status: " + merchant.getStatus());
        }

        merchant.setStatus(MerchantStatus.SUSPENDED);
        merchant.setSuspendedAt(LocalDateTime.now());
        merchant.setSuspensionReason(reason);
        merchant = merchantRepository.save(merchant);

        log.info("Merchant suspended successfully - ID: {}, admin: {}, reason: {}", 
                merchantId, adminId, reason);
        return toDto(merchant);
    }

    /**
     * Updates merchant details.
     *
     * <p>Allows the merchant owner or an administrator to update merchant
     * information. Only non-null fields in the request will be updated.</p>
     *
     * @param merchantId the UUID of the merchant to update
     * @param request the update request containing fields to modify
     * @param userId the UUID of the user performing the update
     * @return the updated merchant as a DTO
     * @throws BusinessException with code AUTH_003 if user is not authorized
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    @Transactional
    public MerchantDto updateMerchant(UUID merchantId, UpdateMerchantRequest request, UUID userId) {
        log.info("Updating merchant: {} by user: {}", merchantId, userId);
        Merchant merchant = getMerchantOrThrow(merchantId);

        if (!merchant.getOwnerUserId().equals(userId)) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRole() != UserRole.ADMIN) {
                log.warn("Unauthorized update attempt on merchant {} by user {}", merchantId, userId);
                throw new BusinessException("AUTH_003", "Not authorized to update this merchant");
            }
            log.debug("Admin {} authorized to update merchant {}", userId, merchantId);
        }

        log.debug("Applying updates to merchant: {}", merchantId);
        if (request.getName() != null) merchant.setName(request.getName());
        if (request.getDescription() != null) merchant.setDescription(request.getDescription());
        if (request.getContactEmail() != null) merchant.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) merchant.setContactPhone(request.getContactPhone());
        if (request.getWebsite() != null) merchant.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null) merchant.setLogoUrl(request.getLogoUrl());

        merchant = merchantRepository.save(merchant);
        log.info("Merchant updated successfully - ID: {}", merchantId);
        return toDto(merchant);
    }

    /**
     * Retrieves a merchant by its unique identifier.
     *
     * @param merchantId the UUID of the merchant to retrieve
     * @return the merchant as a DTO
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    public MerchantDto getMerchant(UUID merchantId) {
        log.info("Retrieving merchant: {}", merchantId);
        MerchantDto merchantDto = toDto(getMerchantOrThrow(merchantId));
        log.debug("Merchant retrieved successfully - ID: {}", merchantId);
        return merchantDto;
    }

    /**
     * Retrieves a merchant by the owner's user ID.
     *
     * @param ownerId the UUID of the merchant owner
     * @return the merchant as a DTO
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    public MerchantDto getMerchantByOwner(UUID ownerId) {
        log.info("Retrieving merchant for owner: {}", ownerId);
        Merchant merchant = merchantRepository.findByOwnerUserId(ownerId)
                .orElseThrow(() -> {
                    log.warn("Merchant not found for owner: {}", ownerId);
                    return new BusinessException("MERCH_004", "Merchant not found");
                });
        log.debug("Merchant retrieved successfully for owner: {}", ownerId);
        return toDto(merchant);
    }

    /**
     * Retrieves all merchants with pagination (ADMIN only).
     *
     * @param pageable pagination parameters
     * @return a page of merchant list response DTOs
     */
    public Page<MerchantListResponseDto> getAllMerchants(Pageable pageable) {
        log.info("Retrieving all merchants - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<MerchantListResponseDto> merchants = merchantRepository.findAll(pageable).map(this::toListDto);
        log.info("Retrieved {} merchants", merchants.getTotalElements());
        return merchants;
    }

    /**
     * Retrieves all merchants with comprehensive filtering (ADMIN only).
     *
     * @param filter the filter criteria
     * @param pageable pagination parameters
     * @return a page of merchant list response DTOs
     */
    public Page<MerchantListResponseDto> getAllMerchants(MerchantFilterDto filter, Pageable pageable) {
        log.info("Retrieving all merchants with filter: {}, page: {}, size: {}", 
                filter, pageable.getPageNumber(), pageable.getPageSize());
        Page<MerchantListResponseDto> merchants = merchantRepository.findAll(MerchantSpecification.filterBy(filter), pageable)
                .map(this::toListDto);
        log.info("Retrieved {} merchants", merchants.getTotalElements());
        return merchants;
    }

    /**
     * Retrieves merchants filtered by status with pagination (ADMIN only).
     *
     * @param status the merchant status to filter by
     * @param pageable pagination parameters
     * @return a page of merchant list response DTOs matching the status
     */
    public Page<MerchantListResponseDto> getMerchantsByStatus(MerchantStatus status, Pageable pageable) {
        log.info("Retrieving merchants by status: {} - page: {}, size: {}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        Page<MerchantListResponseDto> merchants = merchantRepository.findByStatus(status, pageable).map(this::toListDto);
        log.info("Retrieved {} merchants with status: {}", merchants.getTotalElements(), status);
        return merchants;
    }

    /**
     * Searches merchants by name with pagination.
     *
     * @param query the search query for merchant name
     * @param pageable pagination parameters
     * @return a page of merchant list response DTOs matching the search query
     */
    public Page<MerchantListResponseDto> searchMerchants(String query, Pageable pageable) {
        log.info("Searching merchants with query: '{}' - page: {}, size: {}", 
                query, pageable.getPageNumber(), pageable.getPageSize());
        Page<MerchantListResponseDto> merchants = merchantRepository.findByNameContainingIgnoreCase(query, pageable)
                .map(this::toListDto);
        log.info("Found {} merchants matching query: '{}'", merchants.getTotalElements(), query);
        return merchants;
    }

    /**
     * Checks if a merchant is approved.
     *
     * <p>Per BR-018, this check is required before creating outlets
     * for a merchant.</p>
     *
     * @param merchantId the UUID of the merchant to check
     * @return true if the merchant exists and is approved, false otherwise
     */
    public boolean isMerchantApproved(UUID merchantId) {
        log.debug("Checking if merchant is approved: {}", merchantId);
        boolean approved = merchantRepository.findById(merchantId)
                .map(m -> m.getStatus() == MerchantStatus.APPROVED)
                .orElse(false);
        log.debug("Merchant {} approved status: {}", merchantId, approved);
        return approved;
    }

    /**
     * Retrieves a merchant entity by ID or throws an exception.
     *
     * @param merchantId the UUID of the merchant to retrieve
     * @return the merchant entity
     * @throws BusinessException with code MERCH_004 if merchant not found
     */
    private Merchant getMerchantOrThrow(UUID merchantId) {
        log.debug("Looking up merchant: {}", merchantId);
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> {
                    log.error("Merchant not found: {}", merchantId);
                    return new BusinessException("MERCH_004", "Merchant not found");
                });
    }

    /**
     * Converts a Merchant entity to a MerchantDto.
     *
     * @param merchant the merchant entity to convert
     * @return the merchant DTO
     */
    private MerchantDto toDto(Merchant merchant) {
        return MerchantDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .legalName(merchant.getLegalName())
                .description(merchant.getDescription())
                .category(merchant.getCategory())
                .registrationNo(merchant.getRegistrationNo())
                .contactEmail(merchant.getContactEmail())
                .contactPhone(merchant.getContactPhone())
                .website(merchant.getWebsite())
                .logoUrl(merchant.getLogoUrl())
                .status(merchant.getStatus())
                .approvedAt(merchant.getApprovedAt())
                .createdAt(merchant.getCreatedAt())
                .build();
    }

    /**
     * Converts a Merchant entity to a MerchantListResponseDto for list display.
     *
     * @param merchant the merchant entity to convert
     * @return the merchant list response DTO
     */
    private MerchantListResponseDto toListDto(Merchant merchant) {
        return MerchantListResponseDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .legalName(merchant.getLegalName())
                .category(merchant.getCategory())
                .logoUrl(merchant.getLogoUrl())
                .contactEmail(merchant.getContactEmail())
                .contactPhone(merchant.getContactPhone())
                .status(merchant.getStatus())
                .approvedAt(merchant.getApprovedAt())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}
