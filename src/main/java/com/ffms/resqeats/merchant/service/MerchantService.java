package com.ffms.resqeats.merchant.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.merchant.dto.admin.AdminCreateMerchantRequest;
import com.ffms.resqeats.merchant.dto.admin.AdminUpdateMerchantRequest;
import com.ffms.resqeats.merchant.dto.admin.MerchantAdminDto;
import com.ffms.resqeats.merchant.dto.admin.MerchantAdminListDto;
import com.ffms.resqeats.merchant.dto.MerchantFilterDto;
import com.ffms.resqeats.merchant.dto.admin.MerchantLookupDto;
import com.ffms.resqeats.merchant.dto.customer.MerchantCustomerDto;
import com.ffms.resqeats.merchant.dto.customer.MerchantCustomerListDto;
import com.ffms.resqeats.merchant.dto.merchant.MerchantSelfDto;
import com.ffms.resqeats.merchant.dto.merchant.UpdateMyMerchantRequest;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.merchant.specification.MerchantSpecification;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Service class for managing merchant operations per SRS Section 6.4.
 *
 * <p>This service handles all merchant-related business logic including creation,
 * approval workflow, status management, and merchant data operations. It enforces
 * business rules for merchant lifecycle management.</p>
 *
 * <p>Security Context: Uses SecurityContextHolder for user identity and authorization.
 * No need to pass userId through method parameters.</p>
 *
 * <p>Business Rules:</p>
 * <ul>
 *   <li>BR-016: Only ADMIN can create merchants</li>
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

    // =====================
    // Admin Commands
    // =====================

    /**
     * Creates a new merchant (Admin only).
     */
    @Transactional
    public MerchantAdminDto createMerchant(AdminCreateMerchantRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin creating merchant: {} by user: {}", request.getName(), context.getUserId());

        if (request.getRegistrationNo() != null &&
                merchantRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new BusinessException("MERCH_002", "Business registration number already exists");
        }

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
                .status(MerchantStatus.PENDING)
                .build();

        return mapToAdminDto(merchantRepository.save(merchant));
    }

    /**
     * Approves a pending merchant (Admin only).
     */
    @Transactional
    public MerchantAdminDto approveMerchant(Long merchantId) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin approving merchant: {} by user: {}", merchantId, context.getUserId());

        Merchant merchant = findMerchantOrThrow(merchantId);

        if (merchant.getStatus() != MerchantStatus.PENDING) {
            throw new BusinessException("MERCH_003",
                    "Cannot approve merchant. Current status: " + merchant.getStatus());
        }

        merchant.setStatus(MerchantStatus.APPROVED);
        merchant.setApprovedAt(LocalDateTime.now());
        merchant.setApprovedBy(context.getUserId());

        log.info("Merchant approved: {}", merchantId);
        return mapToAdminDto(merchantRepository.save(merchant));
    }

    /**
     * Rejects a pending merchant (Admin only).
     */
    @Transactional
    public MerchantAdminDto rejectMerchant(Long merchantId, String reason) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin rejecting merchant: {} by user: {}", merchantId, context.getUserId());

        Merchant merchant = findMerchantOrThrow(merchantId);

        if (merchant.getStatus() != MerchantStatus.PENDING) {
            throw new BusinessException("MERCH_003",
                    "Cannot reject merchant. Current status: " + merchant.getStatus());
        }

        merchant.setStatus(MerchantStatus.REJECTED);
        merchant.setRejectedAt(LocalDateTime.now());
        merchant.setRejectionReason(reason);

        log.info("Merchant rejected: {} - reason: {}", merchantId, reason);
        return mapToAdminDto(merchantRepository.save(merchant));
    }

    /**
     * Suspends an approved merchant (Admin only).
     */
    @Transactional
    public MerchantAdminDto suspendMerchant(Long merchantId, String reason) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin suspending merchant: {} by user: {}", merchantId, context.getUserId());

        Merchant merchant = findMerchantOrThrow(merchantId);

        if (merchant.getStatus() != MerchantStatus.APPROVED) {
            throw new BusinessException("MERCH_003",
                    "Cannot suspend merchant. Current status: " + merchant.getStatus());
        }

        merchant.setStatus(MerchantStatus.SUSPENDED);
        merchant.setSuspendedAt(LocalDateTime.now());
        merchant.setSuspensionReason(reason);

        log.info("Merchant suspended: {} - reason: {}", merchantId, reason);
        return mapToAdminDto(merchantRepository.save(merchant));
    }

    // =====================
    // Updates
    // =====================

    /**
     * Updates merchant details (Admin only).
     */
    @Transactional
    public MerchantAdminDto updateMerchant(Long merchantId, AdminUpdateMerchantRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin updating merchant: {} by user: {}", merchantId, context.getUserId());

        Merchant merchant = findMerchantOrThrow(merchantId);

        if (request.getName() != null) merchant.setName(request.getName());
        if (request.getLegalName() != null) merchant.setLegalName(request.getLegalName());
        if (request.getDescription() != null) merchant.setDescription(request.getDescription());
        if (request.getCategory() != null) merchant.setCategory(request.getCategory());
        if (request.getRegistrationNo() != null) merchant.setRegistrationNo(request.getRegistrationNo());
        if (request.getContactEmail() != null) merchant.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) merchant.setContactPhone(request.getContactPhone());
        if (request.getWebsite() != null) merchant.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null) merchant.setLogoUrl(request.getLogoUrl());

        log.info("Merchant updated: {}", merchantId);
        return mapToAdminDto(merchantRepository.save(merchant));
    }

    /**
     * Updates merchant's own profile (Merchant User).
     */
    @Transactional
    public MerchantSelfDto updateMyMerchant(UpdateMyMerchantRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Merchant user updating their merchant by user: {}", context.getUserId());

        if (context.getMerchantId() == null) {
            throw new BusinessException("MERCH_004", "User is not assigned to any merchant");
        }

        Merchant merchant = findMerchantOrThrow(context.getMerchantId());

        if (request.getName() != null) merchant.setName(request.getName());
        if (request.getDescription() != null) merchant.setDescription(request.getDescription());
        if (request.getContactEmail() != null) merchant.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) merchant.setContactPhone(request.getContactPhone());
        if (request.getWebsite() != null) merchant.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null) merchant.setLogoUrl(request.getLogoUrl());

        log.info("Merchant profile updated by owner: {}", context.getMerchantId());
        return mapToSelfDto(merchantRepository.save(merchant));
    }

    // =====================
    // Queries
    // =====================

    /**
     * Gets merchant by ID (Admin).
     */
    public MerchantAdminDto getMerchantById(Long merchantId) {
        log.debug("Getting merchant by ID: {}", merchantId);
        return mapToAdminDto(findMerchantOrThrow(merchantId));
    }

    /**
     * Gets merchant by ID (Public).
     */
    public MerchantCustomerDto getPublicMerchantById(Long merchantId) {
        log.debug("Getting public merchant by ID: {}", merchantId);
        return mapToCustomerDetailDto(findMerchantOrThrow(merchantId));
    }

    /**
     * Gets the current user's merchant (Merchant User).
     */
    public MerchantSelfDto getMyMerchant() {
        var context = SecurityContextHolder.getContext();
        log.debug("Getting merchant for current user: {}", context.getUserId());

        if (context.getMerchantId() == null) {
            throw new BusinessException("MERCH_004", "User is not assigned to any merchant");
        }

        return mapToSelfDto(findMerchantOrThrow(context.getMerchantId()));
    }

    /**
     * Lookup merchants for dropdown (Admin).
     */
    public Stream<MerchantLookupDto> lookupMerchants(String query) {
        log.debug("Looking up merchants with query: {}", query);
        if (query == null || query.isBlank()) {
            return merchantRepository.findAll().stream().map(this::mapToLookupDto);
        }
        return merchantRepository.findByNameContainingIgnoreCase(query).stream().map(this::mapToLookupDto);
    }

    /**
     * Filter merchants with pagination (Admin).
     */
    public Page<MerchantAdminListDto> filterMerchants(MerchantFilterDto filter, Pageable pageable) {
        log.debug("Filtering merchants with filter: {}", filter);
        return merchantRepository
                .findAll(MerchantSpecification.filterBy(filter), pageable)
                .map(this::mapToAdminListDto);
    }

    /**
     * Gets merchants by status with pagination.
     */
    public Page<MerchantCustomerListDto> getMerchantsByStatus(MerchantStatus status, Pageable pageable) {
        log.debug("Getting merchants by status: {}", status);
        return merchantRepository
                .findByStatus(status, pageable)
                .map(this::mapToCustomerSummaryDto);
    }

    /**
     * Searches merchants by name with pagination.
     */
    public Page<MerchantCustomerListDto> searchMerchants(String query, Pageable pageable) {
        log.debug("Searching merchants with query: {}", query);
        return merchantRepository
                .findByNameContainingIgnoreCase(query, pageable)
                .map(this::mapToCustomerSummaryDto);
    }

    public boolean isMerchantApproved(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .map(m -> m.getStatus() == MerchantStatus.APPROVED)
                .orElse(false);
    }

    // =====================
    // Helpers
    // =====================

    private Merchant findMerchantOrThrow(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("MERCH_004", "Merchant not found"));
    }

    private MerchantAdminDto mapToAdminDto(Merchant merchant) {
        return MerchantAdminDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .legalName(merchant.getLegalName())
                .registrationNo(merchant.getRegistrationNo())
                .category(merchant.getCategory())
                .logoUrl(merchant.getLogoUrl())
                .contactEmail(merchant.getContactEmail())
                .contactPhone(merchant.getContactPhone())
                .description(merchant.getDescription())
                .website(merchant.getWebsite())
                .status(merchant.getStatus())
                .approvedAt(merchant.getApprovedAt())
                .approvedBy(merchant.getApprovedBy())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .rejectedAt(merchant.getRejectedAt())
                .rejectionReason(merchant.getRejectionReason())
                .suspendedAt(merchant.getSuspendedAt())
                .suspendedReason(merchant.getSuspensionReason())
                .build();
    }

    private MerchantAdminListDto mapToAdminListDto(Merchant merchant) {
        return MerchantAdminListDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .logoUrl(merchant.getLogoUrl())
                .contactEmail(merchant.getContactEmail())
                .contactPhone(merchant.getContactPhone())
                .status(merchant.getStatus())
                .build();
    }

    private MerchantSelfDto mapToSelfDto(Merchant merchant) {
        return MerchantSelfDto.builder()
                .name(merchant.getName())
                .legalName(merchant.getLegalName())
                .registrationNo(merchant.getRegistrationNo())
                .category(merchant.getCategory())
                .logoUrl(merchant.getLogoUrl())
                .contactEmail(merchant.getContactEmail())
                .contactPhone(merchant.getContactPhone())
                .description(merchant.getDescription())
                .website(merchant.getWebsite())
                .status(merchant.getStatus())
                .build();
    }

    private MerchantCustomerListDto mapToCustomerSummaryDto(Merchant merchant) {
        return MerchantCustomerListDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .logoUrl(merchant.getLogoUrl())
                .description(merchant.getDescription())
                .build();
    }

    private MerchantCustomerDto mapToCustomerDetailDto(Merchant merchant) {
        return MerchantCustomerDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .legalName(merchant.getLegalName())
                .category(merchant.getCategory())
                .logoUrl(merchant.getLogoUrl())
                .contactEmail(merchant.getContactEmail())
                .contactPhone(merchant.getContactPhone())
                .description(merchant.getDescription())
                .website(merchant.getWebsite())
                .build();
    }

    private MerchantLookupDto mapToLookupDto(Merchant merchant) {
        return MerchantLookupDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .build();
    }
}
