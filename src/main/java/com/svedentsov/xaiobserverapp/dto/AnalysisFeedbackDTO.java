package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;

@Data
public class AnalysisFeedbackDTO {
    private Boolean isAiSuggestionCorrect;
    private String userProvidedReason;
    private String userProvidedSolution;
    private String comments;
    private String userId;
}
