package com.frh.backend.util;

import com.frh.backend.Model.Order;
import com.frh.backend.Model.PickupToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public final class PickupTokenGenerator {

    private PickupTokenGenerator() {
    }

    public static PickupToken createForOrder(Order order) {
        PickupToken pickupToken = new PickupToken();
        pickupToken.setOrder(order);
        pickupToken.setQrTokenHash(generateQrTokenHash());
        pickupToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        return pickupToken;
    }

    private static String generateQrTokenHash() {
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        return hashToken(rawToken);
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
