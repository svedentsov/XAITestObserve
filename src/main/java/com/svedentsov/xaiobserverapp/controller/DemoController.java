package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.service.DemoDataFactory;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Контроллер для генерации демонстрационных данных.
 * <p>
 * Предоставляет эндпоинт для создания случайных тестовых запусков,
 * что полезно для демонстрации и тестирования интерфейса.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Демо", description = "Операции для генерации демонстрационных данных")
public class DemoController {

    private final TestEventOrchestrator testEventOrchestrator;
    private final DemoDataFactory demoDataFactory;
    private final TestRunMapper testRunMapper;

    /**
     * Создает, сохраняет и возвращает один демонстрационный тестовый запуск.
     *
     * @return {@link ResponseEntity} с {@link TestRunDetailDTO} созданного запуска или ошибку 500.
     */
    @Operation(summary = "Создание демонстрационного тестового запуска", description = "Генерирует случайный тестовый запуск (успешный или проваленный) и сохраняет его в системе. Возвращает созданную сущность. Удобно для демонстрации возможностей дашборда.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Демо-запись успешно создана и возвращена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка при создании демо-записи")
    })
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
