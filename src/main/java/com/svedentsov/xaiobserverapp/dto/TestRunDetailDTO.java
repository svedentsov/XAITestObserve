package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import com.svedentsov.xaiobserverapp.model.TestRun;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public static TestRunDetailDTO fromEntity(TestRun testRun) {
        TestRunDetailDTO dto = new TestRunDetailDTO();
        dto.setId(testRun.getId());
        dto.setTestClass(testRun.getTestClass());
        dto.setTestMethod(testRun.getTestMethod());
        dto.setTimestamp(testRun.getTimestamp());
        dto.setStatus(testRun.getStatus().name());
        dto.setExceptionType(testRun.getExceptionType());
        dto.setStackTrace(testRun.getStackTrace());
        dto.setFailedStep(testRun.getFailedStep());
        dto.setExecutionPath(testRun.getExecutionPath());
        if (testRun.getAnalysisResults() != null) {
            dto.setAnalysisResults(testRun.getAnalysisResults().stream()
                    .map(AnalysisResultDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        if (testRun.getConfiguration() != null) {
            dto.setConfiguration(TestConfigurationDTO.fromEntity(testRun.getConfiguration()));
        }
        return dto;
    }
}
