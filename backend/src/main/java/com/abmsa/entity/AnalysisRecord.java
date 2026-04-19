package com.abmsa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("analysis_records")
public class AnalysisRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Nullable for manual analysis */
    private Long crawledTweetId;

    private Long userId;

    private String inputText;

    private String inputImageUrl;

    private String target;

    /** positive / negative / neutral */
    private String sentiment;

    private Double confidence;

    private Double positiveProb;

    private Double neutralProb;

    private Double negativeProb;

    /** MANUAL / AUTO */
    private String analysisType;

    /** USER_DEFINED / AUTO_EXTRACTED */
    private String targetSource;

    private Long keywordId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
