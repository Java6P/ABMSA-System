package com.abmsa.controller;

import com.abmsa.common.Result;
import com.abmsa.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/sentiment-trend")
    public Result<List<Map<String, Object>>> sentimentTrend(
            @RequestParam(required = false) Long keywordId,
            @RequestParam(defaultValue = "7d") String period) {
        return Result.success(dashboardService.getSentimentTrend(keywordId, period));
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(
            @RequestParam(required = false) Long keywordId,
            @RequestParam(defaultValue = "7d") String period) {
        return Result.success(dashboardService.getSentimentTrend(keywordId, period));
    }

    @GetMapping("/keyword-stats")
    public Result<Map<String, Object>> keywordStatsByParam(
            @RequestParam(required = false) Long keywordId,
            @RequestParam(required = false) Long id) {
        Long actualId = id != null ? id : keywordId;
        return Result.success(dashboardService.getKeywordStats(actualId != null ? actualId : 0L));
    }

    @GetMapping("/keyword/{id}/stats")
    public Result<Map<String, Object>> keywordStatsByPath(@PathVariable Long id) {
        return Result.success(dashboardService.getKeywordStats(id));
    }

    @GetMapping("/target-compare")
    public Result<List<Map<String, Object>>> targetCompareByParam(
            @RequestParam(required = false) Long keywordId,
            @RequestParam(required = false) Long id) {
        Long actualKeywordId = keywordId != null ? keywordId : id;
        return Result.success(dashboardService.getTargetCompare(actualKeywordId));
    }

    @GetMapping("/target-compare/{keywordId}")
    public Result<List<Map<String, Object>>> targetCompareByPath(@PathVariable Long keywordId) {
        return Result.success(dashboardService.getTargetCompare(keywordId));
    }

    @GetMapping("/word-cloud")
    public Result<List<Map<String, Object>>> wordCloudByParam(
            @RequestParam(required = false) Long keywordId,
            @RequestParam(required = false) Long id) {
        Long actualKeywordId = keywordId != null ? keywordId : id;
        return Result.success(dashboardService.getWordCloud(actualKeywordId));
    }

    @GetMapping("/word-cloud/{keywordId}")
    public Result<List<Map<String, Object>>> wordCloudByPath(@PathVariable Long keywordId) {
        return Result.success(dashboardService.getWordCloud(keywordId));
    }

    @GetMapping("/wordcloud")
    public Result<List<Map<String, Object>>> wordcloud(
            @RequestParam(required = false) Long keywordId,
            @RequestParam(required = false) Long id) {
        Long actualKeywordId = keywordId != null ? keywordId : id;
        return Result.success(dashboardService.getWordCloud(actualKeywordId));
    }
}
