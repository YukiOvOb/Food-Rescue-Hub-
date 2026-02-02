package com.frh.backend.repository;

import com.frh.backend.Model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


    List<OrderItem> findByOrder_OrderId(Long orderId);


    List<OrderItem> findByListing_ListingId(Long listingId);
}