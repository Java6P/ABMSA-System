package com.abmsa.service;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;

public interface HistoryService {

    Result<PageResult<AnalysisRecord>> listHistory(Long userId, int page, int size,
                                                    String keyword, String sentiment);

    Result<AnalysisRecord> getRecord(Long id);

    Result<Void> deleteRecord(Long id);

    byte[] exportCsv(Long userId);

    byte[] exportExcel(Long userId);
}
