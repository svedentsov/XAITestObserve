package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AiDecisionMetadata {
    private Integer stepNumber;
    private String action;
    private String locatorStrategy;
    @Column(length = 512)
    private String locatorValue;
    private String interactedText;
    private double confidenceScore;
    private String result;
    private String errorMessage;
    private Long stepStartTime;
    private Long stepEndTime;
    private Long stepDurationMillis;
    @Column(length = 1000)
    private String additionalStepData;
}
