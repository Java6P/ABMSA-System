package com.abmsa.service.impl;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;
import com.abmsa.mapper.AnalysisRecordMapper;
import com.abmsa.service.AnalysisService;
import com.abmsa.service.HistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final AnalysisRecordMapper analysisRecordMapper;
    private final AnalysisService analysisService;

    @Override
    public Result<PageResult<AnalysisRecord>> listHistory(Long userId, int page, int size,
                                                           String keyword, String sentiment) {
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<AnalysisRecord>()
                .eq(AnalysisRecord::getUserId, userId)
                .like(StringUtils.isNotBlank(keyword), AnalysisRecord::getInputText, keyword)
                .eq(StringUtils.isNotBlank(sentiment), AnalysisRecord::getSentiment, sentiment)
                .orderByDesc(AnalysisRecord::getCreatedAt);

        Page<AnalysisRecord> pageObj = analysisRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(pageObj.getTotal(), pageObj.getRecords()));
    }

    @Override
    public Result<AnalysisRecord> getRecord(Long id) {
        AnalysisRecord record = analysisRecordMapper.selectById(id);
        if (record == null) {
            return Result.error(404, "Record not found");
        }
        return Result.success(record);
    }

    @Override
    public Result<Void> deleteRecord(Long id) {
        int rows = analysisRecordMapper.deleteById(id);
        if (rows == 0) {
            return Result.error(404, "Record not found");
        }
        return Result.success();
    }

    @Override
    public byte[] exportCsv(Long userId) {
        return analysisService.exportCsv(userId);
    }

    @Override
    public byte[] exportExcel(Long userId) {
        return analysisService.exportExcel(userId);
    }
}
