package com.frh.backend.service;

import com.frh.backend.Model.Cart;
import com.frh.backend.Model.CartItem;
import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.Order;
import com.frh.backend.Model.OrderItem;
import com.frh.backend.Model.Store;
import com.frh.backend.dto.CreateOrderRequest;
import com.frh.backend.dto.OrderSummaryDTO;
import com.frh.backend.dto.TopSellingItemDto;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.exception.OrderStateException;
import com.frh.backend.repository.CartItemRepository;
import com.frh.backend.repository.CartRepository;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ConsumerProfileRepository consumerProfileRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderFromCart_noActiveCart_throwsNotFound() {
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.createOrderFromCart(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2))
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createOrderFromCart_emptyCart_throwsBadRequest() {
        Cart cart = cart(10L, consumer(1L), store(100L));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartId(10L)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.createOrderFromCart(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createOrderFromCart_invalidPickupSlot_throwsBadRequest() {
        Cart cart = cart(10L, consumer(1L), store(100L));
        Listing listing = listing(1L, store(100L), 5, new BigDecimal("4.00"));
        CartItem item = cartItem(cart, listing, 1);

        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartId(10L)).thenReturn(List.of(item));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.createOrderFromCart(1L, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(1))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createOrderFromCart_listingMissing_throwsNotFound() {
        Cart cart = cart(10L, consumer(1L), store(100L));
        Listing listing = listing(1L, store(100L), 5, new BigDecimal("4.00"));
        CartItem item = cartItem(cart, listing, 1);

        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartId(10L)).thenReturn(List.of(item));
        when(listingRepository.findByIdForUpdate(1L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> orderService.createOrderFromCart(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2))
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createOrderFromCart_insufficientStock_throws() {
        Cart cart = cart(10L, consumer(1L), store(100L));
        Listing listing = listing(1L, store(100L), 1, new BigDecimal("4.00"));
        CartItem item = cartItem(cart, listing, 2);

        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartId(10L)).thenReturn(List.of(item));
        when(listingRepository.findByIdForUpdate(1L)).thenReturn(listing);

        InsufficientStockException ex = assertThrows(
            InsufficientStockException.class,
            () -> orderService.createOrderFromCart(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2))
        );

        assertEquals(1L, ex.getListingId());
        assertEquals(2, ex.getRequested());
        assertEquals(1, ex.getAvailable());
    }

    @Test
    void createOrderFromCart_success_createsOrderTokenAndClearsCart() {
        ConsumerProfile consumer = consumer(1L);
        Store store = store(100L);
        Cart cart = cart(10L, consumer, store);
        Listing listing = listing(1L, store, 5, new BigDecimal("4.00"));
        CartItem item = cartItem(cart, listing, 2);
        LocalDateTime slotStart = LocalDateTime.now().plusHours(1);
        LocalDateTime slotEnd = LocalDateTime.now().plusHours(2);

        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartId(10L)).thenReturn(List.of(item));
        when(listingRepository.findByIdForUpdate(1L)).thenReturn(listing);
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(55L);
            return order;
        });
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrderFromCart(1L, slotStart, slotEnd);

        assertEquals(55L, result.getOrderId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(new BigDecimal("8.00"), result.getTotalAmount());
        assertEquals(1, result.getOrderItems().size());
        assertNotNull(result.getPickupToken());
        assertNotNull(result.getPickupToken().getQrTokenHash());
        assertNull(cart.getStore());
        verify(cartItemRepository).deleteByCart_CartId(10L);
    }

    @Test
    void createOrder_listingValidationAndStockPaths() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setListingId(10L);
        req.setConsumerId(20L);
        req.setQuantity(2);
        req.setPickupSlotStart(LocalDateTime.now().plusHours(1));
        req.setPickupSlotEnd(LocalDateTime.now().plusHours(2));

        when(listingRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> orderService.createOrder(req));

        Listing inactiveListing = listing(10L, store(100L), 5, new BigDecimal("3.00"));
        inactiveListing.setStatus("INACTIVE");
        when(listingRepository.findById(10L)).thenReturn(Optional.of(inactiveListing));
        ResponseStatusException inactiveEx = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(req));
        assertEquals(HttpStatus.BAD_REQUEST, inactiveEx.getStatusCode());

        Listing activeListing = listing(10L, store(100L), 5, new BigDecimal("3.00"));
        activeListing.setStatus("ACTIVE");
        when(listingRepository.findById(10L)).thenReturn(Optional.of(activeListing));
        when(consumerProfileRepository.findById(20L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> orderService.createOrder(req));

        ConsumerProfile consumer = consumer(20L);
        when(consumerProfileRepository.findById(20L)).thenReturn(Optional.of(consumer));
        when(inventoryService.checkStock(10L, 2)).thenReturn(false);
        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(1);
        when(inventoryService.getInventory(10L)).thenReturn(inventory);
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(req));
    }

    @Test
    void createOrder_success() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setListingId(11L);
        req.setConsumerId(21L);
        req.setQuantity(3);
        req.setPickupSlotStart(LocalDateTime.now().plusHours(2));
        req.setPickupSlotEnd(LocalDateTime.now().plusHours(3));

        Listing listing = listing(11L, store(101L), 9, new BigDecimal("2.50"));
        listing.setStatus("ACTIVE");
        ConsumerProfile consumer = consumer(21L);

        when(listingRepository.findById(11L)).thenReturn(Optional.of(listing));
        when(consumerProfileRepository.findById(21L)).thenReturn(Optional.of(consumer));
        when(inventoryService.checkStock(11L, 3)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(req);

        assertEquals("PENDING", result.getStatus());
        assertEquals(new BigDecimal("7.50"), result.getTotalAmount());
        assertEquals(1, result.getOrderItems().size());
        assertEquals(3, result.getOrderItems().get(0).getQuantity());
        assertNotNull(result.getPickupToken());
        assertNotNull(result.getPickupToken().getQrTokenHash());
    }

    @Test
    void acceptRejectCancelAccepted_orderStateTransitions() {
        Order pending = orderWithItem(1L, "PENDING", listing(100L, store(1L), 10, new BigDecimal("3.00")), 2);
        Order accepted = orderWithItem(2L, "ACCEPTED", listing(101L, store(1L), 10, new BigDecimal("4.00")), 1);

        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pending));
        when(orderRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(accepted));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order acceptedResult = orderService.acceptOrder(1L);
        assertEquals("ACCEPTED", acceptedResult.getStatus());
        verify(inventoryService).decrementStock(100L, 2);

        Order cancelled = orderService.cancelAcceptedOrder(2L, "Supplier closed");
        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("Supplier closed", cancelled.getCancelReason());
        verify(inventoryService).restoreStock(101L, 1);

        Order pendingForReject = orderWithItem(3L, "PENDING", listing(102L, store(1L), 10, new BigDecimal("5.00")), 1);
        when(orderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(pendingForReject));
        Order rejected = orderService.rejectOrder(3L, "Out of stock");
        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("Out of stock", rejected.getCancelReason());
    }

    @Test
    void acceptRejectCancelAccepted_invalidState_throwsOrderStateException() {
        Order nonPending = orderWithItem(1L, "REJECTED", listing(1L, store(1L), 3, new BigDecimal("2.00")), 1);
        Order nonAccepted = orderWithItem(2L, "PENDING", listing(2L, store(1L), 3, new BigDecimal("2.00")), 1);

        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(nonPending));
        when(orderRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(nonAccepted));

        assertThrows(OrderStateException.class, () -> orderService.acceptOrder(1L));
        assertThrows(OrderStateException.class, () -> orderService.rejectOrder(1L, "x"));
        assertThrows(OrderStateException.class, () -> orderService.cancelAcceptedOrder(2L, "x"));
    }

    @Test
    void getOrderQueue_mapsSummary() {
        ConsumerProfile consumer = consumer(55L);
        consumer.setDisplayName("Alice");
        consumer.setPhone("91234567");

        Listing listing = listing(9L, store(99L), 4, new BigDecimal("6.00"));
        listing.setTitle("Nasi Lemak");

        Order order = new Order();
        order.setOrderId(88L);
        order.setStatus("PENDING");
        order.setStore(store(99L));
        order.setConsumer(consumer);
        order.setPickupSlotStart(LocalDateTime.now().plusHours(1));
        order.setPickupSlotEnd(LocalDateTime.now().plusHours(2));
        order.setTotalAmount(new BigDecimal("12.00"));
        order.setCancelReason("NA");

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setListing(listing);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("6.00"));
        order.setOrderItems(List.of(item));

        when(orderRepository.findByStoreIdAndStatus(99L, "PENDING")).thenReturn(List.of(order));

        List<OrderSummaryDTO> result = orderService.getOrderQueue(99L, "PENDING");

        assertEquals(1, result.size());
        OrderSummaryDTO dto = result.get(0);
        assertEquals(88L, dto.getOrderId());
        assertEquals(9L, dto.getListingId());
        assertEquals("Nasi Lemak", dto.getListingTitle());
        assertEquals("Alice", dto.getConsumerName());
        assertEquals(2, dto.getQuantity());
    }

    @Test
    void getOrderQueue_withoutItems_usesFallbacks() {
        Order order = new Order();
        order.setOrderId(70L);
        order.setStatus("PENDING");
        order.setStore(store(9L));
        order.setConsumer(consumer(3L));
        order.setOrderItems(List.of());

        when(orderRepository.findByStoreIdAndStatus(9L, null)).thenReturn(List.of(order));

        OrderSummaryDTO dto = orderService.getOrderQueue(9L, null).get(0);

        assertEquals("—", dto.getListingTitle());
        assertEquals(0, dto.getQuantity());
    }

    @Test
    void updateOrderAndStatusAndDeleteAndCancel_paths() {
        Order existing = new Order();
        existing.setOrderId(9L);
        existing.setStatus("PENDING");
        existing.setTotalAmount(new BigDecimal("9.00"));

        when(orderRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.existsById(9L)).thenReturn(true);
        when(orderRepository.existsById(404L)).thenReturn(false);

        Order changes = new Order();
        changes.setStatus("ACCEPTED");
        changes.setCancelReason("Reason");
        Order updated = orderService.updateOrder(9L, changes);
        assertEquals("ACCEPTED", updated.getStatus());
        assertEquals("Reason", updated.getCancelReason());

        Order statusUpdated = orderService.updateOrderStatus(9L, "COMPLETED");
        assertEquals("COMPLETED", statusUpdated.getStatus());

        Order cancelled = orderService.cancelOrder(9L, "User request");
        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("User request", cancelled.getCancelReason());

        orderService.deleteOrder(9L);
        verify(orderRepository).deleteById(9L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orderService.deleteOrder(404L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateOrderOrStatusOrCancel_notFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.updateOrder(1L, new Order()));
        assertThrows(ResponseStatusException.class, () -> orderService.updateOrderStatus(1L, "X"));
        assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(1L, "X"));
    }

    @Test
    void simpleQueryAndCountMethods_delegate() {
        Order order = new Order();
        TopSellingItemDto top = new TopSellingItemDto(1L, "Item", 5L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderRepository.findByConsumer_ConsumerId(10L)).thenReturn(List.of(order));
        when(orderRepository.findByStore_StoreId(20L)).thenReturn(List.of(order));
        when(orderRepository.findByStatus("PENDING")).thenReturn(List.of(order, order));
        when(orderRepository.findByStore_StoreIdAndStatus(20L, "PENDING")).thenReturn(List.of(order));
        when(orderRepository.findTopSellingItemsBySupplierAndStatus(eq(99L), eq("COMPLETED"), any(Pageable.class)))
            .thenReturn(List.of(top));

        assertEquals(true, orderService.getOrderById(1L).isPresent());
        assertEquals(1, orderService.getAllOrders().size());
        assertEquals(1, orderService.getOrdersByConsumer(10L).size());
        assertEquals(1, orderService.getOrdersByStore(20L).size());
        assertEquals(2, orderService.getOrdersByStatus("PENDING").size());
        assertEquals(1, orderService.getOrdersByStoreAndStatus(20L, "PENDING").size());
        assertEquals(2, orderService.countOrdersByStatus("PENDING"));
        assertEquals(1, orderService.countPendingOrdersForStore(20L));
        assertEquals(1, orderService.getTopSellingItems(99L, "COMPLETED", 0).size());
    }

    @Test
    void acceptOrder_notFound_throwsNotFound() {
        when(orderRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orderService.acceptOrder(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(inventoryService, never()).decrementStock(anyLong(), any(Integer.class));
    }

    private static ConsumerProfile consumer(Long id) {
        ConsumerProfile consumer = new ConsumerProfile();
        consumer.setConsumerId(id);
        consumer.setDisplayName("Consumer " + id);
        return consumer;
    }

    private static Store store(Long id) {
        Store store = new Store();
        store.setStoreId(id);
        store.setStoreName("Store " + id);
        return store;
    }

    private static Listing listing(Long listingId, Store store, int qtyAvailable, BigDecimal rescuePrice) {
        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setStore(store);
        listing.setTitle("Listing " + listingId);
        listing.setOriginalPrice(rescuePrice.multiply(new BigDecimal("2")));
        listing.setRescuePrice(rescuePrice);
        listing.setPickupStart(LocalDateTime.now().plusHours(1));
        listing.setPickupEnd(LocalDateTime.now().plusHours(2));
        listing.setExpiryAt(LocalDateTime.now().plusHours(5));
        listing.setStatus("ACTIVE");

        Inventory inventory = new Inventory();
        inventory.setListing(listing);
        inventory.setQtyAvailable(qtyAvailable);
        inventory.setQtyReserved(0);
        listing.setInventory(inventory);
        return listing;
    }

    private static Cart cart(Long id, ConsumerProfile consumer, Store store) {
        Cart cart = new Cart();
        cart.setCartId(id);
        cart.setConsumer(consumer);
        cart.setStore(store);
        cart.setStatus("ACTIVE");
        return cart;
    }

    private static CartItem cartItem(Cart cart, Listing listing, int qty) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setListing(listing);
        cartItem.setQuantity(qty);
        return cartItem;
    }

    private static Order orderWithItem(Long orderId, String status, Listing listing, int qty) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(status);
        order.setStore(listing.getStore());
        order.setConsumer(consumer(1L));
        order.setTotalAmount(listing.getRescuePrice().multiply(BigDecimal.valueOf(qty)));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setListing(listing);
        item.setQuantity(qty);
        item.setUnitPrice(listing.getRescuePrice());
        order.setOrderItems(List.of(item));
        return order;
    }
}
