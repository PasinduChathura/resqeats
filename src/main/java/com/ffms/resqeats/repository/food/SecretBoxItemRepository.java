package com.ffms.resqeats.repository.food;

import com.ffms.resqeats.models.food.SecretBoxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecretBoxItemRepository extends JpaRepository<SecretBoxItem, Long> {

    List<SecretBoxItem> findBySecretBoxId(Long secretBoxId);

    void deleteBySecretBoxId(Long secretBoxId);
}
