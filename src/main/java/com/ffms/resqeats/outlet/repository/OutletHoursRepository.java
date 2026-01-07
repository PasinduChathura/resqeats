package com.ffms.resqeats.outlet.repository;

import com.ffms.resqeats.outlet.entity.OutletHours;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outlet hours repository.
 */
@Repository
public interface OutletHoursRepository extends com.ffms.resqeats.common.repository.BaseScopedRepository<OutletHours> {

    List<OutletHours> findByOutletId(UUID outletId);

    Optional<OutletHours> findByOutletIdAndDayOfWeek(UUID outletId, Integer dayOfWeek);

    @Modifying
    @Query("DELETE FROM OutletHours h WHERE h.outletId = :outletId")
    void deleteByOutletId(@Param("outletId") UUID outletId);

    @Override
    default void validateScope(OutletHours entity) {
        if (entity == null) return;
        requireOutletScope(entity.getOutletId());
    }
}
