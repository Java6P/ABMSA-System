package com.abmsa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("crawl_logs")
public class CrawlLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long keywordId;

    private Long taskId;

    private String status;

    private String message;

    private Integer tweetCount;

    private Integer analysisCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
