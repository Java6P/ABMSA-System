package com.abmsa.service;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;
import com.abmsa.entity.CrawledTweet;
import com.abmsa.entity.MonitorTask;

import java.util.List;

public interface MonitorTaskService {

    Result<List<MonitorTask>> listTasks(Long keywordId);

    Result<MonitorTask> getTask(Long id);

    Result<PageResult<CrawledTweet>> getTaskTweets(Long taskId, int page, int size);

    Result<List<AnalysisRecord>> getTaskResults(Long taskId);

    Result<MonitorTask> triggerCrawl(Long keywordId);
}
