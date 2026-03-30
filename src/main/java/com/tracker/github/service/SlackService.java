package com.tracker.github.service;

import com.tracker.github.entity.Author;
import com.tracker.github.entity.Commit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SlackService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendPushNotification(Author author, List<Commit> commits, String repoName) {
        StringBuilder sb = new StringBuilder();
        sb.append(":rocket: *New Push to `").append(repoName).append("`*\n");
        sb.append(":bust_in_silhouette: *Author:* ").append(author.getName())
          .append(" (").append(author.getEmail()).append(")\n\n");
        sb.append(":memo: *Commits:*\n");

        for (Commit c : commits) {
            sb.append("• `").append(c.getCommitId().substring(0, 7)).append("` - ")
              .append(c.getMessage()).append("\n");
        }

        String payload = "{\"text\": \"" + escapeJson(sb.toString()) + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(webhookUrl, request, String.class);
            System.out.println("Slack notification sent successfully!");
        } catch (Exception e) {
            System.err.println("Failed to send Slack notification: " + e.getMessage());
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\t", "\\t");
    }
}
