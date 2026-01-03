package com.ffms.resqeats.service.food;

import com.ffms.resqeats.dto.food.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface SecretBoxService {

    SecretBoxResponse createSecretBox(CreateSecretBoxRequest request, Long ownerId);

    SecretBoxResponse updateSecretBox(Long secretBoxId, CreateSecretBoxRequest request, Long ownerId);

    SecretBoxResponse getSecretBoxById(Long secretBoxId);

    List<SecretBoxResponse> getSecretBoxesByShop(Long shopId);

    Page<SecretBoxResponse> getSecretBoxesByShop(Long shopId, Pageable pageable);

    List<SecretBoxResponse> getAvailableBoxesByShop(Long shopId);

    Page<SecretBoxResponse> getAllAvailableBoxes(Pageable pageable);

    List<SecretBoxResponse> getNearbyAvailableBoxes(BigDecimal latitude, BigDecimal longitude, Double radiusKm);

    void deleteSecretBox(Long secretBoxId, Long ownerId);

    void deactivateSecretBox(Long secretBoxId, Long ownerId);

    void activateSecretBox(Long secretBoxId, Long ownerId);

    boolean updateQuantity(Long secretBoxId, int quantityChange);

    SecretBoxResponse updateBoxQuantity(Long secretBoxId, Integer newQuantity, Long ownerId);
}
