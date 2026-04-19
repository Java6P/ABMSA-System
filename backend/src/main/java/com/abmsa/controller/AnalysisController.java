package com.abmsa.controller;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.dto.AnalysisRequest;
import com.abmsa.entity.AnalysisRecord;
import com.abmsa.mapper.UserMapper;
import com.abmsa.service.AnalysisService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController extends BaseController {

    private final AnalysisService analysisService;

    public AnalysisController(UserMapper userMapper, AnalysisService analysisService) {
        super(userMapper);
        this.analysisService = analysisService;
    }

    @PostMapping("/predict")
    public Result<AnalysisRecord> predict(@Valid @RequestBody AnalysisRequest request,
                                          Authentication auth) {
        Long userId = resolveUserId(auth);
        return analysisService.analyze(request.getText(), request.getTarget(),
                request.getImageUrl(), userId);
    }

    @PostMapping("/batch")
    public Result<List<AnalysisRecord>> batchAnalyze(@RequestParam("file") MultipartFile file,
                                                      Authentication auth) throws IOException {
        Long userId = resolveUserId(auth);
        List<Map<String, String>> items = parseFile(file);
        if (items == null) {
            return Result.error(400, "Unsupported file format. Use CSV or Excel.");
        }
        return analysisService.analyzeBatch(items, userId);
    }

    @GetMapping("/history")
    public Result<PageResult<AnalysisRecord>> history(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String analysisType,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return analysisService.getHistory(userId, page, size, sentiment, analysisType);
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response, Authentication auth) throws IOException {
        Long userId = resolveUserId(auth);
        byte[] data = analysisService.exportCsv(userId);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=analysis_history.csv");
        response.getOutputStream().write(data);
    }

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response, Authentication auth) throws IOException {
        Long userId = resolveUserId(auth);
        byte[] data = analysisService.exportExcel(userId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=analysis_history.xlsx");
        response.getOutputStream().write(data);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<Map<String, String>> parseFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase() : "";

        if (filename.endsWith(".csv")) {
            return parseCsv(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseExcel(file);
        }
        return null;
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        List<Map<String, String>> items = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return items;
            String[] headers = rows.get(0);
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> map = new LinkedHashMap<>();
                for (int j = 0; j < headers.length && j < row.length; j++) {
                    map.put(headers[j].trim().toLowerCase(), row[j]);
                }
                items.add(map);
            }
        } catch (CsvException e) {
            log.error("CSV parse error: {}", e.getMessage());
        }
        return items;
    }

    private List<Map<String, String>> parseExcel(MultipartFile file) throws IOException {
        List<Map<String, String>> items = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return items;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim().toLowerCase());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> map = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    map.put(headers.get(j), cell == null ? "" : cell.toString());
                }
                items.add(map);
            }
        }
        return items;
    }
}
