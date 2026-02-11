package com.frh.backend.repository;

import com.frh.backend.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerOrderRepository extends JpaRepository<Order, Long> {

  /**
   * Find all orders for a specific consumer
   *
   * @param consumerId the consumer's ID
   * @return list of orders
   */
  List<Order> findByConsumer_ConsumerIdOrderByCreatedAtDesc(Long consumerId);

  /**
   * Find orders by status
   *
   * @param status the order status
   * @return list of orders
   */
  List<Order> findByStatusOrderByCreatedAtDesc(String status);

  /**
   * Find orders for a specific consumer with a specific status
   *
   * @param consumerId the consumer's ID
   * @param status the order status
   * @return list of orders
   */
  List<Order> findByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(
      Long consumerId, String status);
}
