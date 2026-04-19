package com.abmsa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalysisRequest {

    @NotBlank(message = "Text is required")
    private String text;

    private String target;

    private String imageUrl;
}
