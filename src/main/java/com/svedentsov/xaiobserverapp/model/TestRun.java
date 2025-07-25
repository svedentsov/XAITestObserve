package com.svedentsov.xaiobserverapp.model;

import com.svedentsov.xaiobserverapp.dto.EnvironmentDetailsDTO;
import com.svedentsov.xaiobserverapp.dto.TestArtifactsDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {
    public enum TestStatus {
        PASSED,
        FAILED,
        SKIPPED,
        BROKEN
    }

    @Id
    private String id;
    private String testClass;
    private String testMethod;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMillis;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private TestStatus status;
    @Column(length = 2000)
    private String exceptionType;
    @Column(length = 2000)
    private String exceptionMessage;
    @Column(length = 4000)
    private String stackTrace;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "action", column = @Column(name = "failed_step_action")),
            @AttributeOverride(name = "locatorStrategy", column = @Column(name = "failed_step_locator_strategy")),
            @AttributeOverride(name = "locatorValue", column = @Column(name = "failed_step_locator_value", length = 512)),
            @AttributeOverride(name = "confidenceScore", column = @Column(name = "failed_step_confidence_score")),
            @AttributeOverride(name = "result", column = @Column(name = "failed_step_result")),
            @AttributeOverride(name = "stepNumber", column = @Column(name = "failed_step_number")),
            @AttributeOverride(name = "interactedText", column = @Column(name = "failed_step_interacted_text")),
            @AttributeOverride(name = "errorMessage", column = @Column(name = "failed_step_error_message")),
            @AttributeOverride(name = "stepStartTime", column = @Column(name = "failed_step_start_time")),
            @AttributeOverride(name = "stepEndTime", column = @Column(name = "failed_step_end_time")),
            @AttributeOverride(name = "stepDurationMillis", column = @Column(name = "failed_step_duration_millis")),
            @AttributeOverride(name = "additionalStepData", column = @Column(name = "failed_step_additional_data", length = 1000))
    })
    private AiDecisionMetadata failedStep;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "execution_path", joinColumns = @JoinColumn(name = "test_run_id"))
    @OrderColumn(name = "step_index")
    private List<AiDecisionMetadata> executionPath = new ArrayList<>();
    private String appVersion;
    private String environment;
    private String testSuite;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "test_run_tags", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "tag_name")
    private List<String> testTags = new ArrayList<>();
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "env_name")),
            @AttributeOverride(name = "osType", column = @Column(name = "env_os_type")),
            @AttributeOverride(name = "osVersion", column = @Column(name = "env_os_version")),
            @AttributeOverride(name = "browserType", column = @Column(name = "env_browser_type")),
            @AttributeOverride(name = "browserVersion", column = @Column(name = "env_browser_version")),
            @AttributeOverride(name = "screenResolution", column = @Column(name = "env_screen_resolution")),
            @AttributeOverride(name = "deviceType", column = @Column(name = "env_device_type")),
            @AttributeOverride(name = "deviceName", column = @Column(name = "env_device_name")),
            @AttributeOverride(name = "driverVersion", column = @Column(name = "env_driver_version")),
            @AttributeOverride(name = "appBaseUrl", column = @Column(name = "env_app_base_url", length = 512))
    })
    private EnvironmentDetailsDTO environmentDetails;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "screenshotUrls", column = @Column(name = "artifact_screenshot_urls", length = 1000)),
            @AttributeOverride(name = "videoUrl", column = @Column(name = "artifact_video_url", length = 512)),
            @AttributeOverride(name = "appLogUrls", column = @Column(name = "artifact_app_log_urls", length = 1000)),
            @AttributeOverride(name = "browserConsoleLogUrl", column = @Column(name = "artifact_browser_console_log_url", length = 512)),
            @AttributeOverride(name = "harFileUrl", column = @Column(name = "artifact_har_file_url", length = 512))
    })
    private TestArtifactsDTO artifacts;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "test_run_custom_metadata", joinColumns = @JoinColumn(name = "test_run_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> customMetadata;
    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisResult> analysisResults = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private TestConfiguration configuration;

    public void addAnalysisResult(AnalysisResult result) {
        if (analysisResults == null) {
            analysisResults = new ArrayList<>();
        }
        analysisResults.add(result);
        result.setTestRun(this);
    }
}
