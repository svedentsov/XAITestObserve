package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Основной DTO для отправки информации о завершенном тестовом запуске от клиента (тестового фреймворка).
 * Содержит всю необходимую информацию для сохранения и анализа тестового прогона.
 */
@Data
@Schema(description = "Основной DTO для отправки информации о завершенном тестовом запуске")
public class FailureEventDTO {

    @Schema(description = "Уникальный идентификатор тестового запуска (UUID)", required = true, example = "a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890")
    private String testRunId;

    @Schema(description = "Полное имя класса, содержащего тест", example = "com.example.tests.LoginTests")
    private String testClass;

    @Schema(description = "Имя тестового метода", example = "testSuccessfulLogin")
    private String testMethod;

    @Schema(description = "Время начала теста в миллисекундах (Unix epoch)", example = "1678886400000")
    private long startTime;

    @Schema(description = "Время окончания теста в миллисекундах (Unix epoch)", example = "1678886430000")
    private long endTime;

    @Schema(description = "Длительность теста в миллисекундах", example = "30000")
    private long durationMillis;

    @Schema(description = "Финальный статус теста", allowableValues = {"PASSED", "FAILED", "SKIPPED", "BROKEN"}, example = "FAILED")
    private String status;

    @Schema(description = "Тип исключения, если тест упал", example = "org.openqa.selenium.NoSuchElementException")
    private String exceptionType;

    @Schema(description = "Сообщение исключения", example = "Элемент 'submit_button' не найден на странице")
    private String exceptionMessage;

    @Schema(description = "Полный стек-трейс ошибки")
    private String stackTrace;

    @Schema(description = "Детали шага, на котором произошел сбой (если применимо)")
    private AiDecisionMetadata failedStep;

    @Schema(description = "Полный путь выполнения теста, состоящий из отдельных шагов")
    private List<AiDecisionMetadata> executionPath;

    @Schema(description = "Версия тестируемого приложения", example = "2.1.0-release")
    private String appVersion;

    @Schema(description = "Детали окружения, в котором выполнялся тест")
    private EnvironmentDetailsDTO environmentDetails;

    @Schema(description = "Название тестового набора (suite)", example = "Regression")
    private String testSuite;

    @Schema(description = "Список тегов, присвоенных тесту", example = "[\"smoke\", \"login\", \"P0\"]")
    private List<String> testTags;

    @Schema(description = "Ссылки на артефакты теста (скриншоты, видео, логи)")
    private TestArtifactsDTO artifacts;

    @Schema(description = "Произвольные метаданные в формате ключ-значение", example = "{\"jiraTicket\": \"PROJ-123\", \"buildNumber\": \"101\"}")
    private Map<String, String> customMetadata;
}
