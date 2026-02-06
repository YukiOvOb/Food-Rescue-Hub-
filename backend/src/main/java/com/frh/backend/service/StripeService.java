package com.frh.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class StripeService {
    
    // DTO for passing line item details
    public static class StripeLineItem {
        public String title;
        public int quantity;
        public BigDecimal unitPrice;
        
        public StripeLineItem(String title, int quantity, BigDecimal unitPrice) {
            this.title = title;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    /**
     * Create payment session with individual line items showing listing titles
     * @param lineItems List of items with title, quantity, and price
     * @param orderReference "orderId" for success URL
     */
    public String createCheckoutSession(List<StripeLineItem> lineItems, String orderReference) throws StripeException {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("frhapp://payment/success?order_ids=" + orderReference)
                .setCancelUrl("frhapp://payment/cancel");
        
        // Add each listing as a separate line item
        for (StripeLineItem item : lineItems) {
            long unitAmountInCents = item.unitPrice.multiply(new BigDecimal(100)).longValue();
            
            paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.quantity)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("sgd")
                            .setUnitAmount(unitAmountInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.title)  // Use actual listing title
                                    .build())
                            .build())
                    .build());
        }
        
        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

}
