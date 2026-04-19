package com.abmsa.service.impl;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.*;
import com.abmsa.mapper.*;
import com.abmsa.service.CrawlerService;
import com.abmsa.service.MonitorTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorTaskServiceImpl implements MonitorTaskService {

    private final MonitorTaskMapper monitorTaskMapper;
    private final CrawledTweetMapper crawledTweetMapper;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final KeywordMapper keywordMapper;
    private final CrawlLogMapper crawlLogMapper;
    private final CrawlerService crawlerService;

    @Override
    public Result<List<MonitorTask>> listTasks(Long keywordId) {
        List<MonitorTask> list = monitorTaskMapper.selectList(
                new LambdaQueryWrapper<MonitorTask>()
                        .eq(keywordId != null, MonitorTask::getKeywordId, keywordId)
                        .orderByDesc(MonitorTask::getCreatedAt));
        return Result.success(list);
    }

    @Override
    public Result<MonitorTask> getTask(Long id) {
        MonitorTask task = monitorTaskMapper.selectById(id);
        if (task == null) {
            return Result.error(404, "Task not found");
        }
        return Result.success(task);
    }

    @Override
    public Result<PageResult<CrawledTweet>> getTaskTweets(Long taskId, int page, int size) {
        Page<CrawledTweet> pageObj = crawledTweetMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<CrawledTweet>()
                        .eq(CrawledTweet::getCrawlTaskId, taskId)
                        .orderByDesc(CrawledTweet::getCreatedAt));
        return Result.success(PageResult.of(pageObj.getTotal(), pageObj.getRecords()));
    }

    @Override
    public Result<List<AnalysisRecord>> getTaskResults(Long taskId) {
        // Get tweet IDs for this task, then find their analysis records
        List<CrawledTweet> tweets = crawledTweetMapper.selectList(
                new LambdaQueryWrapper<CrawledTweet>()
                        .eq(CrawledTweet::getCrawlTaskId, taskId));

        if (tweets.isEmpty()) {
            return Result.success(List.of());
        }

        List<Long> tweetIds = tweets.stream().map(CrawledTweet::getId).toList();
        List<AnalysisRecord> records = analysisRecordMapper.selectList(
                new LambdaQueryWrapper<AnalysisRecord>()
                        .in(AnalysisRecord::getCrawledTweetId, tweetIds));
        return Result.success(records);
    }

    @Override
    public Result<MonitorTask> triggerCrawl(Long keywordId) {
        Keyword keyword = keywordMapper.selectById(keywordId);
        if (keyword == null) {
            return Result.error(404, "Keyword not found");
        }

        MonitorTask task = new MonitorTask();
        task.setKeywordId(keywordId);
        task.setStatus("RUNNING");
        task.setTriggerType("MANUAL");
        task.setStartTime(LocalDateTime.now());
        monitorTaskMapper.insert(task);

        int tweetCount = 0;
        String errorMessage = null;
        try {
            List<CrawledTweet> tweets = crawlerService.crawlTweets(keywordId, keyword.getKeyword());
            tweetCount = tweets.size();

            // Set crawl task ID
            for (CrawledTweet tweet : tweets) {
                tweet.setCrawlTaskId(task.getId());
                crawledTweetMapper.updateById(tweet);
            }

            // Process each tweet
            for (CrawledTweet tweet : tweets) {
                crawlerService.processTweet(tweet, keywordId);
            }

            task.setStatus("SUCCESS");
        } catch (Exception e) {
            log.error("Manual crawl failed for keyword {}: {}", keywordId, e.getMessage(), e);
            task.setStatus("FAILED");
            errorMessage = e.getMessage();
        }

        task.setEndTime(LocalDateTime.now());
        task.setTweetCount(tweetCount);
        task.setErrorMessage(errorMessage);
        monitorTaskMapper.updateById(task);

        // Save crawl log
        CrawlLog crawlLog = new CrawlLog();
        crawlLog.setKeywordId(keywordId);
        crawlLog.setTaskId(task.getId());
        crawlLog.setStatus(task.getStatus());
        crawlLog.setMessage(errorMessage != null ? errorMessage : "Crawl completed successfully");
        crawlLog.setTweetCount(tweetCount);
        crawlLogMapper.insert(crawlLog);

        return Result.success(task);
    }
}
