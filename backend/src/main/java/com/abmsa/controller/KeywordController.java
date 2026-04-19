package com.abmsa.controller;

import com.abmsa.common.Result;
import com.abmsa.entity.Keyword;
import com.abmsa.mapper.UserMapper;
import com.abmsa.service.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/keyword")
public class KeywordController extends BaseController {

    private final KeywordService keywordService;

    public KeywordController(UserMapper userMapper, KeywordService keywordService) {
        super(userMapper);
        this.keywordService = keywordService;
    }

    @GetMapping("/list")
    public Result<List<Keyword>> list(Authentication auth) {
        return keywordService.listKeywords(resolveUserId(auth));
    }

    @GetMapping("/{id}")
    public Result<Keyword> get(@PathVariable Long id) {
        return keywordService.getKeyword(id);
    }

    @PostMapping
    public Result<Keyword> create(@RequestBody Keyword keyword, Authentication auth) {
        return keywordService.createKeyword(keyword, resolveUserId(auth));
    }

    @PutMapping("/{id}")
    public Result<Keyword> update(@PathVariable Long id, @RequestBody Keyword keyword) {
        return keywordService.updateKeyword(id, keyword);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return keywordService.deleteKeyword(id);
    }

    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return keywordService.toggleKeyword(id, body.get("enabled"));
    }
}
