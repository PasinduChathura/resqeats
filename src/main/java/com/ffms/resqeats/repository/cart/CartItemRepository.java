package com.ffms.resqeats.repository.cart;

import com.ffms.resqeats.models.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndSecretBoxId(Long cartId, Long secretBoxId);

    void deleteByCartId(Long cartId);

    void deleteByCartIdAndSecretBoxId(Long cartId, Long secretBoxId);
}
