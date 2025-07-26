package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import lombok.Data;

@Data
public class AnalysisResultDTO {
    private String id;
    private String analysisType;
    private String suggestedReason;
    private String solution;
    private Double aiConfidence;
    private String rawData;

    public static AnalysisResultDTO fromEntity(AnalysisResult result) {
        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setId(result.getId());
        dto.setAnalysisType(result.getAnalysisType());
        dto.setSuggestedReason(result.getSuggestedReason());
        dto.setSolution(result.getSolution());
        dto.setAiConfidence(result.getAiConfidence());
        dto.setRawData(result.getRawData());
        return dto;
    }
}
