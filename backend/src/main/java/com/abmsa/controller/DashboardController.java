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

    @GetMapping("/keyword-stats/{id}")
    public Result<Map<String, Object>> keywordStats(@PathVariable Long id) {
        return Result.success(dashboardService.getKeywordStats(id));
    }

    @GetMapping("/target-compare/{keywordId}")
    public Result<List<Map<String, Object>>> targetCompare(@PathVariable Long keywordId) {
        return Result.success(dashboardService.getTargetCompare(keywordId));
    }

    @GetMapping("/word-cloud/{keywordId}")
    public Result<List<Map<String, Object>>> wordCloud(@PathVariable Long keywordId) {
        return Result.success(dashboardService.getWordCloud(keywordId));
    }
}
