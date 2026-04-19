package com.abmsa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sub_targets")
public class SubTarget {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long keywordId;

    private String targetName;

    private String description;

    /** 1=enabled, 0=disabled */
    private Integer isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
