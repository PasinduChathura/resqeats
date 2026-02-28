package com.ffms.resqeats.merchant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

/**
 * Merchant entity per SRS Section 7.2.
 * Represents a business partner who owns and operates one or more outlets.
 * Hierarchy: Merchant → Outlet → Item
 * 
 * HIBERNATE FILTERS (applied at repository level via TenantFilterAspect):
 * - merchantIdFilter: Filter by specific merchant_id
 * 
 * Users (MERCHANT_USER, OUTLET_USER) are assigned to merchants via User.merchantId.
 */
@Entity
@Table(name = "merchants")
@FilterDef(name = "merchantIdFilter", parameters = @ParamDef(name = "merchantId", type = Long.class))
@Filter(name = "merchantIdFilter", condition = "id = :merchantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant extends BaseEntity {

    @NotBlank
    @Column(name = "name", length = 255, nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "legal_name", length = 255)
    @JsonProperty("legal_name")
    private String legalName;

    @Column(name = "registration_no", length = 100)
    @JsonProperty("registration_no")
    private String registrationNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    @JsonProperty("category")
    private MerchantCategory category;

    @Column(name = "logo_url", length = 500)
    @JsonProperty("logo_url")
    private String logoUrl;

    @Email
    @Column(name = "contact_email", length = 255)
    @JsonProperty("contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    @JsonProperty("contact_phone")
    private String contactPhone;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "website", length = 255)
    @JsonProperty("website")
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private MerchantStatus status = MerchantStatus.PENDING_APPROVAL;

    @Column(name = "approved_at")
    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    @JsonProperty("approved_by")
    private Long approvedBy;

    @Column(name = "rejected_at")
    @JsonProperty("rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @Column(name = "suspended_at")
    @JsonProperty("suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspension_reason", length = 500)
    @JsonProperty("suspension_reason")
    private String suspensionReason;

    /**
     * Check if merchant is approved and can operate.
     */
    public boolean isApproved() {
        return status == MerchantStatus.APPROVED;
    }

    /**
     * Check if merchant can accept new orders.
     */
    public boolean canAcceptOrders() {
        return status == MerchantStatus.APPROVED;
    }
}
