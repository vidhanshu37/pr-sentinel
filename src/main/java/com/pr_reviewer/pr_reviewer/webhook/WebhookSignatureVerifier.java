package com.pr_reviewer.pr_reviewer.webhook;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Executable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class WebhookSignatureVerifier {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    public boolean isValid(String payloadBody, String signatureHeader, String webhookSecret) {
        if(signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }

        String expectedSignature = computeHmacSha256(payloadBody, webhookSecret);
        String providedSignature = signatureHeader.substring("sha256=".length());

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC SHA256", e);
        }
    }
}
