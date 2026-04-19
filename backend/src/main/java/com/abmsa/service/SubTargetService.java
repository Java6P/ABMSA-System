package com.abmsa.service;

import com.abmsa.common.Result;
import com.abmsa.entity.SubTarget;

import java.util.List;

public interface SubTargetService {

    Result<List<SubTarget>> listByKeyword(Long keywordId);

    Result<SubTarget> createSubTarget(SubTarget subTarget);

    Result<SubTarget> updateSubTarget(Long id, SubTarget subTarget);

    Result<Void> deleteSubTarget(Long id);

    Result<Void> toggleSubTarget(Long id, Integer enabled);
}
