package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.service.DemoDataFactory;
import com.svedentsov.xaiobserverapp.service.RcaService;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DemoController {
    private final TestEventOrchestrator testEventOrchestrator;
    private final DemoDataFactory demoDataFactory;
    private final TestRunMapper testRunMapper;
    private final RcaService rcaService;

    @PostMapping("/demo/create")
    public ResponseEntity<TestRunDetailDTO> createDemoTestRun() {
        log.info("Request to create a demo test run record via API.");
        try {
            FailureEventDTO event = demoDataFactory.generateRandomEvent();
            testEventOrchestrator.processAndSaveTestEvent(event);
            log.info("Demo record for '{}' sent for processing.", event.getTestMethod());
            List<AnalysisResult> analysisResults = rcaService.analyzeTestRun(event);
            TestRun testRunEntity = testRunMapper.toEntity(event);
            analysisResults.forEach(testRunEntity::addAnalysisResult);
            testRunEntity.setAppVersion(event.getAppVersion());
            testRunEntity.setTestSuite(event.getTestSuite());
            TestRunDetailDTO dtoToReturn = testRunMapper.toDetailDto(testRunEntity);
            return ResponseEntity.ok(dtoToReturn);
        } catch (Exception e) {
            log.error("Error creating demo record:", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
