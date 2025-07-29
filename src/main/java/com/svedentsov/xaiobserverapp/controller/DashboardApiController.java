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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для основного API дашборда.
 * <p>
 * Предоставляет эндпоинты для регистрации тестовых событий, получения
 * статистики, деталей запусков и управления данными.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Основной API", description = "Операции для взаимодействия с данными тестовых запусков")
public class DashboardApiController {

    private final TestEventOrchestrator testEventOrchestrator;
    private final TestRunService testRunService;
    private final StatisticsService statisticsService;
    private final FeedbackService feedbackService;
    private final TestRunMapper testRunMapper;

    /**
     * Принимает и асинхронно обрабатывает событие о завершении теста.
     *
     * @param event DTO с полной информацией о тестовом запуске.
     * @return Ответ с кодом 202 (Accepted), указывающий на начало фоновой обработки.
     */
    @Operation(summary = "Регистрация события завершения теста", description = "Асинхронно принимает, обрабатывает и сохраняет детали завершенного тестового запуска. Сразу возвращает ответ, обработка происходит в фоновом режиме.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Событие принято к обработке"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе")
    })
    @PostMapping("/events/test-finished")
    public ResponseEntity<Void> receiveTestEvent(@Valid @RequestBody FailureEventDTO event) {
        log.info("Received test finish event: {} - Status: {}. Offloading to async processor.", event.getTestMethod(), event.getStatus());
        testEventOrchestrator.processAndSaveTestEvent(event);
        return ResponseEntity.accepted().build();
    }

    /**
     * Возвращает детальную информацию о тестовом запуске по его ID.
     *
     * @param id Уникальный идентификатор (UUID) тестового запуска.
     * @return {@link ResponseEntity} с {@link TestRunDetailDTO} и кодом 200 или 404, если запуск не найден.
     */
    @Operation(summary = "Получение детальной информации о тестовом запуске", description = "Возвращает полную информацию о конкретном тестовом запуске по его ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ", content = @Content(schema = @Schema(implementation = TestRunDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Тестовый запуск с указанным ID не найден")
    })
    @GetMapping("/tests/{id}")
    public ResponseEntity<TestRunDetailDTO> getTestDetailJson(
            @Parameter(description = "Уникальный идентификатор тестового запуска (UUID)", required = true, example = "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890")
            @PathVariable String id) {
        log.debug("API request for test details: ID {}", id);
        return testRunService.getTestRunById(id)
                .map(testRunMapper::toDetailDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("TestRun with ID " + id + " not found."));
    }

    /**
     * Возвращает общую статистику по всем тестовым запускам.
     *
     * @return {@link ResponseEntity} с DTO {@link StatisticsDTO} и кодом 200.
     */
    @Operation(summary = "Получение общей статистики", description = "Возвращает агрегированную статистику по всем тестовым запускам. Результаты кэшируются для повышения производительности.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ со статистикой")
    })
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        log.debug("API request for overall statistics.");
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    /**
     * Сохраняет обратную связь от пользователя по результату AI-анализа.
     *
     * @param analysisId  ID результата анализа, к которому относится отзыв.
     * @param feedbackDTO DTO с данными отзыва.
     * @return {@link ResponseEntity} с кодом 201 (Created) в случае успеха.
     */
    @Operation(summary = "Отправка обратной связи по результатам AI-анализа", description = "Позволяет пользователю оценить корректность предложенного AI анализа. Эта информация может использоваться для дообучения модели.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Обратная связь успешно сохранена"),
            @ApiResponse(responseCode = "404", description = "Результат анализа с указанным ID не найден")
    })
    @PostMapping("/analysis/{analysisId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @Parameter(description = "ID результата анализа, к которому относится отзыв (UUID)", required = true)
            @PathVariable String analysisId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DTO с оценкой пользователя", required = true)
            @RequestBody AnalysisFeedbackDTO feedbackDTO) {
        feedbackService.processFeedback(analysisId, feedbackDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Безвозвратно удаляет все данные о тестовых запусках.
     *
     * @return {@link ResponseEntity} с кодом 204 (No Content).
     */
    @Operation(summary = "Удаление всех данных", description = "!!! ОСТОРОЖНО !!! Этот эндпоинт безвозвратно удаляет все данные о тестовых запусках из базы данных.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Все данные успешно удалены")
    })
    @DeleteMapping("/tests/all")
    public ResponseEntity<Void> deleteAllData() {
        log.warn("API request to DELETE ALL test run data has been received.");
        testRunService.deleteAllTestRuns();
        return ResponseEntity.noContent().build();
    }

    /**
     * Возвращает список всех тестовых запусков за текущий день.
     *
     * @return {@link ResponseEntity} со списком {@link TestRunDetailDTO} и кодом 200.
     */
    @Operation(summary = "Получение деталей всех запусков за сегодня", description = "Возвращает список детальных DTO для всех тестовых запусков, выполненных сегодня. Используется для предзагрузки кэша на фронтенде.")
    @GetMapping("/tests/today")
    public ResponseEntity<List<TestRunDetailDTO>> getTodaysTestRuns() {
        log.debug("API request for all of today's test run details.");
        return ResponseEntity.ok(testRunService.getTestRunsForToday());
    }
}
