package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import lombok.Data;

import java.util.List;

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
     * Временная метка завершения теста в формате Unix-времени (миллисекунды).
     */
    private long timestamp;
    /**
     * Статус завершения теста (например, "PASSED", "FAILED", "SKIPPED").
     */
    private String status;
    /**
     * Тип исключения, вызвавшего сбой теста (если применимо).
     */
    private String exceptionType;
    /**
     * Полный стек-трейс исключения (если применимо).
     */
    private String stackTrace;
    /**
     * Метаданные о шаге, на котором произошел сбой, включая действие, локатор и уверенность AI.
     */
    private AiDecisionMetadata failedStep;
    /**
     * Список метаданных, описывающих последовательность шагов выполнения теста.
     */
    private List<AiDecisionMetadata> executionPath;
    /**
     * Версия приложения, на которой выполнялся тест.
     */
    private String appVersion;
    /**
     * Среда выполнения теста (например, "QA", "STAGING", "PRODUCTION").
     */
    private String environment;
    /**
     * Название тестового набора, к которому принадлежит тест.
     */
    private String testSuite;
}
