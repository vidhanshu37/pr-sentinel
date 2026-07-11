package com.pr_reviewer.pr_reviewer.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GitHubInstallationTokenService {
    private final GitHubAppJwtGenerator jwtGenerator;
    private final RestClient restClient = RestClient.create("https://api.github.com");

    private final Map<Long, CachedToken> tokenCache = new ConcurrentHashMap<>();

    public GitHubInstallationTokenService(GitHubAppJwtGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    public String getInstallationToken(Long installationId) {
        CachedToken cached = tokenCache.get(installationId);
        if(cached != null && cached.isStillValid()) {
            return cached.token();
        }

        return fetchAndCacheNewToken(installationId);
    }

    private String fetchAndCacheNewToken(Long installationId) {
        String appJwt = jwtGenerator.generateJwt();

        TokenResponse response = restClient.post()
                .uri("/app/installations/{id}/access_tokens", installationId)
                .header("Authorization", "Bearer " + appJwt)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(TokenResponse.class);

        Instant expiresAt = Instant.parse(response.expiresAt()).minusSeconds(120);
        tokenCache.put(installationId, new CachedToken(response.token(), expiresAt));
        return response.token();
    }


    private record CachedToken(String token, Instant expiresAt) {
        boolean isStillValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    private record TokenResponse(String token, @com.fasterxml.jackson.annotation.JsonProperty("expires_at") String expiresAt) {}

}
