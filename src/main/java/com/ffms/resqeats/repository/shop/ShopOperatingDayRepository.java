package com.ffms.resqeats.repository.shop;

import com.ffms.resqeats.models.shop.ShopOperatingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopOperatingDayRepository extends JpaRepository<ShopOperatingDay, Long> {

    List<ShopOperatingDay> findByShopId(Long shopId);

    Optional<ShopOperatingDay> findByShopIdAndDayOfWeek(Long shopId, DayOfWeek dayOfWeek);

    void deleteByShopId(Long shopId);
}
