package com.pr_reviewer.pr_reviewer.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequestWebhookPayload(
        String action,
        @JsonProperty("pull_request") PullRequest pullRequest,
        Repository repository,
        Installation installation
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequest(Long number) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(String name, Owner owner) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Owner(String login) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Installation(Long id) {}
}
