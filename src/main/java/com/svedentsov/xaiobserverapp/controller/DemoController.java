package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.service.DemoDataFactory;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для генерации демонстрационных данных.
 * Предоставляет эндпоинт для создания случайных тестовых запусков,
 * что полезно для тестирования и демонстрации возможностей приложения.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Демо", description = "Операции для генерации демонстрационных данных")
public class DemoController {

    private final TestEventOrchestrator testEventOrchestrator;
    private final DemoDataFactory demoDataFactory;

    @Operation(summary = "Создание демонстрационного тестового запуска (асинхронно)",
            description = "Асинхронно инициирует создание случайного тестового запуска. " +
                    "Сразу возвращает 202 ACCEPTED. Результат будет отправлен всем подписчикам " +
                    "через WebSocket на топик /topic/new-test-run.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Запрос на создание демо-записи принят к обработке"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка при инициации задачи")
    })
    @PostMapping("/demo/create")
    public ResponseEntity<Void> createDemoTestRun() {
        log.info("Request to create a demo test run record via API.");
        try {
            FailureEventDTO event = demoDataFactory.generateRandomEvent();
            // Запускаем асинхронную обработку
            testEventOrchestrator.processAndSaveTestEvent(event);
            // Немедленно возвращаем ответ, не дожидаясь завершения
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            log.error("Error initiating demo record creation:", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
