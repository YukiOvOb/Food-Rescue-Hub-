package com.frh.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ModelBehaviorTest {

  @Test
  void listingStats_calculateMetrics_withViews() {
    ListingStats stats = new ListingStats();
    stats.setViewCount(20);
    stats.setClickCount(5);
    stats.setOrderCount(2);

    stats.calculateMetrics();

    assertEquals(new BigDecimal("0.2500"), stats.getCtr());
    assertEquals(new BigDecimal("0.1000"), stats.getCvr());
  }

  @Test
  void listingStats_calculateMetrics_withoutViews_setsZero() {
    ListingStats stats = new ListingStats();
    stats.setViewCount(0);
    stats.setClickCount(5);
    stats.setOrderCount(2);

    stats.calculateMetrics();

    assertEquals(BigDecimal.ZERO, stats.getCtr());
    assertEquals(BigDecimal.ZERO, stats.getCvr());
  }

  @Test
  void listingStats_calculateMetrics_withNullViews_setsZero() {
    ListingStats stats = new ListingStats();
    stats.setViewCount(null);
    stats.setClickCount(5);
    stats.setOrderCount(2);

    stats.calculateMetrics();

    assertEquals(BigDecimal.ZERO, stats.getCtr());
    assertEquals(BigDecimal.ZERO, stats.getCvr());
  }

  @Test
  void consumerStats_calculateAvgOrderValue() {
    ConsumerStats stats = new ConsumerStats();
    stats.setCompletedOrders(4);
    stats.setTotalSpend(new BigDecimal("22.00"));
    stats.calculateAvgOrderValue();
    assertEquals(new BigDecimal("5.50"), stats.getAvgOrderValue());

    stats.setCompletedOrders(0);
    stats.calculateAvgOrderValue();
    assertEquals(BigDecimal.ZERO, stats.getAvgOrderValue());

    stats.setCompletedOrders(null);
    stats.setTotalSpend(new BigDecimal("12.00"));
    stats.calculateAvgOrderValue();
    assertEquals(BigDecimal.ZERO, stats.getAvgOrderValue());
  }

  @Test
  void consumerStats_calculateAvgOrderValue_withNullTotalSpend_setsZero() {
    ConsumerStats stats = new ConsumerStats();
    stats.setCompletedOrders(2);
    stats.setTotalSpend(null);

    stats.calculateAvgOrderValue();

    assertEquals(BigDecimal.ZERO, stats.getAvgOrderValue());
  }

  @Test
  void storeRating_validateRating_allowsRangeAndRejectsInvalid() {
    StoreRating valid = new StoreRating();
    valid.setRating(new BigDecimal("4.50"));
    valid.validateRating();

    StoreRating tooLow = new StoreRating();
    tooLow.setRating(new BigDecimal("0.99"));
    assertThrows(IllegalArgumentException.class, tooLow::validateRating);

    StoreRating tooHigh = new StoreRating();
    tooHigh.setRating(new BigDecimal("5.01"));
    assertThrows(IllegalArgumentException.class, tooHigh::validateRating);

    StoreRating missing = new StoreRating();
    missing.setRating(null);
    assertThrows(IllegalArgumentException.class, missing::validateRating);
  }

  @Test
  void wallet_addAndSubtractBalance() {
    Wallet wallet = new Wallet();
    wallet.setBalance(new BigDecimal("10.00"));

    wallet.addToBalance(new BigDecimal("5.50"));
    wallet.subtractFromBalance(new BigDecimal("3.00"));

    assertEquals(new BigDecimal("12.50"), wallet.getBalance());
  }

  @Test
  void testPerson_constructorsAndAccessors() {
    TestPerson person = new TestPerson("Amy", 22, "F");
    person.setId(9L);
    person.setName("Ann");
    person.setAge(23);
    person.setGender("Female");

    assertEquals(9L, person.getId());
    assertEquals("Ann", person.getName());
    assertEquals(23, person.getAge());
    assertEquals("Female", person.getGender());
  }

  @Test
  void userStoreInteractionId_equalsAndHashCode() {
    UserStoreInteraction.UserStoreInteractionId id1 =
        new UserStoreInteraction.UserStoreInteractionId(1L, 2L);
    UserStoreInteraction.UserStoreInteractionId id2 =
        new UserStoreInteraction.UserStoreInteractionId(1L, 2L);
    UserStoreInteraction.UserStoreInteractionId id3 =
        new UserStoreInteraction.UserStoreInteractionId(2L, 3L);
    UserStoreInteraction.UserStoreInteractionId id4 =
        new UserStoreInteraction.UserStoreInteractionId(1L, 3L);

    assertEquals(id1, id2);
    assertEquals(id1.hashCode(), id2.hashCode());
    assertNotEquals(id1, id3);
    assertNotEquals(id1, id4);
    assertEquals(id1, id1);
    assertNotEquals(id1, null);
    assertNotEquals(id1, "not-an-id");
  }

  @Test
  void listingFoodCategoryId_equalsAndHashCode() {
    ListingFoodCategoryId id1 = new ListingFoodCategoryId();
    id1.setListingId(10L);
    id1.setCategoryId(20L);

    ListingFoodCategoryId id2 = new ListingFoodCategoryId();
    id2.setListingId(10L);
    id2.setCategoryId(20L);

    ListingFoodCategoryId differentListing = new ListingFoodCategoryId();
    differentListing.setListingId(11L);
    differentListing.setCategoryId(20L);

    ListingFoodCategoryId differentCategory = new ListingFoodCategoryId();
    differentCategory.setListingId(10L);
    differentCategory.setCategoryId(21L);

    assertEquals(id1, id1);
    assertEquals(id1, id2);
    assertEquals(id1.hashCode(), id2.hashCode());
    assertNotEquals(id1, differentListing);
    assertNotEquals(id1, differentCategory);
    assertNotEquals(id1, null);
    assertNotEquals(id1, "not-an-id");
  }

  @Test
  void pickupToken_isValid_checksUsedAndExpiry() {
    PickupToken token = new PickupToken();
    token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
    token.setUsedAt(null);
    assertTrue(token.isValid());

    token.setUsedAt(LocalDateTime.now());
    assertFalse(token.isValid());
  }

  @Test
  void pickupToken_isValid_expiredToken_returnsFalse() {
    PickupToken token = new PickupToken();
    token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
    token.setUsedAt(null);

    assertFalse(token.isValid());
  }

  @Test
  void listing_availableQty_handlesMissingInventoryAndNullQuantity() {
    Listing listing = new Listing();
    assertEquals(0, listing.getAvailableQty());

    listing.setAvailableQty(6);
    assertEquals(6, listing.getAvailableQty());

    listing.getInventory().setQtyAvailable(null);
    assertEquals(0, listing.getAvailableQty());
  }

  @Test
  void orderItem_calculateTotal_onlyWhenInputsPresent() {
    OrderItem item = new OrderItem();
    item.setUnitPrice(new BigDecimal("3.50"));
    item.setQuantity(4);
    item.calculateTotal();
    assertEquals(new BigDecimal("14.00"), item.getLineTotal());

    item.setLineTotal(BigDecimal.ZERO);
    item.setUnitPrice(null);
    item.calculateTotal();
    assertEquals(BigDecimal.ZERO, item.getLineTotal());

    item.setUnitPrice(new BigDecimal("3.50"));
    item.setQuantity(null);
    item.calculateTotal();
    assertEquals(BigDecimal.ZERO, item.getLineTotal());
  }
}

