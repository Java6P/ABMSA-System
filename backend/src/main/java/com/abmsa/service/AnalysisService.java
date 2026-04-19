package com.abmsa.service;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;

import java.util.List;
import java.util.Map;

public interface AnalysisService {

    Result<AnalysisRecord> analyze(String text, String target, String imageUrl, Long userId);

    Result<List<AnalysisRecord>> analyzeBatch(List<Map<String, String>> items, Long userId);

    Result<PageResult<AnalysisRecord>> getHistory(Long userId, Integer page, Integer size,
                                                   String sentiment, String analysisType);

    byte[] exportCsv(Long userId);

    byte[] exportExcel(Long userId);
}
