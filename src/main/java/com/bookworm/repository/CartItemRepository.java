package com.bookworm.repository;

import com.bookworm.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartIdAndBookId(UUID cartId, UUID bookId);
    void deleteByCartId(UUID cartId);
}
