package com.abmsa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelCallService {

    private final RestTemplate restTemplate;

    @Value("${model.service.url}")
    private String modelServiceUrl;

    private static final String[] SENTIMENTS = {"positive", "neutral", "negative"};
    private static final Random RANDOM = new Random();
    private static final double MOCK_MIN_CONFIDENCE = 0.60;
    private static final double MOCK_CONFIDENCE_RANGE = 0.35;

    /**
     * Call Flask /api/predict for a single item.
     * Falls back to mock data if the service is unavailable.
     */
    public Map<String, Object> predict(String text, String target, String imageUrl) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            request.put("target", target != null ? target : "");
            if (imageUrl != null) request.put("image_url", imageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    modelServiceUrl + "/api/predict",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Model service unavailable, using mock data: {}", e.getMessage());
        }
        return generateMockPrediction(target);
    }

    /**
     * Call Flask /api/predict/batch.
     * Falls back to mock data if the service is unavailable.
     */
    public List<Map<String, Object>> predictBatch(List<Map<String, Object>> items) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("items", items);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    modelServiceUrl + "/api/predict/batch",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Model service unavailable for batch, using mock data: {}", e.getMessage());
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> item : items) {
            results.add(generateMockPrediction(String.valueOf(item.getOrDefault("target", ""))));
        }
        return results;
    }

    /**
     * Call Flask /api/extract-targets.
     * Falls back to empty list if the service is unavailable.
     */
    public List<String> extractTargets(String content) {
        try {
            Map<String, String> request = Map.of("content", content);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    modelServiceUrl + "/api/extract-targets",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object targets = response.getBody().get("targets");
                if (targets instanceof List<?> list) {
                    return list.stream().map(Object::toString).toList();
                }
            }
        } catch (Exception e) {
            log.warn("Model service unavailable for target extraction: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Check if the Flask model service is healthy.
     */
    public boolean isHealthy() {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    modelServiceUrl + "/api/health",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Model service health check failed: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Mock helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> generateMockPrediction(String target) {
        String sentiment = SENTIMENTS[RANDOM.nextInt(SENTIMENTS.length)];
        double confidence = MOCK_MIN_CONFIDENCE + RANDOM.nextDouble() * MOCK_CONFIDENCE_RANGE;
        double[] probs = randomProbs();

        Map<String, Object> result = new HashMap<>();
        result.put("sentiment", sentiment);
        result.put("confidence", Math.round(confidence * 1000.0) / 1000.0);
        result.put("positive_prob", Math.round(probs[0] * 1000.0) / 1000.0);
        result.put("neutral_prob", Math.round(probs[1] * 1000.0) / 1000.0);
        result.put("negative_prob", Math.round(probs[2] * 1000.0) / 1000.0);
        result.put("target", target);
        result.put("mock", true);
        return result;
    }

    private double[] randomProbs() {
        double a = RANDOM.nextDouble();
        double b = RANDOM.nextDouble();
        if (a > b) { double t = a; a = b; b = t; }
        double p0 = a;
        double p1 = b - a;
        double p2 = 1.0 - b;
        return new double[]{p0, p1, p2};
    }
}
