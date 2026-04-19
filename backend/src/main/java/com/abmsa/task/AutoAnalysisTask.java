package com.abmsa.task;

import com.abmsa.entity.CrawledTweet;
import com.abmsa.mapper.CrawledTweetMapper;
import com.abmsa.service.CrawlerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoAnalysisTask {

    private final CrawledTweetMapper crawledTweetMapper;
    private final CrawlerService crawlerService;

    /** Runs every minute by default (configurable via auto-analysis.fixed-delay). */
    @Scheduled(fixedDelayString = "${auto-analysis.fixed-delay:60000}")
    public void autoAnalyze() {
        List<CrawledTweet> unanalyzed = crawledTweetMapper.selectList(
                new LambdaQueryWrapper<CrawledTweet>()
                        .eq(CrawledTweet::getIsAnalyzed, 0)
                        .last("LIMIT 50"));

        if (unanalyzed.isEmpty()) {
            return;
        }

        log.info("AutoAnalysisTask: processing {} unanalyzed tweets", unanalyzed.size());
        for (CrawledTweet tweet : unanalyzed) {
            crawlerService.processTweet(tweet, tweet.getKeywordId());
        }
        log.info("AutoAnalysisTask: finished processing {} tweets", unanalyzed.size());
    }
}
