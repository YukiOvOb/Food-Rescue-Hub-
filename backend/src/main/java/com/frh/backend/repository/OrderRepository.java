package com.frh.backend.repository;

import com.frh.backend.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by consumer
    List<Order> findByConsumer_ConsumerId(Long consumerId);
    
    // Find orders by store
    List<Order> findByStore_StoreId(Long storeId);
    
    // Find orders by status
    List<Order> findByStatus(String status);
    
    // Find orders by store and status
    List<Order> findByStore_StoreIdAndStatus(Long storeId, String status);
    
    // Find orders by consumer and status
    List<Order> findByConsumer_ConsumerIdAndStatus(Long consumerId, String status);
    
    // Check if order exists by order ID
    Optional<Order> findById(Long orderId);
}
