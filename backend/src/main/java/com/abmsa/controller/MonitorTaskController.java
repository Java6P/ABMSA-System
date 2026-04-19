package com.abmsa.controller;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;
import com.abmsa.entity.CrawledTweet;
import com.abmsa.entity.CrawlLog;
import com.abmsa.entity.MonitorTask;
import com.abmsa.mapper.CrawlLogMapper;
import com.abmsa.service.MonitorTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorTaskController {

    private final MonitorTaskService monitorTaskService;
    private final CrawlLogMapper crawlLogMapper;

    @GetMapping("/tasks")
    public Result<List<MonitorTask>> listTasks(
            @RequestParam(required = false) Long keywordId) {
        return monitorTaskService.listTasks(keywordId);
    }

    @GetMapping("/task/{id}")
    public Result<MonitorTask> getTask(@PathVariable Long id) {
        return monitorTaskService.getTask(id);
    }

    @GetMapping("/task/{id}/tweets")
    public Result<PageResult<CrawledTweet>> getTaskTweets(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return monitorTaskService.getTaskTweets(id, page, size);
    }

    @GetMapping("/task/{id}/results")
    public Result<List<AnalysisRecord>> getTaskResults(@PathVariable Long id) {
        return monitorTaskService.getTaskResults(id);
    }

    @PostMapping("/trigger/{keywordId}")
    public Result<MonitorTask> triggerCrawl(@PathVariable Long keywordId) {
        return monitorTaskService.triggerCrawl(keywordId);
    }

    @GetMapping("/logs")
    public Result<List<CrawlLog>> getLogs(
            @RequestParam(required = false) Long keywordId) {
        LambdaQueryWrapper<CrawlLog> wrapper = new LambdaQueryWrapper<CrawlLog>()
                .eq(keywordId != null, CrawlLog::getKeywordId, keywordId)
                .orderByDesc(CrawlLog::getCreatedAt);
        return Result.success(crawlLogMapper.selectList(wrapper));
    }
}
