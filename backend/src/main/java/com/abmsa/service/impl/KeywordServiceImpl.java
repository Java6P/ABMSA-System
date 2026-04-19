package com.abmsa.service.impl;

import com.abmsa.common.Result;
import com.abmsa.entity.Keyword;
import com.abmsa.mapper.KeywordMapper;
import com.abmsa.service.KeywordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final KeywordMapper keywordMapper;

    @Override
    public Result<List<Keyword>> listKeywords(Long userId) {
        List<Keyword> list = keywordMapper.selectList(
                new LambdaQueryWrapper<Keyword>()
                        .eq(Keyword::getUserId, userId)
                        .orderByDesc(Keyword::getCreatedAt));
        return Result.success(list);
    }

    @Override
    public Result<Keyword> getKeyword(Long id) {
        Keyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            return Result.error(404, "Keyword not found");
        }
        return Result.success(keyword);
    }

    @Override
    public Result<Keyword> createKeyword(Keyword keyword, Long userId) {
        keyword.setUserId(userId);
        keyword.setIsEnabled(1);
        keywordMapper.insert(keyword);
        return Result.success(keyword);
    }

    @Override
    public Result<Keyword> updateKeyword(Long id, Keyword keyword) {
        Keyword existing = keywordMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "Keyword not found");
        }
        keyword.setId(id);
        keyword.setUserId(existing.getUserId());
        keywordMapper.updateById(keyword);
        return Result.success(keywordMapper.selectById(id));
    }

    @Override
    public Result<Void> deleteKeyword(Long id) {
        int rows = keywordMapper.deleteById(id);
        if (rows == 0) {
            return Result.error(404, "Keyword not found");
        }
        return Result.success();
    }

    @Override
    public Result<Void> toggleKeyword(Long id, Integer enabled) {
        Keyword keyword = keywordMapper.selectById(id);
        if (keyword == null) {
            return Result.error(404, "Keyword not found");
        }
        keyword.setIsEnabled(enabled);
        keywordMapper.updateById(keyword);
        return Result.success();
    }
}
