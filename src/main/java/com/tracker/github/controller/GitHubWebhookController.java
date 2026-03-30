package com.tracker.github.controller;

import com.tracker.github.service.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class GitHubWebhookController {

    private final WebhookService webhookService;

    public GitHubWebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestBody Map<String, Object> payload) {

        if (!"push".equals(event)) {
            return ResponseEntity.ok("Event ignored: " + event);
        }

        webhookService.processPushEvent(payload);
        return ResponseEntity.ok("Push event processed successfully");
    }
}
