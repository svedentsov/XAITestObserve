package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TestRunDetailDTO {
    private String id;
    private String testClass;
    private String testMethod;
    private LocalDateTime timestamp;
    private String status;
    private String exceptionType;
    private String stackTrace;
    private AiDecisionMetadata failedStep;
    private List<AiDecisionMetadata> executionPath;
    private List<AnalysisResultDTO> analysisResults;
    private TestConfigurationDTO configuration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMillis;
    private String exceptionMessage;
    private EnvironmentDetailsDTO environmentDetails;
    private TestArtifactsDTO artifacts;
    private List<String> testTags;
    private Map<String, String> customMetadata;
}
