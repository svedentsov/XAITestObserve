package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.AnalysisFeedbackDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.StatisticsDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.exception.ResourceNotFoundException;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.service.FeedbackService;
import com.svedentsov.xaiobserverapp.service.StatisticsService;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import com.svedentsov.xaiobserverapp.service.TestRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardApiController {
    private final TestEventOrchestrator testEventOrchestrator;
    private final TestRunService testRunService;
    private final StatisticsService statisticsService;
    private final FeedbackService feedbackService;
    private final TestRunMapper testRunMapper;

    @PostMapping("/events/test-finished")
    public ResponseEntity<Void> receiveTestEvent(@Valid @RequestBody FailureEventDTO event) {
        log.info("Received test finish event: {} - Status: {}. Offloading to async processor.", event.getTestMethod(), event.getStatus());
        testEventOrchestrator.processAndSaveTestEvent(event);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/tests/{id}")
    public ResponseEntity<TestRunDetailDTO> getTestDetailJson(@PathVariable String id) {
        log.debug("API request for test details: ID {}", id);
        return testRunService.getTestRunById(id)
                .map(testRunMapper::toDetailDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("TestRun with ID " + id + " not found."));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        log.debug("API request for overall statistics.");
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    @PostMapping("/analysis/{analysisId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable String analysisId,
            @RequestBody AnalysisFeedbackDTO feedbackDTO) {
        feedbackService.processFeedback(analysisId, feedbackDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/tests/all")
    public ResponseEntity<Void> deleteAllData() {
        log.warn("API request to DELETE ALL test run data has been received.");
        testRunService.deleteAllTestRuns();
        return ResponseEntity.noContent().build();
    }
}
