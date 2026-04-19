package com.abmsa.service;

import com.abmsa.entity.AnalysisRecord;
import com.abmsa.mapper.AnalysisRecordMapper;
import com.abmsa.mapper.CrawledTweetMapper;
import com.abmsa.mapper.KeywordMapper;
import com.abmsa.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnalysisRecordMapper analysisRecordMapper;
    private final CrawledTweetMapper crawledTweetMapper;
    private final KeywordMapper keywordMapper;
    private final UserMapper userMapper;

    private static final int WORD_CLOUD_MAX_WORDS = 50;
    private static final int MIN_WORD_LENGTH = 3;
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalUsers", userMapper.selectCount(null));
        overview.put("totalKeywords", keywordMapper.selectCount(null));
        overview.put("totalTweets", crawledTweetMapper.selectCount(null));
        overview.put("totalAnalyses", analysisRecordMapper.selectCount(null));

        // Sentiment breakdown
        Map<String, Long> sentimentCounts = getSentimentCounts(null);
        overview.put("sentimentBreakdown", sentimentCounts);
        return overview;
    }

    /**
     * Returns daily sentiment trend data for the specified period.
     *
     * @param keywordId filter by keyword (nullable)
     * @param period    "7d" | "30d" | "90d"
     */
    public List<Map<String, Object>> getSentimentTrend(Long keywordId, String period) {
        int days = parsePeriod(period);
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<AnalysisRecord>()
                .ge(AnalysisRecord::getCreatedAt, since)
                .eq(keywordId != null, AnalysisRecord::getKeywordId, keywordId);

        List<AnalysisRecord> records = analysisRecordMapper.selectList(wrapper);

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Map<String, Long>> dailyMap = new LinkedHashMap<>();

        for (int i = days - 1; i >= 0; i--) {
            String day = LocalDateTime.now().minusDays(i).format(dayFmt);
            dailyMap.put(day, new HashMap<>(Map.of("positive", 0L, "neutral", 0L, "negative", 0L)));
        }

        for (AnalysisRecord r : records) {
            if (r.getCreatedAt() == null || r.getSentiment() == null) continue;
            String day = r.getCreatedAt().format(dayFmt);
            dailyMap.computeIfAbsent(day,
                    k -> new HashMap<>(Map.of("positive", 0L, "neutral", 0L, "negative", 0L)));
            dailyMap.get(day).merge(r.getSentiment(), 1L, Long::sum);
        }

        return dailyMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("date", e.getKey());
                    point.putAll(e.getValue());
                    return point;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns statistics for a specific keyword.
     */
    public Map<String, Object> getKeywordStats(Long keywordId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("tweetCount", crawledTweetMapper.selectCount(
                new LambdaQueryWrapper<com.abmsa.entity.CrawledTweet>()
                        .eq(com.abmsa.entity.CrawledTweet::getKeywordId, keywordId)));
        stats.put("analysisCount", analysisRecordMapper.selectCount(
                new LambdaQueryWrapper<AnalysisRecord>()
                        .eq(AnalysisRecord::getKeywordId, keywordId)));
        stats.put("sentimentBreakdown", getSentimentCounts(keywordId));
        return stats;
    }

    /**
     * Returns per-target sentiment comparison for a keyword.
     */
    public List<Map<String, Object>> getTargetCompare(Long keywordId) {
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<AnalysisRecord>()
                .eq(keywordId != null, AnalysisRecord::getKeywordId, keywordId);
        List<AnalysisRecord> records = analysisRecordMapper.selectList(wrapper);

        Map<String, Map<String, Long>> targetMap = new LinkedHashMap<>();
        for (AnalysisRecord r : records) {
            if (r.getTarget() == null || r.getSentiment() == null) continue;
            targetMap.computeIfAbsent(r.getTarget(),
                    k -> new HashMap<>(Map.of("positive", 0L, "neutral", 0L, "negative", 0L)));
            targetMap.get(r.getTarget()).merge(r.getSentiment(), 1L, Long::sum);
        }

        return targetMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("target", e.getKey());
                    entry.putAll(e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns simple word-frequency data derived from tweet content for a keyword.
     */
    public List<Map<String, Object>> getWordCloud(Long keywordId) {
        LambdaQueryWrapper<com.abmsa.entity.CrawledTweet> wrapper =
                new LambdaQueryWrapper<com.abmsa.entity.CrawledTweet>()
                        .eq(keywordId != null, com.abmsa.entity.CrawledTweet::getKeywordId, keywordId);

        List<com.abmsa.entity.CrawledTweet> tweets = crawledTweetMapper.selectList(wrapper);
        Map<String, Long> freq = new LinkedHashMap<>();

        for (com.abmsa.entity.CrawledTweet tweet : tweets) {
            if (tweet.getContent() == null) continue;
            String[] words = tweet.getContent().toLowerCase()
                    .replaceAll("[^a-z0-9 ]", " ")
                    .split("\\s+");
            for (String word : words) {
                if (word.length() > MIN_WORD_LENGTH) {
                    freq.merge(word, 1L, Long::sum);
                }
            }
        }

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(WORD_CLOUD_MAX_WORDS)
                .map(e -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("name", e.getKey());
                    entry.put("value", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Long> getSentimentCounts(Long keywordId) {
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<AnalysisRecord>()
                .eq(keywordId != null, AnalysisRecord::getKeywordId, keywordId);
        List<AnalysisRecord> records = analysisRecordMapper.selectList(wrapper);
        Map<String, Long> counts = new HashMap<>(Map.of("positive", 0L, "neutral", 0L, "negative", 0L));
        for (AnalysisRecord r : records) {
            if (r.getSentiment() != null) {
                counts.merge(r.getSentiment(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private int parsePeriod(String period) {
        if (period == null) return 7;
        return switch (period.toLowerCase()) {
            case "30d" -> 30;
            case "90d" -> 90;
            default -> 7;
        };
    }
}
