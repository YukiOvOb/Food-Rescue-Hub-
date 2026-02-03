package com.frh.backend.service;

import com.frh.backend.Model.*;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceStockTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierProfileRepository supplierProfileRepository;

    @Autowired
    private ConsumerProfileRepository consumerProfileRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        inventoryRepository.deleteAll();
        listingRepository.deleteAll();
        storeRepository.deleteAll();
        supplierProfileRepository.deleteAll();
        consumerProfileRepository.deleteAll();
    }

    @Test
    void createOrderFromCart_deductsStock() {
        ConsumerProfile consumer = createConsumer("consumer1@test.com");
        SupplierProfile supplier = createSupplier("supplier1@test.com");
        Store store = createStore(supplier);
        Listing listing = createListing(store, 5);
        createCartWithItem(consumer, store, listing, 2);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(1);

        orderService.createOrderFromCart(consumer.getConsumerId(), start, end);

        Listing updated = listingRepository.findById(listing.getListingId()).orElseThrow();
        assertEquals(3, updated.getAvailableQty());
        assertEquals(1, orderRepository.count());
    }

    @Test
    void createOrderFromCart_concurrentRequests_onlyOneSucceeds() throws Exception {
        SupplierProfile supplier = createSupplier("supplier2@test.com");
        Store store = createStore(supplier);
        Listing listing = createListing(store, 1);

        ConsumerProfile consumerA = createConsumer("consumerA@test.com");
        ConsumerProfile consumerB = createConsumer("consumerB@test.com");

        createCartWithItem(consumerA, store, listing, 1);
        createCartWithItem(consumerB, store, listing, 1);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Boolean> taskA = () -> runOrder(startLatch, consumerA.getConsumerId(), start, end);
        Callable<Boolean> taskB = () -> runOrder(startLatch, consumerB.getConsumerId(), start, end);

        List<Future<Boolean>> futures = new ArrayList<>();
        futures.add(executor.submit(taskA));
        futures.add(executor.submit(taskB));

        startLatch.countDown();

        int successCount = 0;
        int failureCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        executor.shutdown();

        Listing updated = listingRepository.findById(listing.getListingId()).orElseThrow();
        assertEquals(0, updated.getAvailableQty());
        assertEquals(1, successCount);
        assertEquals(1, failureCount);
        assertEquals(1, orderRepository.count());
    }

    private boolean runOrder(CountDownLatch startLatch, Long consumerId, LocalDateTime start, LocalDateTime end) throws Exception {
        startLatch.await();
        try {
            orderService.createOrderFromCart(consumerId, start, end);
            return true;
        } catch (InsufficientStockException ex) {
            return false;
        }
    }

    private ConsumerProfile createConsumer(String email) {
        ConsumerProfile consumer = new ConsumerProfile();
        consumer.setEmail(email);
        consumer.setPassword("password123");
        consumer.setDisplayName("Test Consumer");
        return consumerProfileRepository.save(consumer);
    }

    private SupplierProfile createSupplier(String email) {
        SupplierProfile supplier = new SupplierProfile();
        supplier.setEmail(email);
        supplier.setPassword("password123");
        supplier.setBusinessName("Test Supplier");
        supplier.setDisplayName("Supplier");
        return supplierProfileRepository.save(supplier);
    }

    private Store createStore(SupplierProfile supplier) {
        Store store = new Store();
        store.setSupplierProfile(supplier);
        store.setStoreName("Test Store");
        store.setAddressLine("123 Test Street");
        store.setPostalCode("123456");
        return storeRepository.save(store);
    }

    private Listing createListing(Store store, int qty) {
        Listing listing = new Listing();
        listing.setStore(store);
        listing.setTitle("Test Listing");
        listing.setDescription("Test Description");
        listing.setOriginalPrice(new BigDecimal("10.00"));
        listing.setRescuePrice(new BigDecimal("5.00"));
        LocalDateTime pickupStart = LocalDateTime.now().plusHours(1);
        listing.setPickupStart(pickupStart);
        listing.setPickupEnd(pickupStart.plusHours(1));
        listing.setExpiryAt(pickupStart.plusDays(1));
        listing.setStatus("ACTIVE");
        listing.setAvailableQty(qty);
        return listingRepository.save(listing);
    }

    private void createCartWithItem(ConsumerProfile consumer, Store store, Listing listing, int qty) {
        Cart cart = new Cart();
        cart.setConsumer(consumer);
        cart.setStore(store);
        cart.setStatus("ACTIVE");
        Cart savedCart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(savedCart);
        cartItem.setListing(listing);
        cartItem.setQuantity(qty);
        cartItemRepository.save(cartItem);
    }
}
