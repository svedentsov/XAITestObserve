package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.AnalysisFeedbackDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.StatisticsDTO;
import com.svedentsov.xaiobserverapp.dto.TestRunDetailDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisFeedback;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.repository.AnalysisFeedbackRepository;
import com.svedentsov.xaiobserverapp.repository.AnalysisResultRepository;
import com.svedentsov.xaiobserverapp.service.StatisticsService;
import com.svedentsov.xaiobserverapp.service.TestRunService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * REST-контроллер для взаимодействия с API дашборда.
 * Предоставляет конечные точки для получения информации о тестовых запусках,
 * статистики и обработки событий завершения тестов, а также для отправки обратной связи.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardApiController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardApiController.class);

    private final TestRunService testRunService;
    private final StatisticsService statisticsService;
    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisFeedbackRepository analysisFeedbackRepository;

    /**
     * Обрабатывает POST-запросы для получения событий о завершении тестового запуска.
     * Эта конечная точка используется для приема данных о выполнении тестов
     * (например, из CI/CD пайплайна или тестового фреймворка) и их сохранения.
     *
     * @param event DTO, содержащий информацию о завершившемся тестовом событии.
     * @return {@link ResponseEntity} со статусом 201 (Created), если событие успешно обработано,
     * 400 (Bad Request) при ошибке валидации, или 500 (Internal Server Error) при критической ошибке.
     */
    @PostMapping("/events/test-finished")
    public ResponseEntity<Void> receiveTestEvent(@RequestBody FailureEventDTO event) {
        logger.info("Получено событие о завершении теста: {} - Статус: {}",
                event.getTestMethod(), event.getStatus());
        try {
            testRunService.processAndSaveTestEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при обработке события: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Критическая ошибка при обработке события: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обрабатывает GET-запросы для получения детальной информации о конкретном тестовом запуске.
     * Возвращает данные в формате JSON.
     *
     * @param id Уникальный идентификатор тестового запуска.
     * @return {@link ResponseEntity} с {@link TestRunDetailDTO} и статусом 200 (OK), если тест найден,
     * или 404 (Not Found), если тест с таким ID не существует.
     */
    @GetMapping("/tests/{id}")
    public ResponseEntity<TestRunDetailDTO> getTestDetailJson(@PathVariable String id) {
        logger.debug("API запрос деталей теста: ID {}", id);
        return testRunService.getTestRunById(id)
                .map(TestRunDetailDTO::fromEntity) // Маппим сущность в DTO
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Обрабатывает GET-запросы для получения общей статистики по всем тестовым запускам.
     *
     * @return {@link ResponseEntity} с {@link StatisticsDTO} и статусом 200 (OK).
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        logger.debug("API запрос общей статистики.");
        StatisticsDTO stats = statisticsService.getOverallStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Обрабатывает POST-запросы для отправки обратной связи по результатам анализа.
     * Позволяет пользователям оценить корректность анализа, предложенного системой,
     * и добавить комментарии.
     *
     * @param analysisId  Идентификатор результата анализа, к которому относится обратная связь.
     * @param feedbackDTO DTO, содержащий флаг корректности анализа и пользовательский комментарий.
     * @return {@link ResponseEntity} с сохраненным объектом {@link AnalysisFeedback} и статусом 201 (Created),
     * или 404 (Not Found), если анализ с указанным ID не найден.
     * @throws ResponseStatusException Если анализ с заданным {@code analysisId} не найден.
     */
    @PostMapping("/analysis/{analysisId}/feedback")
    public ResponseEntity<AnalysisFeedback> submitFeedback(
            @PathVariable Long analysisId,
            @RequestBody AnalysisFeedbackDTO feedbackDTO) {
        logger.info("Получена обратная связь для анализа ID {}: isCorrect={}, userProvidedReason='{}'",
                analysisId, feedbackDTO.getIsAiSuggestionCorrect(), feedbackDTO.getUserProvidedReason());

        AnalysisResult analysisResult = analysisResultRepository.findById(analysisId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Анализ с ID " + analysisId + " не найден"));

        // Обновляем флаг userConfirmedCorrect в AnalysisResult на основе обратной связи
        analysisResult.setUserConfirmedCorrect(feedbackDTO.getIsAiSuggestionCorrect());
        analysisResultRepository.save(analysisResult); // Сохраняем обновленный AnalysisResult

        AnalysisFeedback feedback = new AnalysisFeedback();
        feedback.setAnalysisResult(analysisResult);
        feedback.setIsAiSuggestionCorrect(feedbackDTO.getIsAiSuggestionCorrect()); 
        feedback.setUserProvidedReason(feedbackDTO.getUserProvidedReason());     
        feedback.setUserProvidedSolution(feedbackDTO.getUserProvidedSolution());   
        feedback.setComments(feedbackDTO.getComments());                       
        feedback.setUserId(StringUtils.hasText(feedbackDTO.getUserId()) ? feedbackDTO.getUserId() : "anonymous"); 
        feedback.setFeedbackTimestamp(LocalDateTime.now()); // Текущее время для фидбека

        AnalysisFeedback savedFeedback = analysisFeedbackRepository.save(feedback);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback);
    }
}
