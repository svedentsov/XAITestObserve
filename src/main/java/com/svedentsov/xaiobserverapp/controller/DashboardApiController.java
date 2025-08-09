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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для предоставления API, используемого фронтендом дашборда
 * и внешними системами (CI/CD).
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

    @Operation(summary = "Получение списка тестовых запусков с пагинацией", description = "Возвращает страницу с тестовыми запусками, отсортированными по времени. Используется для динамической подгрузки данных на дашборде.")
    @GetMapping("/tests")
    public ResponseEntity<Page<TestRunDetailDTO>> getTestRunsPaginated(
            @PageableDefault(size = 30, sort = "timestamp,desc") Pageable pageable) {
        log.debug("API request for paginated test runs: {}", pageable);
        return ResponseEntity.ok(testRunService.getAllTestRunsPaginated(pageable));
    }

    @Operation(summary = "Регистрация события завершения теста", description = "Асинхронно принимает, обрабатывает и сохраняет детали завершенного тестового запуска. Сразу возвращает ответ, обработка происходит в фоновом режиме.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Событие принято к обработке"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе")
    })
    @PostMapping("/events/test-finished")
    public ResponseEntity<Void> receiveTestEvent(@Valid @RequestBody FailureEventDTO event) {
        log.info("Received test finish event: {} - Status: {}. Offloading to async processor.", event.testMethod(), event.status());
        testEventOrchestrator.processAndSaveTestEvent(event);
        return ResponseEntity.accepted().build();
    }

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

    @Operation(summary = "Получение общей статистики", description = "Возвращает агрегированную статистику по всем тестовым запускам. Результаты кэшируются для повышения производительности.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ со статистикой")
    })
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        log.debug("API request for overall statistics.");
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    @Operation(summary = "Отправка обратной связи по результатам AI-анализа", description = "Позволяет пользователю оценить корректность предложенного AI анализа. Эта информация может использоваться для дообучения модели.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Обратная связь успешно сохранена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
            @ApiResponse(responseCode = "404", description = "Результат анализа с указанным ID не найден")
    })
    @PostMapping("/analysis/{analysisId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @Parameter(description = "ID результата анализа, к которому относится отзыв (UUID)", required = true)
            @PathVariable String analysisId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DTO с оценкой пользователя", required = true,
                    content = @Content(schema = @Schema(implementation = AnalysisFeedbackDTO.class)))
            @Valid @RequestBody AnalysisFeedbackDTO feedbackDTO) {
        feedbackService.processFeedback(analysisId, feedbackDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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
}
