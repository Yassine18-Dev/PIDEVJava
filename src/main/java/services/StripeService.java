package services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StripeService {

    public StripeService() {
        String secretKey = System.getenv("STRIPE_SECRET_KEY");

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Variable STRIPE_SECRET_KEY introuvable.");
        }

        Stripe.apiKey = secretKey;
    }

    public Session createSession(int tournoiId, int userId, BigDecimal prix) throws Exception {
        long amount = prix
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://success.com?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://cancel.com")
                .putMetadata("tournoi_id", String.valueOf(tournoiId))
                .putMetadata("user_id", String.valueOf(userId))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Inscription tournoi #" + tournoiId)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        return Session.create(params);
    }

    public boolean isPaid(String sessionId) throws Exception {
        Session session = Session.retrieve(sessionId);
        return "paid".equalsIgnoreCase(session.getPaymentStatus());
    }
}