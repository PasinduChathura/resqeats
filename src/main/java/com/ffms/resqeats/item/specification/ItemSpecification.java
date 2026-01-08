package com.ffms.resqeats.item.specification;

import com.ffms.resqeats.item.dto.ItemFilterDto;
import com.ffms.resqeats.item.entity.Item;
import com.ffms.resqeats.item.enums.ItemStatus;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Item entity filtering.
 * Builds dynamic queries based on ItemFilterDto.
 * 
 * SCOPE FILTERING:
 * - ADMIN/SUPER_ADMIN: No automatic scope filtering (full access)
 * - MERCHANT: Automatically scoped to merchant's items
 * - OUTLET_USER: Automatically scoped to merchant's items
 * - USER/Anonymous: Only ACTIVE items (public data)
 */
public class ItemSpecification {

    /**
     * Creates a specification from a filter DTO with automatic scope filtering.
     *
     * @param filter the filter criteria
     * @return a JPA specification with scope predicates applied
     */
    public static Specification<Item> filterBy(ItemFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ===== AUTOMATIC SCOPE FILTERING =====
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (context.isAnonymous()) {
                // Anonymous users - only ACTIVE items
                predicates.add(criteriaBuilder.equal(root.get("status"), ItemStatus.ACTIVE));
            } else if (!context.hasGlobalAccess()) {
                // MERCHANT - scope to merchant's items
                if (context.getRole() == UserRole.MERCHANT && context.getMerchantId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("merchantId"), context.getMerchantId()));
                }
                // OUTLET_USER - scope to merchant's items (read access)
                else if (context.getRole() == UserRole.OUTLET_USER && context.getMerchantId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("merchantId"), context.getMerchantId()));
                }
                // USER - only ACTIVE items (public data)
                else if (context.getRole() == UserRole.USER) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), ItemStatus.ACTIVE));
                }
            }

            if (filter == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // ===== USER-PROVIDED FILTERS =====

            // Filter by merchantId
            if (filter.getMerchantId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("merchantId"), filter.getMerchantId()));
            }

            // Filter by single category
            if (filter.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), filter.getCategory()));
            }

            // Filter by multiple categories
            if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
                predicates.add(root.get("category").in(filter.getCategories()));
            }

            // Filter by itemType
            if (filter.getItemType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("itemType"), filter.getItemType()));
            }

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Search in name and description
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
            }

            // Filter by minBasePrice (basePrice >= minBasePrice)
            if (filter.getMinBasePrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("basePrice"), filter.getMinBasePrice()));
            }

            // Filter by maxBasePrice (basePrice <= maxBasePrice)
            if (filter.getMaxBasePrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("basePrice"), filter.getMaxBasePrice()));
            }

            // Filter by minDiscountedPrice (discountedPrice >= minDiscountedPrice)
            if (filter.getMinDiscountedPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("discountedPrice"), filter.getMinDiscountedPrice()));
            }

            // Filter by maxDiscountedPrice (discountedPrice <= maxDiscountedPrice)
            if (filter.getMaxDiscountedPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("discountedPrice"), filter.getMaxDiscountedPrice()));
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

            // Filter by minimum discount percentage
            if (filter.getMinDiscountPercent() != null) {
                // Calculate discount percentage: (basePrice - discountedPrice) / basePrice * 100
                var discountPercent = criteriaBuilder.quot(
                        criteriaBuilder.diff(root.get("basePrice"), root.get("discountedPrice")),
                        root.get("basePrice"));
                var discountPercentValue = criteriaBuilder.prod(discountPercent, 100.0);
                predicates.add(criteriaBuilder.ge(
                        discountPercentValue, filter.getMinDiscountPercent()));
            }

            // Filter by items with images
            if (filter.getHasImages() != null) {
                if (filter.getHasImages()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("imageUrl")));
                    predicates.add(criteriaBuilder.notEqual(root.get("imageUrl"), ""));
                } else {
                    Predicate isNull = criteriaBuilder.isNull(root.get("imageUrl"));
                    Predicate isEmpty = criteriaBuilder.equal(root.get("imageUrl"), "");
                    predicates.add(criteriaBuilder.or(isNull, isEmpty));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
