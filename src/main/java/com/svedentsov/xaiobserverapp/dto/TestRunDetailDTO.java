package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO с полной и детальной информацией о конкретном тестовом запуске.
 * Используется как основной объект ответа при запросе деталей теста.
 */
@Data
@Schema(description = "Детальная информация о конкретном тестовом запуске (объект ответа)")
public class TestRunDetailDTO {

    @Schema(description = "Уникальный ID запуска", example = "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890")
    private String id;

    @Schema(description = "Класс теста", example = "com.example.tests.LoginTests")
    private String testClass;

    @Schema(description = "Метод теста", example = "testInvalidPassword")
    private String testMethod;

    @Schema(description = "Временная метка завершения теста")
    private LocalDateTime timestamp;

    @Schema(description = "Статус", example = "FAILED")
    private String status;

    @Schema(description = "Тип исключения", example = "java.lang.AssertionError")
    private String exceptionType;

    @Schema(description = "Стек-трейс")
    private String stackTrace;

    @Schema(description = "Детали шага, на котором произошел сбой")
    private AiDecisionMetadata failedStep;

    @Schema(description = "Полный путь выполнения теста")
    private List<AiDecisionMetadata> executionPath;

    @Schema(description = "Результаты AI-анализа сбоя")
    private List<AnalysisResultDTO> analysisResults;

    @Schema(description = "Конфигурация, в которой выполнялся тест")
    private TestConfigurationDTO configuration;

    @Schema(description = "Время начала теста")
    private LocalDateTime startTime;

    @Schema(description = "Время окончания теста")
    private LocalDateTime endTime;

    @Schema(description = "Длительность теста в мс", example = "25432")
    private long durationMillis;

    @Schema(description = "Сообщение исключения", example = "Expected title 'Dashboard' but found 'Login'")
    private String exceptionMessage;

    @Schema(description = "Детали окружения")
    private EnvironmentDetailsDTO environmentDetails;

    @Schema(description = "Артефакты теста")
    private TestArtifactsDTO artifacts;

    @Schema(description = "Теги теста", example = "[\"login\", \"P1\", \"flaky\"]")
    private List<String> testTags;

    @Schema(description = "Дополнительные метаданные", example = "{\"jiraTicket\": \"PROJ-456\"}")
    private Map<String, String> customMetadata;
}
