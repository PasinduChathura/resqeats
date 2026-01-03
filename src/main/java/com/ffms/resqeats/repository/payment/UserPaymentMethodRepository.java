package com.ffms.resqeats.repository.payment;

import com.ffms.resqeats.models.payment.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Long> {

    List<UserPaymentMethod> findByUserIdAndIsActiveTrue(Long userId);

    Optional<UserPaymentMethod> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT pm FROM UserPaymentMethod pm WHERE pm.user.id = :userId AND pm.isDefault = true AND pm.isActive = true")
    Optional<UserPaymentMethod> findDefaultByUserId(@Param("userId") Long userId);

    @Query("UPDATE UserPaymentMethod pm SET pm.isDefault = false WHERE pm.user.id = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);
}
