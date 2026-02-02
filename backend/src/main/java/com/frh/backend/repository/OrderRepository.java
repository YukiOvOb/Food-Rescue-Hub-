package com.frh.backend.repository;

import com.frh.backend.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    List<Order> findByStore_StoreId(Long storeId);

    // storeId and order status
    List<Order> findByStore_StoreIdAndStatus(Long storeId, String status);


    //  Consumer App

    // 根据消费者ID查询订单 (顾客查看自己的历史订单)

    List<Order> findByConsumer_ConsumerId(Long consumerId);
}