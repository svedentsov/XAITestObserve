package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.AnalysisFeedbackDTO;
import com.svedentsov.xaiobserverapp.dto.DashboardStatisticsDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.mapper.TestRunMapper;
import com.svedentsov.xaiobserverapp.service.FeedbackService;
import com.svedentsov.xaiobserverapp.service.StatisticsService;
import com.svedentsov.xaiobserverapp.service.TestEventOrchestrator;
import com.svedentsov.xaiobserverapp.service.TestRunService;
import io.swagger.v3.oas.annotations.Operation;
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

    /**
     * Возвращает расширенную статистику для дашборда.
     *
     * @return ResponseEntity с {@link DashboardStatisticsDTO}.
     */
    @Operation(summary = "Получение расширенной статистики для дашборда", description = "Возвращает агрегированную статистику по всем тестовым запускам, включая распределения и топы. Результаты кэшируются.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ со статистикой", content = @Content(schema = @Schema(implementation = DashboardStatisticsDTO.class)))
    })
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsDTO> getStatistics() { // ИЗМЕНЕНИЕ
        log.debug("API request for dashboard statistics.");
        return ResponseEntity.ok(statisticsService.getDashboardStatistics()); // ИЗМЕНЕНИЕ
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

    @Operation(summary = "Отправка обратной связи по результату анализа", description = "Позволяет пользователю оценить, был ли AI-анализ корректным.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв успешно принят"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
            @ApiResponse(responseCode = "404", description = "Результат анализа с указанным ID не найден")
    })
    @PostMapping("/analysis/{analysisId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable String analysisId,
            @Valid @RequestBody AnalysisFeedbackDTO feedbackDTO) {
        log.info("Received feedback for analysisId {}: {}", analysisId, feedbackDTO.isAiSuggestionCorrect());
        feedbackService.processFeedback(analysisId, feedbackDTO);
        return ResponseEntity.ok().build();
    }
}
