package com.abmsa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetExtractService {

    private final ModelCallService modelCallService;

    @Value("${target.extract.strategy:RULE_FIRST}")
    private String defaultStrategy;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
    private static final Pattern CAPITALIZED_PATTERN =
            Pattern.compile("\\b([A-Z][a-z]{2,})(?:\\s+[A-Z][a-z]{2,})*\\b");

    /**
     * Rule-based extraction: @mentions, #hashtags, and capitalized noun phrases.
     */
    public List<String> extractByRules(String content) {
        List<String> targets = new ArrayList<>();
        if (!StringUtils.hasText(content)) {
            return targets;
        }

        Matcher mentionMatcher = MENTION_PATTERN.matcher(content);
        while (mentionMatcher.find()) {
            targets.add(mentionMatcher.group(1));
        }

        Matcher hashtagMatcher = HASHTAG_PATTERN.matcher(content);
        while (hashtagMatcher.find()) {
            targets.add(hashtagMatcher.group(1));
        }

        Matcher capMatcher = CAPITALIZED_PATTERN.matcher(content);
        while (capMatcher.find()) {
            String candidate = capMatcher.group(0).trim();
            // Exclude sentence-start false positives (simple heuristic)
            if (candidate.length() > 2 && !targets.contains(candidate)) {
                targets.add(candidate);
            }
        }

        return targets.stream().distinct().toList();
    }

    /**
     * Model-based extraction via the Flask service.
     */
    public List<String> extractByModel(String content) {
        return modelCallService.extractTargets(content);
    }

    /**
     * Extract targets using the configured strategy:
     *  - RULE_FIRST  : rule-based first; fall back to model if empty
     *  - MODEL_FIRST : model-based first; fall back to rules if empty
     *  - RULE_ONLY   : rule-based only
     *  - MODEL_ONLY  : model-based only
     */
    public List<String> extract(String content, String strategy) {
        String strat = StringUtils.hasText(strategy) ? strategy : defaultStrategy;
        return switch (strat.toUpperCase()) {
            case "MODEL_FIRST" -> {
                List<String> result = extractByModel(content);
                yield result.isEmpty() ? extractByRules(content) : result;
            }
            case "RULE_ONLY" -> extractByRules(content);
            case "MODEL_ONLY" -> extractByModel(content);
            default -> { // RULE_FIRST
                List<String> result = extractByRules(content);
                yield result.isEmpty() ? extractByModel(content) : result;
            }
        };
    }
}
