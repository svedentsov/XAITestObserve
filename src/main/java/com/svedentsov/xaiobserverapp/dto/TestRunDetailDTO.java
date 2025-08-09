package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO для детального представления информации о конкретном тестовом запуске в ответах API.
 * Этот объект агрегирует всю информацию, необходимую для отображения на UI.
 *
 * @param id                 Уникальный ID запуска.
 * @param testClass          Класс теста.
 * @param testMethod         Метод теста.
 * @param timestamp          Временная метка завершения теста.
 * @param status             Статус.
 * @param exceptionType      Тип исключения.
 * @param stackTrace         Стек-трейс.
 * @param failedStep         Детали шага, на котором произошел сбой.
 * @param executionPath      Полный путь выполнения теста.
 * @param analysisResults    Результаты AI-анализа сбоя.
 * @param configuration      Конфигурация, в которой выполнялся тест.
 * @param startTime          Время начала теста.
 * @param endTime            Время окончания теста.
 * @param durationMillis     Длительность теста в мс.
 * @param exceptionMessage   Сообщение исключения.
 * @param environmentDetails Детали окружения.
 * @param artifacts          Артефакты теста.
 * @param testTags           Теги теста.
 * @param customMetadata     Дополнительные метаданные.
 */
@Schema(description = "Детальная информация о конкретном тестовом запуске (объект ответа)")
public record TestRunDetailDTO(

        @Schema(description = "Уникальный ID запуска", example = "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890")
        String id,

        @Schema(description = "Класс теста", example = "com.example.tests.LoginTests")
        String testClass,

        @Schema(description = "Метод теста", example = "testInvalidPassword")
        String testMethod,

        @Schema(description = "Временная метка завершения теста")
        LocalDateTime timestamp,

        @Schema(description = "Статус", example = "FAILED")
        String status,

        @Schema(description = "Тип исключения", example = "java.lang.AssertionError")
        String exceptionType,

        @Schema(description = "Стек-трейс")
        String stackTrace,

        @Schema(description = "Детали шага, на котором произошел сбой")
        AiDecisionMetadata failedStep,

        @Schema(description = "Полный путь выполнения теста")
        List<AiDecisionMetadata> executionPath,

        @Schema(description = "Результаты AI-анализа сбоя")
        List<AnalysisResultDTO> analysisResults,

        @Schema(description = "Конфигурация, в которой выполнялся тест")
        TestConfigurationDTO configuration,

        @Schema(description = "Время начала теста")
        LocalDateTime startTime,

        @Schema(description = "Время окончания теста")
        LocalDateTime endTime,

        @Schema(description = "Длительность теста в мс", example = "25432")
        long durationMillis,

        @Schema(description = "Сообщение исключения", example = "Expected title 'Dashboard' but found 'Login'")
        String exceptionMessage,

        @Schema(description = "Детали окружения")
        EnvironmentDetailsDTO environmentDetails,

        @Schema(description = "Артефакты теста")
        TestArtifactsDTO artifacts,

        @Schema(description = "Теги теста", example = "[\"login\", \"P1\", \"flaky\"]")
        List<String> testTags,

        @Schema(description = "Дополнительные метаданные", example = "{\"jiraTicket\": \"PROJ-456\"}")
        Map<String, String> customMetadata
) {
}
