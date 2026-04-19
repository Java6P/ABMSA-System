package com.abmsa.controller;

import com.abmsa.common.Result;
import com.abmsa.entity.SubTarget;
import com.abmsa.service.SubTargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subtarget")
@RequiredArgsConstructor
public class SubTargetController {

    private final SubTargetService subTargetService;

    @GetMapping("/list")
    public Result<List<SubTarget>> list(@RequestParam Long keywordId) {
        return subTargetService.listByKeyword(keywordId);
    }

    @PostMapping
    public Result<SubTarget> create(@RequestBody SubTarget subTarget) {
        return subTargetService.createSubTarget(subTarget);
    }

    @PutMapping("/{id}")
    public Result<SubTarget> update(@PathVariable Long id, @RequestBody SubTarget subTarget) {
        return subTargetService.updateSubTarget(id, subTarget);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return subTargetService.deleteSubTarget(id);
    }

    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return subTargetService.toggleSubTarget(id, body.get("enabled"));
    }
}
