package com.pr_reviewer.pr_reviewer.review;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class NaivePrReviewer {
    private final ChatClient chatClient;

    public NaivePrReviewer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String review(String rawDiff) {
        return chatClient.prompt()
                .system("You are a senior software engineer reviewing a pull request. " +
                        "Give specific, actionable feedback on the code changes below.")
                .user(rawDiff)
                .call()
                .content();
    }
}
