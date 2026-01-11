package com.ffms.resqeats.outlet.specification;

import com.ffms.resqeats.outlet.dto.OutletFilterDto;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Outlet entity filtering.
 * Builds dynamic queries based on OutletFilterDto.
 * 
 * SCOPE FILTERING:
 * - ADMIN/SUPER_ADMIN: No automatic scope filtering (full access)
 * - MERCHANT_USER: Automatically scoped to merchant's outlets
 * - OUTLET_USER: Automatically scoped to own outlet
 * - CUSTOMER_USER/Anonymous: Only ACTIVE outlets (public data)
 */
public class OutletSpecification {

    /**
     * Creates a specification from a filter DTO with automatic scope filtering.
     *
     * @param filter the filter criteria
     * @return a JPA specification with scope predicates applied
     */
    public static Specification<Outlet> filterBy(OutletFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // ===== AUTOMATIC SCOPE FILTERING =====
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (context.isAnonymous()) {
                // Anonymous users - only ACTIVE outlets
                predicates.add(criteriaBuilder.equal(root.get("status"), 
                        com.ffms.resqeats.outlet.enums.OutletStatus.ACTIVE));
            } else if (!context.hasGlobalAccess()) {
                // MERCHANT_USER - scope to merchant's outlets
                if (context.getRole() == UserRole.MERCHANT_USER && context.getMerchantId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("merchantId"), context.getMerchantId()));
                }
                // OUTLET_USER - scope to own outlet only
                else if (context.getRole() == UserRole.OUTLET_USER && context.getOutletId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), context.getOutletId()));
                }
                // CUSTOMER_USER - only ACTIVE outlets (public data)
                else if (context.getRole() == UserRole.CUSTOMER_USER) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), 
                            com.ffms.resqeats.outlet.enums.OutletStatus.ACTIVE));
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

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filter by city
            if (filter.getCity() != null && !filter.getCity().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("city")), 
                        "%" + filter.getCity().toLowerCase() + "%"));
            }

            // Search in name, address, description
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate addressPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, addressPredicate, descriptionPredicate));
            }

            // Location-based filtering (radius search)
            if (filter.getLatitude() != null && filter.getLongitude() != null && filter.getRadiusKm() != null) {
                // Using Haversine formula for distance calculation
                // This is an approximation and may not be as accurate as PostGIS
                Double lat = filter.getLatitude().doubleValue();
                Double lon = filter.getLongitude().doubleValue();
                Double radius = filter.getRadiusKm().doubleValue();
                
                // Calculate approximate bounding box
                // 1 degree latitude ≈ 111 km
                Double latDelta = radius / 111.0;
                // 1 degree longitude varies with latitude
                Double lonDelta = radius / (111.0 * Math.cos(Math.toRadians(lat)));
                
                predicates.add(criteriaBuilder.between(
                        root.get("latitude"), 
                        BigDecimal.valueOf(lat - latDelta), 
                        BigDecimal.valueOf(lat + latDelta)));
                predicates.add(criteriaBuilder.between(
                        root.get("longitude"), 
                        BigDecimal.valueOf(lon - lonDelta), 
                        BigDecimal.valueOf(lon + lonDelta)));
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

            // Filter by postalCode
            if (filter.getPostalCode() != null && !filter.getPostalCode().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("postalCode"), filter.getPostalCode()));
            }

            // Filter by minimum rating
            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("averageRating"), filter.getMinRating()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
