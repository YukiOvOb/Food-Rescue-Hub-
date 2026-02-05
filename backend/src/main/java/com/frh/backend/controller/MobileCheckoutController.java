package com.frh.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frh.backend.Model.*;
import com.frh.backend.repository.*;
import com.frh.backend.service.StripeService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/mobile/checkout")
public class MobileCheckoutController {
    public static class CheckoutRequest {
        public List<CheckoutItem> items;
        public LocalDateTime pickupSlotStart;
        public LocalDateTime pickupSlotEnd;
    }
    public static class CheckoutItem {
        public Long listingId;
        public int quantity;
    }

    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ConsumerProfileRepository consumerProfileRepository;
    @Autowired
    private StripeService stripeService;

    @PostMapping("/start")
    @Transactional
    public ResponseEntity<?> startMobileCheckout(HttpSession session, @RequestBody CheckoutRequest request) {
        // validation: ensure user in session and items present
        Long consumerId = (Long) session.getAttribute("USER_ID");
        if (consumerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        if (request.items == null || request.items.isEmpty()) return ResponseEntity.badRequest().body("Cart is empty");
        
        // Validate pickup slot is provided
        if (request.pickupSlotStart == null || request.pickupSlotEnd == null) {
            return ResponseEntity.badRequest().body("Pickup time slot is required");
        }
        
        // get consumer from session id
        ConsumerProfile consumer = consumerProfileRepository.findById(consumerId)
                .orElseThrow(() -> new RuntimeException("Consumer profile not found"));
        // Split Orders per Store
        Map<Store, List<OrderItem>> storeItemMap = new HashMap<>();

        BigDecimal paymentGrandTotal = BigDecimal.ZERO;
        List<StripeService.StripeLineItem> stripeLineItems = new ArrayList<>();
        
        for (CheckoutItem reqItem : request.items) {
            // Use pessimistic lock for stock validation
            Listing listing = listingRepository.findByIdForUpdate(reqItem.listingId);
            if (listing == null) {
                throw new RuntimeException("Listing not found ID: " + reqItem.listingId);
            }

            // Check stock availability
            Inventory inventory = listing.getInventory();
            if (inventory == null || inventory.getQtyAvailable() < reqItem.quantity) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Insufficient stock for listing: " + listing.getTitle() + 
                          ". Available: " + (inventory != null ? inventory.getQtyAvailable() : 0) +
                          ", Requested: " + reqItem.quantity);
            }

            // Decrement stock (reserve it)
            inventory.setQtyAvailable(inventory.getQtyAvailable() - reqItem.quantity);
            inventory.setQtyReserved(inventory.getQtyReserved() + reqItem.quantity);

            Store store = listing.getStore();

            storeItemMap.putIfAbsent(store, new ArrayList<>());
            BigDecimal price = listing.getRescuePrice() != null ? listing.getRescuePrice() : listing.getOriginalPrice();
            BigDecimal lineTotal = price.multiply(new BigDecimal(reqItem.quantity));
            paymentGrandTotal = paymentGrandTotal.add(lineTotal);
            
            // Add to Stripe line items with actual listing title
            stripeLineItems.add(new StripeService.StripeLineItem(
                listing.getTitle(),
                reqItem.quantity,
                price
            ));

            OrderItem orderItem = new OrderItem();
            orderItem.setListing(listing);
            orderItem.setUnitPrice(price);
            orderItem.setQuantity(reqItem.quantity);

            storeItemMap.get(store).add(orderItem);
        }

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
            order.setStatus("PENDING_PAYMENT");
            order.setCurrency("SGD");
            order.setTotalAmount(storeTotalAmount);
            // Use pickup slot from request instead of hardcoded times
            order.setPickupSlotStart(request.pickupSlotStart);
            order.setPickupSlotEnd(request.pickupSlotEnd);

            for (OrderItem item : itemsForThisStore) {
                item.setOrder(order);
            }
            order.setOrderItems(itemsForThisStore);
            order = orderRepository.save(order);
            createdOrderIds.add(order.getOrderId());
        }

        try {
            String orderIdsStr = createdOrderIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            String paymentUrl = stripeService.createCheckoutSession(stripeLineItems, orderIdsStr);
            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("orderIds", createdOrderIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Stripe Error: " + e.getMessage());
        }
    }
}
