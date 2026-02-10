package com.frh.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void createCheckoutSession_success_returnsUrl() throws Exception {
        StripeService service = new StripeService();
        ReflectionTestUtils.setField(service, "stripeSecretKey", "sk_test_123");

        StripeService.StripeLineItem first = new StripeService.StripeLineItem("Bread", 2, new BigDecimal("3.50"));
        StripeService.StripeLineItem second = new StripeService.StripeLineItem("Soup", 1, new BigDecimal("4.20"));

        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSession.getUrl()).thenReturn("https://checkout.stripe.test/session_123");

        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class)) {
            sessionMock
                .when(() -> Session.create(Mockito.any(SessionCreateParams.class)))
                .thenReturn(mockSession);

            String url = service.createCheckoutSession(List.of(first, second), "1001");

            assertEquals("https://checkout.stripe.test/session_123", url);
            assertEquals("sk_test_123", Stripe.apiKey);
            sessionMock.verify(() -> Session.create(Mockito.any(SessionCreateParams.class)));
        }
    }

    @Test
    void createCheckoutSession_whenStripeThrows_propagatesException() {
        StripeService service = new StripeService();
        ReflectionTestUtils.setField(service, "stripeSecretKey", "sk_test_123");

        StripeService.StripeLineItem item =
            new StripeService.StripeLineItem("Noodles", 1, new BigDecimal("6.30"));

        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class)) {
            sessionMock
                .when(() -> Session.create(Mockito.any(SessionCreateParams.class)))
                .thenThrow(new ApiConnectionException("Stripe API unavailable"));

            ApiConnectionException ex = assertThrows(
                ApiConnectionException.class,
                () -> service.createCheckoutSession(List.of(item), "1002")
            );

            assertTrue(ex.getMessage().contains("Stripe API unavailable"));
        }
    }
}
