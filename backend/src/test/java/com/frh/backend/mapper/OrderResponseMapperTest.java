package com.frh.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.Order;
import com.frh.backend.Model.OrderItem;
import com.frh.backend.Model.PickupToken;
import com.frh.backend.Model.Store;
import com.frh.backend.dto.OrderResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderResponseMapperTest {

  private final OrderResponseMapper mapper = new OrderResponseMapper();

  @Test
  void toOrderResponseList_nullInput_returnsEmptyList() {
    List<OrderResponseDto> result = mapper.toOrderResponseList(null);
    assertTrue(result.isEmpty());
  }

  @Test
  void toOrderResponseList_withNullAndPopulatedOrder_mapsAllSupportedFields() {
    LocalDateTime now = LocalDateTime.now();

    Store store = new Store();
    store.setStoreId(7L);
    store.setStoreName("Downtown Bakery");
    store.setAddressLine("123 Main St");
    store.setPostalCode("123456");
    store.setLat(new BigDecimal("1.3000000"));
    store.setLng(new BigDecimal("103.8000000"));

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(9L);
    consumer.setDisplayName("Alice Tan");
    consumer.setDefault_lat(new BigDecimal("1.3521000"));
    consumer.setDefault_lng(new BigDecimal("103.8198000"));

    Listing listing = new Listing();
    listing.setListingId(88L);
    listing.setTitle("Surprise bread pack");

    OrderItem itemWithListing = new OrderItem();
    itemWithListing.setOrderItemId(1001L);
    itemWithListing.setQuantity(2);
    itemWithListing.setUnitPrice(new BigDecimal("4.50"));
    itemWithListing.setLineTotal(new BigDecimal("9.00"));
    itemWithListing.setListing(listing);

    OrderItem itemWithoutListing = new OrderItem();
    itemWithoutListing.setOrderItemId(1002L);
    itemWithoutListing.setQuantity(1);
    itemWithoutListing.setUnitPrice(new BigDecimal("3.00"));
    itemWithoutListing.setLineTotal(new BigDecimal("3.00"));

    PickupToken token = new PickupToken();
    token.setQrTokenHash("qr_hash_abc123");
    token.setExpiresAt(now.plusHours(2));

    Order order = new Order();
    order.setOrderId(55L);
    order.setStatus("READY");
    order.setTotalAmount(new BigDecimal("12.00"));
    order.setCurrency("SGD");
    order.setPickupSlotStart(now.plusHours(1));
    order.setPickupSlotEnd(now.plusHours(2));
    order.setCancelReason(null);
    order.setCreatedAt(now.minusDays(1));
    order.setUpdatedAt(now);
    order.setStore(store);
    order.setConsumer(consumer);
    order.setOrderItems(Arrays.asList(null, itemWithListing, itemWithoutListing));
    order.setPickupToken(token);

    List<OrderResponseDto> result = mapper.toOrderResponseList(Arrays.asList(null, order));

    assertEquals(2, result.size());

    OrderResponseDto nullMapped = result.get(0);
    assertNull(nullMapped.getOrderId());
    assertNull(nullMapped.getStore());
    assertNull(nullMapped.getConsumer());
    assertNotNull(nullMapped.getOrderItems());
    assertTrue(nullMapped.getOrderItems().isEmpty());

    OrderResponseDto mapped = result.get(1);
    assertEquals(55L, mapped.getOrderId());
    assertEquals("READY", mapped.getStatus());
    assertEquals(new BigDecimal("12.00"), mapped.getTotalAmount());
    assertEquals("SGD", mapped.getCurrency());
    assertEquals("Downtown Bakery", mapped.getStore().getStoreName());
    assertEquals("Alice Tan", mapped.getConsumer().getDisplayName());
    assertEquals("qr_hash_abc123", mapped.getPickupTokenHash());
    assertEquals(token.getExpiresAt(), mapped.getPickupTokenExpiresAt());

    assertEquals(3, mapped.getOrderItems().size());
    assertNull(mapped.getOrderItems().get(0).getOrderItemId());
    assertNull(mapped.getOrderItems().get(0).getListing());
    assertEquals(1001L, mapped.getOrderItems().get(1).getOrderItemId());
    assertEquals(88L, mapped.getOrderItems().get(1).getListing().getListingId());
    assertEquals("Surprise bread pack", mapped.getOrderItems().get(1).getListing().getTitle());
    assertEquals(1002L, mapped.getOrderItems().get(2).getOrderItemId());
    assertNull(mapped.getOrderItems().get(2).getListing());
  }

  @Test
  void toOrderResponse_withMissingNestedValues_keepsOptionalSectionsUnset() {
    Order order = new Order();
    order.setOrderId(77L);
    order.setStore(null);
    order.setConsumer(null);
    order.setOrderItems(null);
    order.setPickupToken(null);

    OrderResponseDto dto = mapper.toOrderResponse(order);

    assertEquals(77L, dto.getOrderId());
    assertNull(dto.getStore());
    assertNull(dto.getConsumer());
    assertNotNull(dto.getOrderItems());
    assertTrue(dto.getOrderItems().isEmpty());
    assertNull(dto.getPickupTokenHash());
    assertNull(dto.getPickupTokenExpiresAt());
  }
}
