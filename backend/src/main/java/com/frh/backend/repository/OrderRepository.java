package com.frh.backend.repository;

import com.frh.backend.Model.Order;
import com.frh.backend.dto.TopSellingItemDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Find orders by supplier (through store relationship)
    List<Order> findByStore_SupplierProfile_SupplierId(Long supplierId);
    
    // Find orders by supplier and status
    List<Order> findByStore_SupplierProfile_SupplierIdAndStatus(Long supplierId, String status);

    // Top selling items by supplier and order status
    @Query("SELECT new com.frh.backend.dto.TopSellingItemDto(oi.listing.listingId, oi.listing.title, SUM(oi.quantity)) " +
        "FROM Order o JOIN o.orderItems oi " +
        "WHERE o.store.supplierProfile.supplierId = :supplierId AND o.status = :status " +
        "GROUP BY oi.listing.listingId, oi.listing.title " +
        "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingItemDto> findTopSellingItemsBySupplierAndStatus(
        @Param("supplierId") Long supplierId,
        @Param("status") String status,
        Pageable pageable);
    
    // Check if order exists by order ID

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
