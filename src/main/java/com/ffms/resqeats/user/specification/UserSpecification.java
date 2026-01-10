package com.ffms.resqeats.user.specification;

import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.dto.UserFilterDto;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for User entity filtering.
 * Builds dynamic queries based on UserFilterDto.
 * 
 * SCOPE FILTERING:
 * - ADMIN/SUPER_ADMIN: No automatic scope filtering (full access)
 * - MERCHANT_USER: Automatically scoped to merchant's users
 * - OUTLET_USER: Automatically scoped to outlet's users
 * - CUSTOMER_USER: Automatically scoped to own user record only
 */
public class UserSpecification {

    /**
     * Creates a specification from a filter DTO with automatic scope filtering.
     *
     * @param filter the filter criteria
     * @return a JPA specification with scope predicates applied
     */
    public static Specification<User> filterBy(UserFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ===== AUTOMATIC SCOPE FILTERING =====
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (!context.isAnonymous() && !context.hasGlobalAccess()) {
                // MERCHANT_USER - scope to merchant's users
                if (context.getRole() == UserRole.MERCHANT_USER && context.getMerchantId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("merchantId"), context.getMerchantId()));
                }
                // OUTLET_USER - scope to outlet's users
                else if (context.getRole() == UserRole.OUTLET_USER && context.getOutletId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("outletId"), context.getOutletId()));
                }
                // CUSTOMER_USER - can only see their own record
                else if (context.getRole() == UserRole.CUSTOMER_USER && context.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), context.getUserId()));
                }
            }

            if (filter == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // ===== USER-PROVIDED FILTERS =====

            // Filter by role
            if (filter.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), filter.getRole()));
            }

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Search in email, phone, firstName, lastName
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate phonePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phone")), searchPattern);
                Predicate firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), searchPattern);
                Predicate lastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), searchPattern);
                predicates.add(criteriaBuilder.or(emailPredicate, phonePredicate, 
                        firstNamePredicate, lastNamePredicate));
            }

            // Filter by merchantId
            if (filter.getMerchantId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("merchantId"), filter.getMerchantId()));
            }

            // Filter by outletId
            if (filter.getOutletId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("outletId"), filter.getOutletId()));
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

            // Filter by emailDomain
            if (filter.getEmailDomain() != null && !filter.getEmailDomain().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), 
                        "%@" + filter.getEmailDomain().toLowerCase()));
            }

            // Filter by verified status
            if (filter.getVerified() != null) {
                if (filter.getVerified()) {
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.isTrue(root.get("emailVerified")),
                            criteriaBuilder.isTrue(root.get("phoneVerified"))
                    ));
                } else {
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.isFalse(root.get("emailVerified")),
                            criteriaBuilder.isFalse(root.get("phoneVerified"))
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
