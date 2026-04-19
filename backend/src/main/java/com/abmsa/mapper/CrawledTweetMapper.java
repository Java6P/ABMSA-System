package com.abmsa.mapper;

import com.abmsa.entity.CrawledTweet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrawledTweetMapper extends BaseMapper<CrawledTweet> {
}
