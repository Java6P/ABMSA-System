package com.abmsa.service.impl;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;
import com.abmsa.mapper.AnalysisRecordMapper;
import com.abmsa.service.AnalysisService;
import com.abmsa.service.ModelCallService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRecordMapper analysisRecordMapper;
    private final ModelCallService modelCallService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] CSV_HEADERS = {
            "ID", "Text", "Target", "Sentiment", "Confidence",
            "PositiveProb", "NeutralProb", "NegativeProb",
            "AnalysisType", "TargetSource", "CreatedAt"
    };

    @Override
    public Result<AnalysisRecord> analyze(String text, String target, String imageUrl, Long userId) {
        Map<String, Object> prediction = modelCallService.predict(text, target, imageUrl);
        AnalysisRecord record = buildRecord(prediction, text, target, imageUrl, userId, "MANUAL", "USER_DEFINED");
        analysisRecordMapper.insert(record);
        return Result.success(record);
    }

    @Override
    public Result<List<AnalysisRecord>> analyzeBatch(List<Map<String, String>> items, Long userId) {
        if (items == null || items.isEmpty()) {
            return Result.error(400, "No items to analyze");
        }

        List<Map<String, Object>> requestItems = items.stream()
                .map(item -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("text", item.get("text"));
                    m.put("target", item.getOrDefault("target", ""));
                    m.put("image_url", item.getOrDefault("imageUrl", ""));
                    return m;
                })
                .toList();

        List<Map<String, Object>> predictions = modelCallService.predictBatch(requestItems);
        List<AnalysisRecord> records = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Map<String, String> item = items.get(i);
            Map<String, Object> pred = i < predictions.size() ? predictions.get(i) : Map.of();
            AnalysisRecord record = buildRecord(
                    pred,
                    item.get("text"),
                    item.getOrDefault("target", ""),
                    item.getOrDefault("imageUrl", ""),
                    userId, "MANUAL", "USER_DEFINED");
            analysisRecordMapper.insert(record);
            records.add(record);
        }
        return Result.success(records);
    }

    @Override
    public Result<PageResult<AnalysisRecord>> getHistory(Long userId, Integer page, Integer size,
                                                          String sentiment, String analysisType) {
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<AnalysisRecord>()
                .eq(AnalysisRecord::getUserId, userId)
                .eq(StringUtils.isNotBlank(sentiment), AnalysisRecord::getSentiment, sentiment)
                .eq(StringUtils.isNotBlank(analysisType), AnalysisRecord::getAnalysisType, analysisType)
                .orderByDesc(AnalysisRecord::getCreatedAt);

        Page<AnalysisRecord> pageObj = analysisRecordMapper.selectPage(
                new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(pageObj.getTotal(), pageObj.getRecords()));
    }

    @Override
    public byte[] exportCsv(Long userId) {
        List<AnalysisRecord> records = fetchAllForUser(userId);
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(CSV_HEADERS);
            for (AnalysisRecord r : records) {
                writer.writeNext(toRow(r));
            }
        } catch (IOException e) {
            log.error("CSV export error", e);
        }
        return sw.toString().getBytes();
    }

    @Override
    public byte[] exportExcel(Long userId) {
        List<AnalysisRecord> records = fetchAllForUser(userId);
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("AnalysisHistory");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < CSV_HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(CSV_HEADERS[i]);
            }
            int rowNum = 1;
            for (AnalysisRecord r : records) {
                Row row = sheet.createRow(rowNum++);
                String[] values = toRow(r);
                for (int i = 0; i < values.length; i++) {
                    row.createCell(i).setCellValue(values[i]);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Excel export error", e);
            return new byte[0];
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<AnalysisRecord> fetchAllForUser(Long userId) {
        return analysisRecordMapper.selectList(
                new LambdaQueryWrapper<AnalysisRecord>()
                        .eq(AnalysisRecord::getUserId, userId)
                        .orderByDesc(AnalysisRecord::getCreatedAt));
    }

    private AnalysisRecord buildRecord(Map<String, Object> prediction,
                                       String text, String target, String imageUrl,
                                       Long userId, String analysisType, String targetSource) {
        AnalysisRecord record = new AnalysisRecord();
        record.setUserId(userId);
        record.setInputText(text);
        record.setInputImageUrl(imageUrl);
        record.setTarget(target);
        record.setAnalysisType(analysisType);
        record.setTargetSource(targetSource);
        record.setSentiment(String.valueOf(prediction.getOrDefault("sentiment", "neutral")));
        record.setConfidence(toDouble(prediction.get("confidence")));
        record.setPositiveProb(toDouble(prediction.get("positive_prob")));
        record.setNeutralProb(toDouble(prediction.get("neutral_prob")));
        record.setNegativeProb(toDouble(prediction.get("negative_prob")));
        return record;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private String[] toRow(AnalysisRecord r) {
        return new String[]{
                String.valueOf(r.getId()),
                r.getInputText(),
                r.getTarget(),
                r.getSentiment(),
                r.getConfidence() == null ? "" : String.valueOf(r.getConfidence()),
                r.getPositiveProb() == null ? "" : String.valueOf(r.getPositiveProb()),
                r.getNeutralProb() == null ? "" : String.valueOf(r.getNeutralProb()),
                r.getNegativeProb() == null ? "" : String.valueOf(r.getNegativeProb()),
                r.getAnalysisType(),
                r.getTargetSource(),
                r.getCreatedAt() == null ? "" : r.getCreatedAt().format(FMT)
        };
    }
}
