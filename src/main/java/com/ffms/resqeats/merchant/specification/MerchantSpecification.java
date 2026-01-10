package com.ffms.resqeats.merchant.specification;

import com.ffms.resqeats.merchant.dto.MerchantFilterDto;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Merchant entity filtering.
 * Builds dynamic queries based on MerchantFilterDto.
 * 
 * SCOPE FILTERING:
 * - ADMIN/SUPER_ADMIN: No automatic scope filtering (full access)
 * - MERCHANT_USER/OUTLET_USER: Automatically scoped to own merchant
 * - CUSTOMER_USER: No access (blocked)
 */
public class MerchantSpecification {

    /**
     * Creates a specification from a filter DTO with automatic scope filtering.
     *
     * @param filter the filter criteria
     * @return a JPA specification with scope predicates applied
     */
    public static Specification<Merchant> filterBy(MerchantFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ===== AUTOMATIC SCOPE FILTERING =====
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (!context.isAnonymous() && !context.hasGlobalAccess()) {
                // MERCHANT_USER/OUTLET_USER - scope to own merchant only
                if ((context.getRole() == UserRole.MERCHANT_USER || context.getRole() == UserRole.OUTLET_USER) 
                        && context.getMerchantId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), context.getMerchantId()));
                }
                // CUSTOMER_USER - can only see APPROVED merchants (public data)
                else if (context.getRole() == UserRole.CUSTOMER_USER) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), 
                            com.ffms.resqeats.merchant.enums.MerchantStatus.APPROVED));
                }
            }

            if (filter == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // ===== USER-PROVIDED FILTERS =====

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filter by category
            if (filter.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), filter.getCategory()));
            }

            // Search in name, legalName, description
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate legalNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("legalName")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, legalNamePredicate, descriptionPredicate));
            }

            // Filter by registrationNo
            if (filter.getRegistrationNo() != null && !filter.getRegistrationNo().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("registrationNo"), filter.getRegistrationNo()));
            }

            // Filter by dateFrom (createdAt >= dateFrom)
            if (filter.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.getDateFrom()));
            }

            // Filter by dateTo (createdAt <= dateTo)
            if (filter.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), filter.getDateTo()));
            }

            // Filter by approvedFrom (approvedAt >= approvedFrom)
            if (filter.getApprovedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("approvedAt"), filter.getApprovedFrom()));
            }

            // Filter by approvedTo (approvedAt <= approvedTo)
            if (filter.getApprovedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("approvedAt"), filter.getApprovedTo()));
            }

            // Filter by approvedBy
            if (filter.getApprovedBy() != null) {
                predicates.add(criteriaBuilder.equal(root.get("approvedBy"), filter.getApprovedBy()));
            }

            // Filter by emailDomain
            if (filter.getEmailDomain() != null && !filter.getEmailDomain().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("contactEmail")), 
                        "%@" + filter.getEmailDomain().toLowerCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
