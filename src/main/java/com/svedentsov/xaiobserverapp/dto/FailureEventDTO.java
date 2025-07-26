package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FailureEventDTO {
    private String testRunId;
    private String testClass;
    private String testMethod;
    private long startTime;
    private long endTime;
    private long durationMillis;
    private String status;
    private String exceptionType;
    private String exceptionMessage;
    private String stackTrace;
    private AiDecisionMetadata failedStep;
    private List<AiDecisionMetadata> executionPath;
    private String appVersion;
    private EnvironmentDetailsDTO environmentDetails;
    private String testSuite;
    private List<String> testTags;
    private TestArtifactsDTO artifacts;
    private Map<String, String> customMetadata;
}
