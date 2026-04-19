package com.abmsa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_tasks")
public class MonitorTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long keywordId;

    /** PENDING / RUNNING / SUCCESS / FAILED */
    private String status;

    /** AUTO / MANUAL */
    private String triggerType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer tweetCount;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
