package com.frh.backend.controller;

import com.frh.backend.Model.*;
import com.frh.backend.repository.*;
import com.frh.backend.Services.StripeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile/checkout")
public class MobileCheckoutController {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ConsumerProfileRepository consumerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripeService stripeService;

    // accept APP JSON data
    public static class CheckoutRequest {
        public Long userId;
        public List<CheckoutItem> items;
    }

    public static class CheckoutItem {
        public Long listingId;
        public int quantity;
    }
    // ------------------------------------

    @PostMapping("/start")
    @Transactional
    public ResponseEntity<?> startCheckout(@RequestBody CheckoutRequest request) {

        // validation
        if (request.userId == null) return ResponseEntity.badRequest().body("UserId is required");
        if (request.items == null || request.items.isEmpty()) return ResponseEntity.badRequest().body("Cart is empty");

        //get info of users
        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ConsumerProfile consumer = consumerProfileRepository.findById(request.userId)
                .orElseGet(() -> {
                    ConsumerProfile newProfile = new ConsumerProfile();
                    newProfile.setUser(user);
                    return consumerProfileRepository.save(newProfile);
                });

        // Split Orders
        //  Map: Store -> List<OrderItem>
        Map<Store, List<OrderItem>> storeItemMap = new HashMap<>();


        BigDecimal paymentGrandTotal = BigDecimal.ZERO;

        for (CheckoutItem reqItem : request.items) {

            Listing listing = listingRepository.findById(reqItem.listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found ID: " + reqItem.listingId));



            Store store = listing.getStore();


            storeItemMap.putIfAbsent(store, new ArrayList<>());

            // get price RescuePrice first
            BigDecimal price = listing.getRescuePrice() != null ? listing.getRescuePrice() : listing.getOriginalPrice();

            // total payment amount
            BigDecimal lineTotal = price.multiply(new BigDecimal(reqItem.quantity));
            paymentGrandTotal = paymentGrandTotal.add(lineTotal);

            // create item
            OrderItem orderItem = new OrderItem();
            orderItem.setListing(listing);
            orderItem.setUnitPrice(price);
            orderItem.setQuantity(reqItem.quantity);


            storeItemMap.get(store).add(orderItem);
        }

        //create order for store
        List<Long> createdOrderIds = new ArrayList<>();

        for (Map.Entry<Store, List<OrderItem>> entry : storeItemMap.entrySet()) {
            Store store = entry.getKey();
            List<OrderItem> itemsForThisStore = entry.getValue();


            BigDecimal storeTotalAmount = BigDecimal.ZERO;
            for (OrderItem item : itemsForThisStore) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                storeTotalAmount = storeTotalAmount.add(itemTotal);
            }

            // create order
            Order order = new Order();
            order.setStore(store);
            order.setConsumer(consumer);
            order.setStatus("PENDING");
            order.setCurrency("SGD");
            order.setTotalAmount(storeTotalAmount); //this order is only for according store
            order.setPickupSlotStart(LocalDateTime.now().plusHours(1));
            order.setPickupSlotEnd(LocalDateTime.now().plusHours(5));


            for (OrderItem item : itemsForThisStore) {
                item.setOrder(order);
            }
            order.setOrderItems(itemsForThisStore);

            // save
            order = orderRepository.save(order);
            createdOrderIds.add(order.getOrderId());
        }

        //call Stripe
        try {
            //  orderID convert to string
            String orderIdsStr = createdOrderIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // put payment amount and orderId string
            String paymentUrl = stripeService.createCheckoutSession(paymentGrandTotal, orderIdsStr);

            //return to  APP
            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("orderIds", createdOrderIds); // APP 拿到的是数组 [101, 102]

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Stripe Error: " + e.getMessage());
        }
    }
}