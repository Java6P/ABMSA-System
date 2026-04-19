package com.abmsa.service.impl;

import com.abmsa.common.Result;
import com.abmsa.entity.SubTarget;
import com.abmsa.mapper.SubTargetMapper;
import com.abmsa.service.SubTargetService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubTargetServiceImpl implements SubTargetService {

    private final SubTargetMapper subTargetMapper;

    @Override
    public Result<List<SubTarget>> listByKeyword(Long keywordId) {
        List<SubTarget> list = subTargetMapper.selectList(
                new LambdaQueryWrapper<SubTarget>()
                        .eq(SubTarget::getKeywordId, keywordId)
                        .orderByAsc(SubTarget::getCreatedAt));
        return Result.success(list);
    }

    @Override
    public Result<SubTarget> createSubTarget(SubTarget subTarget) {
        subTarget.setIsEnabled(1);
        subTargetMapper.insert(subTarget);
        return Result.success(subTarget);
    }

    @Override
    public Result<SubTarget> updateSubTarget(Long id, SubTarget subTarget) {
        SubTarget existing = subTargetMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "SubTarget not found");
        }
        subTarget.setId(id);
        subTarget.setKeywordId(existing.getKeywordId());
        subTargetMapper.updateById(subTarget);
        return Result.success(subTargetMapper.selectById(id));
    }

    @Override
    public Result<Void> deleteSubTarget(Long id) {
        int rows = subTargetMapper.deleteById(id);
        if (rows == 0) {
            return Result.error(404, "SubTarget not found");
        }
        return Result.success();
    }

    @Override
    public Result<Void> toggleSubTarget(Long id, Integer enabled) {
        SubTarget subTarget = subTargetMapper.selectById(id);
        if (subTarget == null) {
            return Result.error(404, "SubTarget not found");
        }
        subTarget.setIsEnabled(enabled);
        subTargetMapper.updateById(subTarget);
        return Result.success();
    }
}
