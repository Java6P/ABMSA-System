package com.abmsa.service;

import com.abmsa.entity.*;
import com.abmsa.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final CrawledTweetMapper crawledTweetMapper;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final SubTargetMapper subTargetMapper;
    private final TargetExtractService targetExtractService;
    private final ModelCallService modelCallService;

    @Value("${target.extract.strategy:RULE_FIRST}")
    private String extractStrategy;

    private static final Random RANDOM = new Random();
    private static final int MOCK_TWEET_MIN = 5;
    private static final int MOCK_TWEET_RANGE = 6;

    private static final String[] MOCK_TWEET_TEMPLATES = {
            "Just read about %s - absolutely fascinating! The implications are huge.",
            "Wow, %s is trending again. Not sure how I feel about this.",
            "Breaking: new developments around %s surfaced today. Thoughts?",
            "Why is nobody talking about what %s means for the future?",
            "My take on %s after reading the latest reports: cautiously optimistic.",
            "The media coverage of %s has been completely one-sided lately.",
            "I've been following %s for months. Today's update changes everything.",
            "Hot take: %s is overhyped and the reality is much more nuanced.",
            "Incredible progress with %s! This is exactly what we needed.",
            "Disappointed by how %s is being handled. We deserve better.",
            "Finally, some good news about %s! Very encouraging signs.",
            "%s has really been in the spotlight this week. What do you all think?",
            "Can't stop thinking about the recent %s situation. Very concerning.",
            "Everyone's talking about %s today - here's my perspective.",
            "The latest %s update is a game-changer. Excited to see what comes next!"
    };

    private static final String[] MOCK_AUTHOR_NAMES = {
            "TechReporter", "SocialWatcher", "DataInsight", "NewsDigest",
            "AnalyticsBot", "TrendTracker", "OpinionHub", "MediaPulse",
            "InfoStream", "PublicVoice", "DigitalDesk", "NetSentinel"
    };

    /**
     * Simulates crawling tweets for a given keyword.
     * Generates 5-10 mock tweets containing the keyword.
     */
    public List<CrawledTweet> crawlTweets(Long keywordId, String keyword) {
        int count = MOCK_TWEET_MIN + RANDOM.nextInt(MOCK_TWEET_RANGE);
        List<CrawledTweet> tweets = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            CrawledTweet tweet = new CrawledTweet();
            tweet.setKeywordId(keywordId);
            tweet.setTweetId(UUID.randomUUID().toString());
            String template = MOCK_TWEET_TEMPLATES[RANDOM.nextInt(MOCK_TWEET_TEMPLATES.length)];
            tweet.setContent(String.format(template, keyword));
            tweet.setAuthorName(MOCK_AUTHOR_NAMES[RANDOM.nextInt(MOCK_AUTHOR_NAMES.length)]);
            tweet.setPublishTime(LocalDateTime.now().minusMinutes(RANDOM.nextInt(60)));
            tweet.setIsAnalyzed(0);
            crawledTweetMapper.insert(tweet);
            tweets.add(tweet);
        }
        log.info("Crawled {} mock tweets for keyword '{}'", count, keyword);
        return tweets;
    }

    /**
     * Determines analysis targets for a tweet.
     * Checks user-defined sub-targets first; then applies extraction strategy.
     */
    public List<String> determineTargets(CrawledTweet tweet, Long keywordId) {
        List<SubTarget> subTargets = subTargetMapper.selectList(
                new LambdaQueryWrapper<SubTarget>()
                        .eq(SubTarget::getKeywordId, keywordId)
                        .eq(SubTarget::getIsEnabled, 1));

        if (!subTargets.isEmpty()) {
            return subTargets.stream().map(SubTarget::getTargetName).toList();
        }

        List<String> extracted = targetExtractService.extract(tweet.getContent(), extractStrategy);
        return extracted.isEmpty() ? List.of("general") : extracted;
    }

    /**
     * Processes a single tweet: determines targets, runs sentiment analysis, saves records.
     */
    public void processTweet(CrawledTweet tweet, Long keywordId) {
        try {
            List<String> targets = determineTargets(tweet, keywordId);
            for (String target : targets) {
                Map<String, Object> prediction = modelCallService.predict(
                        tweet.getContent(), target, tweet.getImageUrl());

                AnalysisRecord record = new AnalysisRecord();
                record.setCrawledTweetId(tweet.getId());
                record.setUserId(null);
                record.setInputText(tweet.getContent());
                record.setInputImageUrl(tweet.getImageUrl());
                record.setTarget(target);
                record.setKeywordId(keywordId);
                record.setAnalysisType("AUTO");
                record.setTargetSource("AUTO_EXTRACTED");
                record.setSentiment(String.valueOf(prediction.getOrDefault("sentiment", "neutral")));
                record.setConfidence(toDouble(prediction.get("confidence")));
                record.setPositiveProb(toDouble(prediction.get("positive_prob")));
                record.setNeutralProb(toDouble(prediction.get("neutral_prob")));
                record.setNegativeProb(toDouble(prediction.get("negative_prob")));
                analysisRecordMapper.insert(record);
            }

            // Mark tweet as analyzed
            tweet.setIsAnalyzed(1);
            crawledTweetMapper.updateById(tweet);
        } catch (Exception e) {
            log.error("Failed to process tweet id={}: {}", tweet.getId(), e.getMessage(), e);
        }
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception ex) { return 0.0; }
    }
}
