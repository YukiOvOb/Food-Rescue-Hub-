package com.frh.backend.repository;

import com.frh.backend.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByConsumer_ConsumerIdAndStatus(Long consumerId, String status);
}