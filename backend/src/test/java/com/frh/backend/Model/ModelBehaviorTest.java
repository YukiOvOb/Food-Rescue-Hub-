package com.frh.backend.Model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void consumerStats_calculateAvgOrderValue() {
        ConsumerStats stats = new ConsumerStats();
        stats.setCompletedOrders(4);
        stats.setTotalSpend(new BigDecimal("22.00"));
        stats.calculateAvgOrderValue();
        assertEquals(new BigDecimal("5.50"), stats.getAvgOrderValue());

        stats.setCompletedOrders(0);
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

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
    }

    @Test
    void pickupToken_isValid_checksUsedAndExpiry() {
        PickupToken token = new PickupToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsedAt(null);
        assertTrue(token.isValid());

        token.setUsedAt(LocalDateTime.now());
        assertEquals(false, token.isValid());
    }
}
