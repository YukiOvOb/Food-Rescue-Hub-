package com.frh.backend.service;

import com.frh.backend.Model.Cart;
import com.frh.backend.Model.CartItem;
import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.Model.Store;
import com.frh.backend.dto.CartResponseDto;
import com.frh.backend.exception.CrossStoreException;
import com.frh.backend.repository.CartItemRepository;
import com.frh.backend.repository.CartRepository;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.ListingRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ConsumerProfileRepository consumerProfileRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getOrCreateActiveCart_createsCartWhenMissing() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);

        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setCartId(101L);
            return cart;
        });
        when(cartItemRepository.findByCart_CartId(101L)).thenReturn(List.of());

        CartResponseDto result = cartService.getOrCreateActiveCart(session);

        assertEquals(101L, result.getCartId());
        assertNull(result.getSupplierId());
        assertEquals(BigDecimal.ZERO, result.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getTotal());
        assertEquals(BigDecimal.ZERO, result.getTotalSavings());
        assertNotNull(result.getItems());
        assertTrueEmpty(result.getItems().isEmpty());
    }

    @Test
    void getOrCreateActiveCart_unauthenticated_throwsUnauthorized() {
        HttpSession session = new MockHttpSession();

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> cartService.getOrCreateActiveCart(session)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getOrCreateActiveCart_wrongRole_throwsForbidden() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);
        session.setAttribute("USER_ROLE", "SUPPLIER");

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> cartService.getOrCreateActiveCart(session)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void addItem_invalidQuantity_throwsBadRequest() {
        MockHttpSession session = consumerSession(1L);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> cartService.addItem(session, 99L, 0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void addItem_listingMissing_throwsNotFound() {
        MockHttpSession session = consumerSession(1L);
        when(listingRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> cartService.addItem(session, 99L, 1)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void addItem_crossStore_throwsException() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store existingStore = store(10L, "Store A");
        Store listingStore = store(20L, "Store B");
        Listing listing = listing(5L, listingStore, "Baguette", new BigDecimal("10.00"), new BigDecimal("6.00"));
        Cart activeCart = cart(500L, consumer, existingStore);

        when(listingRepository.findById(5L)).thenReturn(Optional.of(listing));
        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));

        CrossStoreException ex = assertThrows(
            CrossStoreException.class,
            () -> cartService.addItem(session, 5L, 1)
        );

        assertEquals(10L, ex.getCurrentSupplierId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItem_existingItem_incrementsQuantityAndReturnsTotals() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Listing listing = listing(5L, store, "Baguette", new BigDecimal("10.00"), new BigDecimal("6.00"));
        Cart cart = cart(500L, consumer, null);
        CartItem existing = cartItem(cart, listing, 2);
        CartItem updated = cartItem(cart, listing, 5);

        when(listingRepository.findById(5L)).thenReturn(Optional.of(listing));
        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(500L, 5L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartId(500L)).thenReturn(List.of(updated));

        CartResponseDto result = cartService.addItem(session, 5L, 3);

        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQty());
        assertEquals(new BigDecimal("30.00"), result.getSubtotal());
        assertEquals(new BigDecimal("20.00"), result.getTotalSavings());
    }

    @Test
    void addItem_newItem_mapsImageAndSavingsLabel() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Listing listing = listing(8L, store, "Salad", new BigDecimal("12.00"), new BigDecimal("7.00"));
        ListingPhoto photo = new ListingPhoto();
        photo.setPhotoUrl("https://img/salad.png");
        listing.setPhotos(List.of(photo));

        Cart activeCart = cart(700L, consumer, null);
        CartItem saved = cartItem(activeCart, listing, 2);

        when(listingRepository.findById(8L)).thenReturn(Optional.of(listing));
        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(700L, 8L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartId(700L)).thenReturn(List.of(saved));

        CartResponseDto result = cartService.addItem(session, 8L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals("https://img/salad.png", result.getItems().get(0).getImageUrl());
        assertEquals("Worth $12+", result.getItems().get(0).getSavingsLabel());
        assertEquals(new BigDecimal("14.00"), result.getSubtotal());
        assertEquals(new BigDecimal("10.00"), result.getTotalSavings());
    }

    @Test
    void updateQuantity_zero_removesItemAndClearsStoreWhenEmpty() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Listing listing = listing(9L, store, "Rice", new BigDecimal("8.00"), new BigDecimal("5.00"));
        Cart activeCart = cart(900L, consumer, store);
        CartItem existing = cartItem(activeCart, listing, 2);

        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(900L, 9L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCart_CartId(900L)).thenReturn(List.of(), List.of());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDto result = cartService.updateQuantity(session, 9L, 0);

        verify(cartItemRepository, times(1)).delete(existing);
        assertNull(activeCart.getStore());
        assertNull(result.getSupplierId());
        assertTrueEmpty(result.getItems().isEmpty());
    }

    @Test
    void updateQuantity_positive_updatesItemAndReturnsCart() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Listing listing = listing(9L, store, "Rice", new BigDecimal("8.00"), new BigDecimal("5.00"));
        Cart activeCart = cart(901L, consumer, store);
        CartItem existing = cartItem(activeCart, listing, 2);

        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(901L, 9L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartId(901L)).thenReturn(List.of(existing));

        CartResponseDto result = cartService.updateQuantity(session, 9L, 4);

        assertEquals(4, existing.getQuantity());
        assertEquals(1, result.getItems().size());
        assertEquals(4, result.getItems().get(0).getQty());
    }

    @Test
    void removeItem_notFound_throwsNotFound() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Cart activeCart = cart(1000L, consumer, null);

        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(1000L, 999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> cartService.removeItem(session, 999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeItem_whenCartStillHasItems_keepsStore() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Listing listing = listing(9L, store, "Rice", new BigDecimal("8.00"), new BigDecimal("5.00"));
        Cart activeCart = cart(1001L, consumer, store);
        CartItem removedItem = cartItem(activeCart, listing, 1);
        CartItem remainingItem = cartItem(activeCart, listing, 2);

        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(1001L, 9L)).thenReturn(Optional.of(removedItem));
        when(cartItemRepository.findByCart_CartId(1001L)).thenReturn(List.of(remainingItem), List.of(remainingItem));

        CartResponseDto result = cartService.removeItem(session, 9L);

        verify(cartItemRepository).delete(removedItem);
        verify(cartRepository, never()).save(any(Cart.class));
        assertEquals(10L, result.getSupplierId());
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    void clearCart_deletesItemsAndClearsStore() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Cart activeCart = cart(1200L, consumer, store);

        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        doNothing().when(cartItemRepository).deleteByCart_CartId(1200L);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartId(1200L)).thenReturn(List.of());

        CartResponseDto result = cartService.clearCart(session);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertNull(cartCaptor.getValue().getStore());
        assertNull(result.getSupplierId());
        assertTrueEmpty(result.getItems().isEmpty());
    }

    @Test
    void addItem_existingStoreSameAsListing_allowsAdd() {
        MockHttpSession session = consumerSession(1L);
        ConsumerProfile consumer = consumer(1L);
        Store store = store(10L, "Store A");
        Listing listing = listing(8L, store, "Salad", new BigDecimal("12.00"), new BigDecimal("7.00"));
        Cart activeCart = cart(1300L, consumer, store);
        CartItem saved = cartItem(activeCart, listing, 1);

        when(listingRepository.findById(8L)).thenReturn(Optional.of(listing));
        when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
        when(cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE"))
            .thenReturn(Optional.of(activeCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartIdAndListing_ListingId(1300L, 8L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.findByCart_CartId(1300L)).thenReturn(List.of(saved));

        CartResponseDto result = cartService.addItem(session, 8L, 1);

        assertEquals(1, result.getItems().size());
        assertEquals(10L, result.getSupplierId());
    }

    @Test
    void toCartItemDto_emptyPhotosNullPickupNullStore_noSavingsLabel() {
        Listing listing = listing(14L, null, "Deal", new BigDecimal("5.00"), new BigDecimal("5.00"));
        listing.setPhotos(List.of());
        listing.setPickupStart(null);
        listing.setPickupEnd(null);

        CartItem item = cartItem(cart(1400L, consumer(1L), null), listing, 1);

        CartResponseDto.CartItemDto dto = cartService.toCartItemDto(item);

        assertNull(dto.getImageUrl());
        assertNull(dto.getPickupStart());
        assertNull(dto.getPickupEnd());
        assertNull(dto.getStoreName());
        assertNull(dto.getSavingsLabel());
    }

    @Test
    void toCartResponseDto_handlesNullPricesInMappedItems() {
        Cart cart = cart(1500L, consumer(1L), null);
        CartItem item = cartItem(cart, listing(15L, store(1L, "S"), "Deal", new BigDecimal("10.00"), new BigDecimal("5.00")), 1);
        when(cartItemRepository.findByCart_CartId(1500L)).thenReturn(List.of(item));

        CartService spyService = Mockito.spy(cartService);
        CartResponseDto.CartItemDto mapped = new CartResponseDto.CartItemDto();
        mapped.setListingId(15L);
        mapped.setTitle("Deal");
        mapped.setQty(1);
        mapped.setLineTotal(new BigDecimal("0.00"));
        mapped.setOriginalPrice(null);
        mapped.setUnitPrice(new BigDecimal("5.00"));
        Mockito.doReturn(mapped).when(spyService).toCartItemDto(any(CartItem.class));

        CartResponseDto dto = spyService.toCartResponseDto(cart);

        assertEquals(BigDecimal.ZERO, dto.getTotalSavings());
    }

    private static MockHttpSession consumerSession(Long consumerId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", consumerId);
        session.setAttribute("USER_ROLE", "CONSUMER");
        return session;
    }

    private static ConsumerProfile consumer(Long id) {
        ConsumerProfile consumer = new ConsumerProfile();
        consumer.setConsumerId(id);
        consumer.setEmail("consumer@test.com");
        return consumer;
    }

    private static Cart cart(Long cartId, ConsumerProfile consumer, Store store) {
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setConsumer(consumer);
        cart.setStore(store);
        cart.setStatus("ACTIVE");
        return cart;
    }

    private static Store store(Long storeId, String name) {
        Store store = new Store();
        store.setStoreId(storeId);
        store.setStoreName(name);
        return store;
    }

    private static Listing listing(Long listingId, Store store, String title, BigDecimal originalPrice, BigDecimal rescuePrice) {
        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setStore(store);
        listing.setTitle(title);
        listing.setOriginalPrice(originalPrice);
        listing.setRescuePrice(rescuePrice);
        listing.setPickupStart(LocalDateTime.now().plusHours(1));
        listing.setPickupEnd(LocalDateTime.now().plusHours(2));
        listing.setExpiryAt(LocalDateTime.now().plusHours(4));
        return listing;
    }

    private static CartItem cartItem(Cart cart, Listing listing, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setListing(listing);
        item.setQuantity(quantity);
        return item;
    }

    private static void assertTrueEmpty(boolean isEmpty) {
        assertEquals(true, isEmpty);
    }
}
