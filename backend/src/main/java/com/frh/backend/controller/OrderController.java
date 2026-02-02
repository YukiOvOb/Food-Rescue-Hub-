package com.frh.backend.controller;

import com.frh.backend.Model.*;
import com.frh.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // 允许前端跨域
public class OrderController {
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ConsumerProfileRepository consumerProfileRepository;
    @Autowired
    private UserRepository userRepository;



    @PostMapping("/mock")
    @Transactional
    public ResponseEntity<?> createMockOrder(@RequestParam Long storeId,
                                             @RequestParam Long userId,
                                             @RequestParam BigDecimal amount) {

        // 1. 找商店
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // 2. 找人
        ConsumerProfile consumer = consumerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    ConsumerProfile newProfile = new ConsumerProfile();
                    newProfile.setUser(user);
                    return consumerProfileRepository.save(newProfile);
                });

        // 🔥 3. 【重点修复】找一个商品 (Listing)
        // 必须从数据库里找一个真实存在的商品，否则 OrderItem 不知道关联谁
        Listing listing = listingRepository.findAll().stream()
                .filter(l -> l.getStore().getStoreId().equals(storeId)) // 最好找这家店的商品
                .findFirst()
                .orElse(listingRepository.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("No listings found! Please add a listing first.")));

        // 4. 创建订单
        Order order = new Order();
        order.setStore(store);
        order.setConsumer(consumer);
        order.setTotalAmount(amount);
        order.setStatus("PAID");
        order.setCurrency("SGD");
        order.setPickupSlotStart(LocalDateTime.now().plusHours(1));
        order.setPickupSlotEnd(LocalDateTime.now().plusHours(3));

        // 5. 创建订单项
        OrderItem item = new OrderItem();
        item.setOrder(order);

        // 🔥 【重点修复】把找到的商品塞进去！
        item.setListing(listing);

        item.setUnitPrice(amount);
        item.setQuantity(1);

        order.getOrderItems().add(item);

        orderRepository.save(order);

        return ResponseEntity.ok("Mock Order Created! ID: " + order.getOrderId());
    }

    // 2. 【供应商】查看自己店铺的订单
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Order>> getStoreOrders(@PathVariable Long storeId) {
        // 这里假设 OrderRepository 有这个方法
        // 如果没有，请在 OrderRepository 里加一行: List<Order> findByStore_StoreId(Long storeId);
        List<Order> orders = orderRepository.findByStore_StoreId(storeId);
        return ResponseEntity.ok(orders);
    }


    @PutMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        Order order = orderOpt.get();

        if (!"PAID".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Order status is " + order.getStatus() + ", cannot settle.");
        }

        order.setStatus("COMPLETED");

        orderRepository.save(order);


        return ResponseEntity.ok("Order " + orderId + " Settlement Successful!");
    }
}