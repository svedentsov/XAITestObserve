package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * Основной DTO для приёма информации о завершенном тестовом запуске от внешних систем.
 * Содержит всю исчерпывающую информацию о тесте, его результате и контексте выполнения.
 *
 * @param testRunId          Уникальный идентификатор тестового запуска (UUID).
 * @param testClass          Полное имя класса, содержащего тест.
 * @param testMethod         Имя тестового метода.
 * @param startTime          Время начала теста в миллисекундах (Unix epoch).
 * @param endTime            Время окончания теста в миллисекундах (Unix epoch).
 * @param durationMillis     Длительность теста в миллисекундах.
 * @param status             Финальный статус теста (PASSED, FAILED, SKIPPED).
 * @param exceptionType      Тип исключения, если тест упал.
 * @param exceptionMessage   Сообщение исключения.
 * @param stackTrace         Полный стек-трейс ошибки.
 * @param failedStep         Детали шага, на котором произошел сбой.
 * @param executionPath      Полный путь выполнения теста, состоящий из отдельных шагов.
 * @param appVersion         Версия тестируемого приложения.
 * @param environmentDetails Детали окружения, в котором выполнялся тест.
 * @param testSuite          Название тестового набора (suite).
 * @param testTags           Список тегов, присвоенных тесту.
 * @param artifacts          Ссылки на артефакты теста (скриншоты, видео, логи).
 * @param customMetadata     Произвольные метаданные в формате ключ-значение.
 */
@Schema(description = "Основной DTO для отправки информации о завершенном тестовом запуске")
public record FailureEventDTO(

        @NotBlank
        @Schema(description = "Уникальный идентификатор тестового запуска (UUID)", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890")
        String testRunId,

        @Schema(description = "Полное имя класса, содержащего тест", example = "com.example.tests.LoginTests")
        String testClass,

        @Schema(description = "Имя тестового метода", example = "testSuccessfulLogin")
        String testMethod,

        @Schema(description = "Время начала теста в миллисекундах (Unix epoch)", example = "1719907200000")
        long startTime,

        @Schema(description = "Время окончания теста в миллисекундах (Unix epoch)", example = "1719907235000")
        long endTime,

        @Schema(description = "Длительность теста в миллисекундах", example = "35000")
        long durationMillis,

        @Schema(description = "Финальный статус теста", allowableValues = {"PASSED", "FAILED", "SKIPPED", "BROKEN"}, example = "FAILED")
        String status,

        @Schema(description = "Тип исключения, если тест упал", example = "org.openqa.selenium.NoSuchElementException")
        String exceptionType,

        @Schema(description = "Сообщение исключения", example = "Элемент 'submit_button' не найден на странице")
        String exceptionMessage,

        @Schema(description = "Полный стек-трейс ошибки")
        String stackTrace,

        @Schema(description = "Детали шага, на котором произошел сбой (если применимо)")
        AiDecisionMetadata failedStep,

        @Schema(description = "Полный путь выполнения теста, состоящий из отдельных шагов")
        List<AiDecisionMetadata> executionPath,

        @Schema(description = "Версия тестируемого приложения", example = "2.3.1-release")
        String appVersion,

        @Schema(description = "Детали окружения, в котором выполнялся тест")
        EnvironmentDetailsDTO environmentDetails,

        @Schema(description = "Название тестового набора (suite)", example = "Regression")
        String testSuite,

        @Schema(description = "Список тегов, присвоенных тесту", example = "[\"smoke\", \"login\", \"P1\"]")
        List<String> testTags,

        @Schema(description = "Ссылки на артефакты теста (скриншоты, видео, логи)")
        TestArtifactsDTO artifacts,

        @Schema(description = "Произвольные метаданные в формате ключ-значение", example = "{\"jiraTicket\": \"PROJ-123\", \"buildNumber\": \"101\"}")
        Map<String, String> customMetadata
) {
}
