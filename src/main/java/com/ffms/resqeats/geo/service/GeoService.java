package com.ffms.resqeats.geo.service;

import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.enums.OutletAvailabilityStatus;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Geo service for nearby outlet queries per SRS Section 6.8.
 *
 * <p>Uses Redis GEO commands for efficient spatial queries:</p>
 * <ul>
 *   <li>GEOADD: Add outlet coordinates to the geo index</li>
 *   <li>GEORADIUS: Find outlets within a specified radius</li>
 *   <li>GEODIST: Calculate distance between two geographic points</li>
 * </ul>
 *
 * <p>Business Rules:</p>
 * <ul>
 *   <li>BR-014: Default search radius is 5km</li>
 *   <li>BR-015: Maximum search radius is 50km</li>
 *   <li>BR-016: Only active outlets are shown in search results</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OutletRepository outletRepository;

    private static final String GEO_KEY = "outlets:geo";
    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final double MAX_RADIUS_KM = 50.0;

    /**
     * Finds nearby outlets within a specified radius from the given coordinates.
     *
     * <p>This method queries the Redis geo index to find outlets within the specified
     * radius. If no radius is provided, the default radius of 5km is used (BR-014).
     * The maximum allowed radius is 50km (BR-015). Only active outlets are included
     * in the results (BR-016).</p>
     *
     * @param latitude the latitude coordinate of the search center point
     * @param longitude the longitude coordinate of the search center point
     * @param radiusKm the search radius in kilometers; uses default if null
     * @return a list of nearby outlets sorted by distance ascending; empty list if none found
     */
    public List<NearbyOutlet> findNearbyOutlets(double latitude, double longitude, Double radiusKm) {
        log.info("Finding nearby outlets - latitude: {}, longitude: {}, requestedRadius: {} km", 
                latitude, longitude, radiusKm);
        
        if (radiusKm == null) {
            radiusKm = DEFAULT_RADIUS_KM;
            log.debug("No radius specified, using default radius: {} km", DEFAULT_RADIUS_KM);
        }
        if (radiusKm > MAX_RADIUS_KM) {
            log.warn("Requested radius {} km exceeds maximum, capping to {} km", radiusKm, MAX_RADIUS_KM);
            radiusKm = MAX_RADIUS_KM;
        }

        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);

        log.debug("Executing Redis GEORADIUS query with center ({}, {}) and radius {} km", 
                longitude, latitude, radiusKm);
        
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = null;
        try {
            results = redisTemplate.opsForGeo()
                    .radius(GEO_KEY, circle,
                            RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                    .includeDistance()
                                    .includeCoordinates()
                                    .sortAscending()
                                    .limit(100));
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable for geo query, falling back to database search: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while querying Redis geo index: {}", ex.getMessage(), ex);
        }

        if (results == null) {
            // Fallback: compute distances from DB for ACTIVE outlets
            log.debug("Falling back to DB-based geo search for radius {} km", radiusKm);
            List<Outlet> allActive = outletRepository.findAllByStatus(OutletStatus.ACTIVE);
            List<NearbyOutlet> fallback = new ArrayList<>();
            for (Outlet outlet : allActive) {
                if (outlet.getLatitude() == null || outlet.getLongitude() == null) continue;
                double d = calculateDistance(latitude, longitude, outlet.getLatitude().doubleValue(), outlet.getLongitude().doubleValue());
                if (d <= radiusKm) {
                    fallback.add(NearbyOutlet.builder()
                            .outletId(outlet.getId())
                            .merchantId(outlet.getMerchantId())
                            .name(outlet.getName())
                            .address(outlet.getAddress())
                            .latitude(outlet.getLatitude().doubleValue())
                            .longitude(outlet.getLongitude().doubleValue())
                            .distanceKm(d)
                            .isOpen(outlet.getStatus() == OutletStatus.ACTIVE
                                && outlet.getAvailabilityStatus() == OutletAvailabilityStatus.OPEN)
                            .build());
                }
            }
            fallback.sort(Comparator.comparingDouble(NearbyOutlet::getDistanceKm));
            if (fallback.size() > 100) fallback = fallback.subList(0, 100);
            log.info("DB fallback returned {} nearby outlets", fallback.size());
            return fallback;
        }

        log.debug("Redis returned {} geo results, processing outlet details", results.getContent().size());
        
        List<NearbyOutlet> nearbyOutlets = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
            String outletId = result.getContent().getName().toString();
            double distanceKm = result.getDistance().getValue();

            log.debug("Processing outlet {} at distance {} km", outletId, distanceKm);
            
            outletRepository.findById(Long.valueOf(outletId)).ifPresent(outlet -> {
                if (outlet.getStatus() == OutletStatus.ACTIVE) {
                    nearbyOutlets.add(NearbyOutlet.builder()
                            .outletId(outlet.getId())
                            .merchantId(outlet.getMerchantId())
                            .name(outlet.getName())
                            .address(outlet.getAddress())
                            .latitude(outlet.getLatitude() != null ? outlet.getLatitude().doubleValue() : null)
                            .longitude(outlet.getLongitude() != null ? outlet.getLongitude().doubleValue() : null)
                            .distanceKm(distanceKm)
                            .isOpen(outlet.getAvailabilityStatus() == OutletAvailabilityStatus.OPEN)
                            .build());
                    log.debug("Added active outlet {} to results", outlet.getId());
                } else {
                    log.debug("Skipping inactive outlet {} with status {}", outlet.getId(), outlet.getStatus());
                }
            });
        }

        log.info("Successfully found {} nearby active outlets within {} km radius", nearbyOutlets.size(), radiusKm);
        return nearbyOutlets;
    }

    /**
     * Finds nearby outlets with pagination support.
     *
     * <p>This method provides a paginated view of active outlets. Currently uses
     * a status-based query as a fallback since JPA does not support native geo queries.</p>
     *
     * @param latitude the latitude coordinate of the search center point
     * @param longitude the longitude coordinate of the search center point
     * @param radiusKm the search radius in kilometers
     * @param pageable the pagination information
     * @return a page of active outlets
     */
    public Page<Outlet> findNearbyOutletsPaginated(double latitude, double longitude, 
                                                     double radiusKm, Pageable pageable) {
        log.info("Finding nearby outlets paginated - latitude: {}, longitude: {}, radius: {} km, page: {}", 
                latitude, longitude, radiusKm, pageable.getPageNumber());
        
        log.debug("Using status-based query as fallback for pagination");
        Page<Outlet> results = outletRepository.findByStatus(OutletStatus.ACTIVE, pageable);
        
        log.info("Successfully retrieved page {} with {} outlets (total: {})", 
                pageable.getPageNumber(), results.getNumberOfElements(), results.getTotalElements());
        return results;
    }

    /**
     * Calculates the distance between a user's location and a specific outlet.
     *
     * <p>First attempts to retrieve the outlet's coordinates from Redis geo index.
     * If not found in Redis, falls back to the database. Uses the Haversine formula
     * for distance calculation.</p>
     *
     * @param userLat the user's latitude coordinate
     * @param userLng the user's longitude coordinate
     * @param outletId the unique identifier of the outlet
     * @return the distance in kilometers, or null if the outlet location cannot be determined
     */
    public Double getDistanceToOutlet(double userLat, double userLng, Long outletId) {
        log.info("Calculating distance to outlet {} from user location ({}, {})", outletId, userLat, userLng);
        
        log.debug("Attempting to retrieve outlet position from Redis geo index");
        List<Point> positions = null;
        try {
            positions = redisTemplate.opsForGeo().position(GEO_KEY, outletId.toString());
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable for position lookup, will fallback to DB: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while retrieving position from Redis: {}", ex.getMessage(), ex);
        }

        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            log.debug("Outlet {} not found in Redis geo index, falling back to database", outletId);
            
            Outlet outlet = outletRepository.findById(outletId).orElse(null);
            if (outlet != null && outlet.getLatitude() != null && outlet.getLongitude() != null) {
                double d = calculateDistance(userLat, userLng, 
                        outlet.getLatitude().doubleValue(), outlet.getLongitude().doubleValue());
                log.info("Successfully calculated distance to outlet {} using database: {} km", outletId, d);
                return d;
            }
            log.warn("Unable to determine distance for outlet {} - outlet not found or missing coordinates", outletId);
            return null;
        }

        Point outletPoint = positions.get(0);
        log.debug("Found outlet {} in Redis at coordinates ({}, {})", outletId, outletPoint.getX(), outletPoint.getY());

        double d = calculateDistance(userLat, userLng, outletPoint.getY(), outletPoint.getX());
        log.info("Successfully calculated distance to outlet {} using Redis: {} km", outletId, d);
        return d;
    }

    /**
     * Adds an outlet to the Redis geo index.
     *
     * <p>The outlet is only added if it has valid latitude and longitude coordinates.
     * This enables efficient spatial queries using Redis GEO commands.</p>
     *
     * @param outlet the outlet entity to add to the geo index
     */
    public void addOutletToGeoIndex(Outlet outlet) {
        log.info("Adding outlet {} to geo index", outlet.getId());
        
        if (outlet.getLatitude() != null && outlet.getLongitude() != null) {
            log.debug("Executing Redis GEOADD for outlet {} at coordinates ({}, {})", 
                    outlet.getId(), outlet.getLongitude(), outlet.getLatitude());
            
            try {
                redisTemplate.opsForGeo().add(GEO_KEY,
                        new Point(outlet.getLongitude().doubleValue(), outlet.getLatitude().doubleValue()),
                        outlet.getId().toString());
                log.info("Successfully added outlet {} to geo index", outlet.getId());
            } catch (RedisConnectionFailureException ex) {
                log.warn("Redis unavailable, skipping geo index add for outlet {}: {}", outlet.getId(), ex.getMessage());
            } catch (Exception ex) {
                log.error("Unexpected error when adding outlet {} to geo index: {}", outlet.getId(), ex.getMessage(), ex);
            }
        } else {
            log.warn("Cannot add outlet {} to geo index - missing coordinates (lat: {}, lng: {})", 
                    outlet.getId(), outlet.getLatitude(), outlet.getLongitude());
        }
    }

    /**
     * Removes an outlet from the Redis geo index.
     *
     * <p>This should be called when an outlet is deactivated or deleted to ensure
     * it no longer appears in nearby outlet searches.</p>
     *
     * @param outletId the unique identifier of the outlet to remove
     */
    public void removeOutletFromGeoIndex(Long outletId) {
        log.info("Removing outlet {} from geo index", outletId);
        
        log.debug("Executing Redis ZREM for outlet {}", outletId);
        try {
            redisTemplate.opsForGeo().remove(GEO_KEY, outletId.toString());
            log.info("Successfully removed outlet {} from geo index", outletId);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable, skipping geo index remove for outlet {}: {}", outletId, ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error when removing outlet {} from geo index: {}", outletId, ex.getMessage(), ex);
        }
    }

    /**
     * Synchronizes all active outlets to the Redis geo index.
     *
     * <p>This method runs on application startup and every hour thereafter to ensure
     * the geo index stays in sync with the database. Only active outlets are indexed.</p>
     */
    @Scheduled(fixedRate = 3600000)
    public void syncGeoIndex() {
        log.info("Starting geo index synchronization");

        try {
            List<Outlet> activeOutlets = outletRepository.findAllByStatus(OutletStatus.ACTIVE);
            log.debug("Found {} active outlets to sync to geo index", activeOutlets.size());
            
            int successCount = 0;
            int skipCount = 0;
            
            for (Outlet outlet : activeOutlets) {
                if (outlet.getLatitude() != null && outlet.getLongitude() != null) {
                    addOutletToGeoIndex(outlet);
                    successCount++;
                } else {
                    skipCount++;
                    log.debug("Skipping outlet {} - missing coordinates", outlet.getId());
                }
            }

            log.info("Geo index sync completed successfully - {} outlets indexed, {} skipped due to missing coordinates", 
                    successCount, skipCount);
        } catch (Exception e) {
            log.error("Failed to sync geo index: {}", e.getMessage(), e);
        }
    }

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula.
     *
     * <p>The Haversine formula determines the great-circle distance between two points
     * on a sphere given their longitudes and latitudes. This provides accurate distance
     * calculations for locations on Earth's surface.</p>
     *
     * @param lat1 the latitude of the first point in degrees
     * @param lon1 the longitude of the first point in degrees
     * @param lat2 the latitude of the second point in degrees
     * @param lon2 the longitude of the second point in degrees
     * @return the distance between the two points in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        log.debug("Calculating Haversine distance between ({}, {}) and ({}, {})", lat1, lon1, lat2, lon2);
        
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;
        log.debug("Haversine distance calculated: {} km", distance);
        
        return distance;
    }

    /**
     * Data Transfer Object representing a nearby outlet in search results.
     *
     * <p>Contains outlet details along with distance information from the search
     * center point. Provides a human-readable distance display format.</p>
     *
     * @author ResqEats Team
     * @version 1.0
     * @since 2024-01-01
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NearbyOutlet {
        private Long outletId;
        private Long merchantId;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double distanceKm;
        private Boolean isOpen;

        /**
         * Returns a human-readable distance display string.
         *
         * <p>Displays distance in meters if less than 1km, otherwise in kilometers.</p>
         *
         * @return formatted distance string (e.g., "500 m" or "2.5 km")
         */
        public String getDistanceDisplay() {
            if (distanceKm == null) return "Unknown";
            if (distanceKm < 1) {
                return String.format("%.0f m", distanceKm * 1000);
            }
            return String.format("%.1f km", distanceKm);
        }
    }
}
