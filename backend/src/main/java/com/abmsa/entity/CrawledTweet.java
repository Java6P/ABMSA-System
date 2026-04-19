package com.abmsa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("crawled_tweets")
public class CrawledTweet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long keywordId;

    private String tweetId;

    private String content;

    private String authorName;

    private LocalDateTime publishTime;

    private String imageUrl;

    /** 0=not analyzed, 1=analyzed */
    private Integer isAnalyzed;

    private Long crawlTaskId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
