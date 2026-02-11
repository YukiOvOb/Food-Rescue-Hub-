package com.frh.backend.repository;

import com.frh.backend.model.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(
      Long consumerId, String status);
}
