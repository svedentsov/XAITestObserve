package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Объект передачи данных (DTO) для события о сбое теста.
 * Представляет собой информацию, поступающую в систему при завершении
 * выполнения тестового метода, особенно в случае его падения.
 */
@Data
public class FailureEventDTO {
    /**
     * Уникальный идентификатор всего тестового запуска, частью которого является данное событие.
     */
    private String testRunId;
    /**
     * Полное имя класса, в котором выполнялся тест.
     */
    private String testClass;
    /**
     * Имя тестового метода.
     */
    private String testMethod;
    /**
     * Временная метка начала выполнения теста в формате Unix-времени (миллисекунды).
     */
    private long startTime;
    /**
     * Временная метка завершения теста в формате Unix-времени (миллисекунды).
     */
    private long endTime;
    /**
     * Длительность выполнения теста в миллисекундах.
     */
    private long durationMillis;
    /**
     * Статус завершения теста (например, "PASSED", "FAILED", "SKIPPED", "BROKEN").
     */
    private String status;
    /**
     * Тип исключения, вызвавшего сбой теста (если применимо).
     */
    private String exceptionType;
    /**
     * Сообщение исключения (если применимо).
     */
    private String exceptionMessage;
    /**
     * Полный стек-трейс исключения (если применимо).
     */
    private String stackTrace;
    /**
     * Метаданные о шаге, на котором произошел сбой, включая действие, локатор и уверенность AI.
     * Используется {@link AiDecisionMetadata}.
     */
    private AiDecisionMetadata failedStep;
    /**
     * Список метаданных, описывающих последовательность шагов выполнения теста.
     * Каждый элемент представляет собой {@link AiDecisionMetadata}.
     */
    private List<AiDecisionMetadata> executionPath;
    /**
     * Версия приложения, на которой выполнялся тест.
     */
    private String appVersion;
    /**
     * Детали тестовой среды, включая тип ОС, браузер, разрешение экрана и т.д.
     * Используется {@link EnvironmentDetailsDTO}.
     */
    private EnvironmentDetailsDTO environmentDetails; // Заменено String environment на более детальный DTO
    /**
     * Название тестового набора, к которому принадлежит тест.
     */
    private String testSuite;
    /**
     * Список тегов или категорий, связанных с тестом.
     */
    private List<String> testTags;
    /**
     * Ссылки на артефакты, сгенерированные во время выполнения теста (скриншоты, видео, логи).
     * Используется {@link TestArtifactsDTO}.
     */
    private TestArtifactsDTO artifacts;
    /**
     * Дополнительные произвольные метаданные, которые могут быть полезны для анализа.
     */
    private Map<String, String> customMetadata;
}
