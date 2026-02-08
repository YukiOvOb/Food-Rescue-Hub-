package com.frh.backend.service;

import com.stripe.Stripe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StripeServiceTest {

    @AfterEach
    void resetStripeApiKey() {
        Stripe.apiKey = null;
    }

    @Test
    void init_setsStripeApiKeyWhenConfigured() {
        StripeService service = new StripeService();
        ReflectionTestUtils.setField(service, "stripeSecretKey", "sk_test_123");

        service.init();

        assertEquals("sk_test_123", Stripe.apiKey);
    }

    @Test
    void init_doesNotSetStripeApiKeyWhenBlank() {
        Stripe.apiKey = "existing";
        StripeService service = new StripeService();
        ReflectionTestUtils.setField(service, "stripeSecretKey", " ");

        service.init();

        assertEquals("existing", Stripe.apiKey);
    }

    @Test
    void createCheckoutSession_withoutKey_throwsIllegalState() {
        StripeService service = new StripeService();
        ReflectionTestUtils.setField(service, "stripeSecretKey", "");

        StripeService.StripeLineItem lineItem = new StripeService.StripeLineItem("Bread", 2, new BigDecimal("3.50"));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.createCheckoutSession(List.of(lineItem), "1,2")
        );

        assertEquals("Stripe is not configured", ex.getMessage());
        assertEquals("Bread", lineItem.title);
        assertEquals(2, lineItem.quantity);
        assertEquals(new BigDecimal("3.50"), lineItem.unitPrice);
    }
}
