package com.frh.backend.repository;

import com.frh.backend.Model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  // fetch the supplier's order queue

  /*
   * Return all orders for a given store, optionally filtered by status.
   * Eagerly loads consumer + orderItems so the DTO mapper does not trigger N+1.
   */

  @Query("SELECT o FROM Order o " +
      "JOIN FETCH o.store s " +
      "JOIN FETCH o.consumer c " +
      "LEFT JOIN FETCH o.orderItems oi " +
      "LEFT JOIN FETCH oi.listing l " +
      "WHERE s.storeId = :storeId " +
      "AND (:status IS NULL OR o.status = :status) " +
      "ORDER BY o.createdAt DESC")
  List<Order> findByStoreIdAndStatus(@Param("storeId") Long storeId,
      @Param("status") String status);

  /*
   * Acquires a PESSIMISTIC_WRITE (SELECT … FOR UPDATE) lock on the row; Only one
   * thread can hold this lock at a time – prevents double-processing.
   */

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.orderId = :id")
  Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
