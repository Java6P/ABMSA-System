package com.abmsa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("keywords")
public class Keyword {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String keyword;

    private String description;

    /** 1=enabled, 0=disabled */
    private Integer isEnabled;

    /** Cron expression for crawl frequency */
    private String crawlFrequency;

    private LocalDateTime lastCrawlTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
