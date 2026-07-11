package com.pr_reviewer.pr_reviewer.webhook;
import com.pr_reviewer.pr_reviewer.auth.GitHubInstallationTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/webhook")
public class GitHubWebhookController {
    private final WebhookSignatureVerifier signatureVerifier;

    private final String webhookSecret;
    private final ObjectMapper objectMapper;
    private final GitHubInstallationTokenService installationTokenService;

    public GitHubWebhookController(
            WebhookSignatureVerifier signatureVerifier,
            @Value("${github.webhook.secret}") String webhookSecret, ObjectMapper objectMapper, GitHubInstallationTokenService gitHubInstallationTokenService) {

        this.signatureVerifier = signatureVerifier;
        this.webhookSecret = webhookSecret;
        this.objectMapper = objectMapper;
        this.installationTokenService = gitHubInstallationTokenService;
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
            PullRequestWebhookPayload payload = objectMapper.readValue(rawPayLoad, PullRequestWebhookPayload.class);

            if("opened".equals(payload.action()) || "synchronize".equals(payload.action())) {
                 String token = installationTokenService.getInstallationToken(payload.installation().id());

                System.out.println("Got installation token for PR #" + payload.pullRequest().number()
                        + " in " + payload.repository().owner().login() + "/" + payload.repository().name()
                        + " — token starts with: " + token.substring(0, 8) + "...");
            }
        }

        return ResponseEntity.ok("received");
    }
}
