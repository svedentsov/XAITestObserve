package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.service.DemoDataFactory;
import com.svedentsov.xaiobserverapp.service.RcaService;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

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
            CompletableFuture<TestRun> savedTestRunFuture = testEventOrchestrator.processAndSaveTestEvent(event);
            TestRun savedTestRun = savedTestRunFuture.get(); // Блокируемся и получаем результат
            log.info("Demo record for '{}' has been fully processed. ID: {}", event.getTestMethod(), savedTestRun.getId());
            TestRunDetailDTO dtoToReturn = testRunMapper.toDetailDto(savedTestRun);
            return ResponseEntity.ok(dtoToReturn);
        } catch (Exception e) {
            log.error("Error creating demo record:", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return ResponseEntity.internalServerError().build();
        }
    }
}
