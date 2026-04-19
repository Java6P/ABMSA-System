package com.abmsa.service;

import com.abmsa.common.Result;
import com.abmsa.entity.Keyword;

import java.util.List;

public interface KeywordService {

    Result<List<Keyword>> listKeywords(Long userId);

    Result<Keyword> getKeyword(Long id);

    Result<Keyword> createKeyword(Keyword keyword, Long userId);

    Result<Keyword> updateKeyword(Long id, Keyword keyword);

    Result<Void> deleteKeyword(Long id);

    Result<Void> toggleKeyword(Long id, Integer enabled);
}
