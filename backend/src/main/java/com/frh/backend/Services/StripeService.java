package com.frh.backend.Services; // ✅ 对应你的 Services 文件夹

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * create payment session can merge many orders
     * @param amount total payment amount
     * @param orderReference "orderId"
     */
    public String createCheckoutSession(BigDecimal amount, String orderReference) throws StripeException {

        // Stripe's API design requires that all amounts be transmitted as integers in the currency's smallest unit
        //convert to cents
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)

                // take orderID to APP
                // format: frhapp://payment/success?order_ids=101,102
                .setSuccessUrl("frhapp://payment/success?order_ids=" + orderReference)

                // cancel
                .setCancelUrl("frhapp://payment/cancel")

                // display info of items
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("sgd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                // display: "Food Rescue Orders: 101,102"
                                                                .setName("Food Rescue Orders: " + orderReference)
                                                                .build())
                                                .build())
                                .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}