package com.example.hobbiesmanagment.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    // OpenRouter — free tier, no credit card required
    private static final String OPENROUTER_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    // Free models on OpenRouter (updated July 2026)
    private static final String MODEL = "google/gemma-4-31b-it:free";
    private static final String FALLBACK_MODEL = "google/gemma-4-26b-a4b-it:free";

    // System prompt — gives the AI context about SkillMate
    private static final String SYSTEM_CONTEXT =
            "You are SkillMate AI, a friendly skill advisor for the SkillMate platform. " +
            "SkillMate connects learners with mentors who teach skills like programming, music, cooking, painting, fitness, baking, singing, and more. " +
            "Your role is to help users figure out which skill to learn, suggest relevant skills on the platform, " +
            "and guide them toward finding the right mentor. " +
            "Keep your answers short, friendly, and practical. " +
            "Always respond in the same language the user wrote in.";

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the user message to OpenRouter and returns the model's text reply.
     * Uses OpenAI-compatible /chat/completions format.
     */
    public String ask(String userMessage) {
        Map<String, Object> systemMsg = Map.of(
                "role", "system",
                "content", SYSTEM_CONTEXT
        );

        Map<String, Object> userMsg = Map.of(
                "role", "user",
                "content", userMessage
        );

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(systemMsg, userMsg),
                "max_tokens", 512,
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:5173");
        headers.set("X-Title", "SkillMate");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(OPENROUTER_URL, HttpMethod.POST, entity, Map.class);
            return extractText(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("OpenRouter HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Try fallback model if primary is rate-limited
            if (e.getStatusCode().value() == 429) {
                try {
                    Map<String, Object> fallbackBody = Map.of(
                            "model", FALLBACK_MODEL,
                            "messages", List.of(systemMsg, userMsg),
                            "max_tokens", 512,
                            "temperature", 0.7
                    );
                    HttpEntity<Map<String, Object>> fallbackEntity = new HttpEntity<>(fallbackBody, headers);
                    ResponseEntity<Map> fallbackResponse = restTemplate.exchange(OPENROUTER_URL, HttpMethod.POST, fallbackEntity, Map.class);
                    return extractText(fallbackResponse.getBody());
                } catch (Exception fallbackEx) {
                    log.error("Fallback model also failed: {}", fallbackEx.getMessage());
                    throw new RuntimeException("Failed to reach AI: " + fallbackEx.getMessage(), fallbackEx);
                }
            }
            throw new RuntimeException("Failed to reach AI: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling OpenRouter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reach AI: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the text from OpenRouter's OpenAI-compatible response.
     * Response shape: { choices: [ { message: { content: "..." } } ] }
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map responseBody) {
        if (responseBody == null) return "Sorry, I couldn't process your request.";

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) responseBody.get("choices");

        if (choices == null || choices.isEmpty())
            return "Sorry, I received no response from the AI.";

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return "Sorry, the response was empty.";

        Object content = message.get("content");
        return content != null ? content.toString().trim() : "Sorry, I couldn't generate a response.";
    }
}
