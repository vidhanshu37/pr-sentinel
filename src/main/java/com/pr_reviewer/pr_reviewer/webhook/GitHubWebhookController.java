package com.pr_reviewer.pr_reviewer.webhook;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// This controller handles GitHub webhook events, specifically pull request events. It verifies the signature of incoming requests to ensure they are from GitHub and processes the payload accordingly.
@RestController
@RequestMapping("/webhook")
public class GitHubWebhookController {
    private final WebhookSignatureVerifier signatureVerifier;

    private final String webhookSecret;

    public GitHubWebhookController(
            WebhookSignatureVerifier signatureVerifier,
            @Value("${github.webhook.secret}") String webhookSecret) {

        this.signatureVerifier = signatureVerifier;
        this.webhookSecret = webhookSecret;
    }


    @PostMapping("/github")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawPayLoad,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader("X-GitHub-Event") String eventType) {

        if (!signatureVerifier.isValid(rawPayLoad, signature, webhookSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        if ("pull_request".equals(eventType)) {
            System.out.println("Verified pull_request event received. Payload length: " + rawPayLoad.length());
        }

        return ResponseEntity.ok("received");
    }
}
