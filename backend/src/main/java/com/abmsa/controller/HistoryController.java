package com.abmsa.controller;

import com.abmsa.common.PageResult;
import com.abmsa.common.Result;
import com.abmsa.entity.AnalysisRecord;
import com.abmsa.mapper.UserMapper;
import com.abmsa.service.HistoryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/history")
public class HistoryController extends BaseController {

    private final HistoryService historyService;

    public HistoryController(UserMapper userMapper, HistoryService historyService) {
        super(userMapper);
        this.historyService = historyService;
    }

    @GetMapping("/list")
    public Result<PageResult<AnalysisRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String analysisType,
            Authentication auth) {
        return historyService.listHistory(resolveUserId(auth), page, size, keyword, sentiment, analysisType);
    }

    @GetMapping("/{id}")
    public Result<AnalysisRecord> get(@PathVariable Long id) {
        return historyService.getRecord(id);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return historyService.deleteRecord(id);
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response, Authentication auth) throws IOException {
        byte[] data = historyService.exportCsv(resolveUserId(auth));
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=history.csv");
        response.getOutputStream().write(data);
    }

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response, Authentication auth) throws IOException {
        byte[] data = historyService.exportExcel(resolveUserId(auth));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=history.xlsx");
        response.getOutputStream().write(data);
    }
}
