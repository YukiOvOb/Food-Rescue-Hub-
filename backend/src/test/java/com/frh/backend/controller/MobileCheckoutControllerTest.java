package com.frh.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.Order;
import com.frh.backend.Model.Store;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.OrderRepository;
import com.frh.backend.service.StripeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MobileCheckoutController.class)
class MobileCheckoutControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ListingRepository listingRepository;

  @MockitoBean private OrderRepository orderRepository;

  @MockitoBean private ConsumerProfileRepository consumerProfileRepository;

  @MockitoBean private StripeService stripeService;

  @Test
  void startMobileCheckout_unauthorizedWithoutSessionUser() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "items", List.of(Map.of("listingId", 1L, "quantity", 1)),
            "pickupSlotStart", LocalDateTime.now().plusHours(1).toString(),
            "pickupSlotEnd", LocalDateTime.now().plusHours(2).toString());

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void startMobileCheckout_emptyItems_returnsBadRequest() throws Exception {
    Map<String, Object> payload =
        Map.of(
            "items", List.of(),
            "pickupSlotStart", LocalDateTime.now().plusHours(1).toString(),
            "pickupSlotEnd", LocalDateTime.now().plusHours(2).toString());

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void startMobileCheckout_missingPickupSlot_returnsBadRequest() throws Exception {
    Map<String, Object> payload = Map.of("items", List.of(Map.of("listingId", 1L, "quantity", 1)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void startMobileCheckout_listingNotFound_returnsNotFound() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(100L)).thenReturn(null);

    Map<String, Object> payload = payload(List.of(Map.of("listingId", 100L, "quantity", 1)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isNotFound());
  }

  @Test
  void startMobileCheckout_insufficientStock_returnsConflict() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    Listing listing = listing(10L, store(1L, "Store A"), new BigDecimal("5.00"), 1, 0);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(10L)).thenReturn(listing);

    Map<String, Object> payload = payload(List.of(Map.of("listingId", 10L, "quantity", 2)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isConflict());
  }

  @Test
  void startMobileCheckout_success_singleStore() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);

    Store store = store(1L, "Store A");
    Listing listing = listing(10L, store, new BigDecimal("5.00"), 10, 0);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(10L)).thenReturn(listing);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setOrderId(500L);
              return order;
            });
    when(stripeService.createCheckoutSession(anyList(), eq("500"))).thenReturn("https://pay/500");

    Map<String, Object> payload = payload(List.of(Map.of("listingId", 10L, "quantity", 2)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentUrl").value("https://pay/500"))
        .andExpect(jsonPath("$.orderIds[0]").value(500));

    assertEquals(8, listing.getInventory().getQtyAvailable());
    assertEquals(2, listing.getInventory().getQtyReserved());
  }

  @Test
  void startMobileCheckout_success_multiStore_createsMultipleOrders() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);

    Listing listingA = listing(11L, store(1L, "Store A"), new BigDecimal("3.00"), 5, 0);
    Listing listingB = listing(12L, store(2L, "Store B"), new BigDecimal("4.00"), 5, 0);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(11L)).thenReturn(listingA);
    when(listingRepository.findByIdForUpdate(12L)).thenReturn(listingB);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            new org.mockito.stubbing.Answer<Order>() {
              private long id = 700L;

              @Override
              public Order answer(org.mockito.invocation.InvocationOnMock invocation) {
                Order order = invocation.getArgument(0);
                order.setOrderId(id++);
                return order;
              }
            });
    when(stripeService.createCheckoutSession(anyList(), eq("700,701")))
        .thenReturn("https://pay/multi");

    Map<String, Object> payload =
        payload(
            List.of(
                Map.of("listingId", 11L, "quantity", 1), Map.of("listingId", 12L, "quantity", 2)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentUrl").value("https://pay/multi"))
        .andExpect(jsonPath("$.orderIds.length()").value(2));

    verify(orderRepository, times(2)).save(any(Order.class));
  }

  @Test
  void startMobileCheckout_paymentServiceUnavailable_returns503() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    Listing listing = listing(20L, store(3L, "Store C"), new BigDecimal("5.00"), 5, 0);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(20L)).thenReturn(listing);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setOrderId(900L);
              return order;
            });
    when(stripeService.createCheckoutSession(anyList(), eq("900")))
        .thenThrow(new IllegalStateException("Stripe is not configured"));

    Map<String, Object> payload = payload(List.of(Map.of("listingId", 20L, "quantity", 1)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void startMobileCheckout_stripeError_returns500() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    Listing listing = listing(30L, store(4L, "Store D"), new BigDecimal("6.00"), 5, 0);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(30L)).thenReturn(listing);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setOrderId(901L);
              return order;
            });
    when(stripeService.createCheckoutSession(anyList(), eq("901")))
        .thenThrow(new RuntimeException("Stripe down"));

    Map<String, Object> payload = payload(List.of(Map.of("listingId", 30L, "quantity", 1)));

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void startMobileCheckout_orderSavedWithPickupSlotsFromRequest() throws Exception {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(1L);
    Listing listing = listing(40L, store(5L, "Store E"), new BigDecimal("7.00"), 10, 0);

    when(consumerProfileRepository.findById(1L)).thenReturn(Optional.of(consumer));
    when(listingRepository.findByIdForUpdate(40L)).thenReturn(listing);
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setOrderId(902L);
              return order;
            });
    when(stripeService.createCheckoutSession(anyList(), eq("902"))).thenReturn("https://pay/902");

    LocalDateTime start = LocalDateTime.now().plusHours(3);
    LocalDateTime end = LocalDateTime.now().plusHours(4);
    Map<String, Object> payload =
        Map.of(
            "items", List.of(Map.of("listingId", 40L, "quantity", 1)),
            "pickupSlotStart", start.toString(),
            "pickupSlotEnd", end.toString());

    mockMvc
        .perform(
            post("/api/mobile/checkout/start")
                .sessionAttr("USER_ID", 1L)
                .sessionAttr("USER_ROLE", "CONSUMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk());

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());
    assertEquals(start, orderCaptor.getValue().getPickupSlotStart());
    assertEquals(end, orderCaptor.getValue().getPickupSlotEnd());
    assertNotNull(orderCaptor.getValue().getPickupToken());
    assertNotNull(orderCaptor.getValue().getPickupToken().getQrTokenHash());
  }

  private static Map<String, Object> payload(List<Map<String, Object>> items) {
    return Map.of(
        "items", items,
        "pickupSlotStart", LocalDateTime.now().plusHours(1).toString(),
        "pickupSlotEnd", LocalDateTime.now().plusHours(2).toString());
  }

  private static Listing listing(
      Long id, Store store, BigDecimal rescuePrice, int qtyAvailable, int qtyReserved) {
    Listing listing = new Listing();
    listing.setListingId(id);
    listing.setStore(store);
    listing.setTitle("Listing " + id);
    listing.setOriginalPrice(rescuePrice.multiply(new BigDecimal("2")));
    listing.setRescuePrice(rescuePrice);
    listing.setPickupStart(LocalDateTime.now().plusHours(1));
    listing.setPickupEnd(LocalDateTime.now().plusHours(2));
    listing.setExpiryAt(LocalDateTime.now().plusHours(4));
    listing.setStatus("ACTIVE");

    Inventory inventory = new Inventory();
    inventory.setListing(listing);
    inventory.setQtyAvailable(qtyAvailable);
    inventory.setQtyReserved(qtyReserved);
    listing.setInventory(inventory);
    return listing;
  }

  private static Store store(Long id, String name) {
    Store store = new Store();
    store.setStoreId(id);
    store.setStoreName(name);
    return store;
  }
}
