package com.abmsa.task;

import com.abmsa.entity.*;
import com.abmsa.mapper.*;
import com.abmsa.service.CrawlerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordCrawlTask {

    private final KeywordMapper keywordMapper;
    private final MonitorTaskMapper monitorTaskMapper;
    private final CrawledTweetMapper crawledTweetMapper;
    private final CrawlLogMapper crawlLogMapper;
    private final CrawlerService crawlerService;

    @Value("${crawler.enabled:true}")
    private boolean crawlerEnabled;

    @Scheduled(cron = "${crawler.schedule.cron}")
    public void executeCrawl() {
        if (!crawlerEnabled) {
            log.debug("Crawler is disabled, skipping scheduled crawl");
            return;
        }

        log.info("Starting scheduled keyword crawl");
        List<Keyword> enabledKeywords = keywordMapper.selectList(
                new LambdaQueryWrapper<Keyword>().eq(Keyword::getIsEnabled, 1));

        for (Keyword keyword : enabledKeywords) {
            MonitorTask task = new MonitorTask();
            task.setKeywordId(keyword.getId());
            task.setStatus("RUNNING");
            task.setTriggerType("AUTO");
            task.setStartTime(LocalDateTime.now());
            monitorTaskMapper.insert(task);

            int tweetCount = 0;
            String errorMsg = null;

            try {
                List<CrawledTweet> tweets = crawlerService.crawlTweets(
                        keyword.getId(), keyword.getKeyword());
                tweetCount = tweets.size();

                for (CrawledTweet tweet : tweets) {
                    tweet.setCrawlTaskId(task.getId());
                    crawledTweetMapper.updateById(tweet);
                }

                task.setStatus("SUCCESS");

                // Update last crawl time
                keyword.setLastCrawlTime(LocalDateTime.now());
                keywordMapper.updateById(keyword);

            } catch (Exception e) {
                log.error("Scheduled crawl failed for keyword '{}': {}",
                        keyword.getKeyword(), e.getMessage(), e);
                task.setStatus("FAILED");
                errorMsg = e.getMessage();
            }

            task.setEndTime(LocalDateTime.now());
            task.setTweetCount(tweetCount);
            task.setErrorMessage(errorMsg);
            monitorTaskMapper.updateById(task);

            CrawlLog crawlLog = new CrawlLog();
            crawlLog.setKeywordId(keyword.getId());
            crawlLog.setTaskId(task.getId());
            crawlLog.setStatus(task.getStatus());
            crawlLog.setMessage(errorMsg != null ? errorMsg : "Scheduled crawl completed");
            crawlLog.setTweetCount(tweetCount);
            crawlLogMapper.insert(crawlLog);
        }
        log.info("Scheduled keyword crawl finished, processed {} keywords", enabledKeywords.size());
    }
}
