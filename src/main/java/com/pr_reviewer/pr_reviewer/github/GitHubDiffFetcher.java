package com.pr_reviewer.pr_reviewer.github;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubDiffFetcher {
    private final RestClient restClient =  RestClient.create("https://api.github.com");

    public String fetchDiff(String owner, String repo, Long prNumber, String installationToken) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{prNumber}", owner, repo, prNumber)
                .header("Authorization", "Bearer " + installationToken)
                .header("Accept", "application/vnd.github.v3.diff")
                .retrieve()
                .body(String.class);
    }
}
