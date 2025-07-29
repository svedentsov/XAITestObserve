package com.svedentsov.xaiobserverapp.model;

import com.svedentsov.xaiobserverapp.dto.EnvironmentDetailsDTO;
import com.svedentsov.xaiobserverapp.dto.TestArtifactsDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Основная сущность, представляющая один конкретный запуск теста.
 * Хранит всю информацию о тесте: его имя, статус, время выполнения,
 * детали окружения, артефакты, ошибки, а также результаты анализа.
 * Является центральной сущностью в доменной модели приложения.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {

    /**
     * Перечисление возможных статусов завершения теста.
     */
    public enum TestStatus {
        /**
         * Тест успешно пройден.
         */
        PASSED,
        /**
         * Тест провален из-за ошибки в логике приложения или несоответствия утверждениям.
         */
        FAILED,
        /**
         * Выполнение теста было пропущено (например, из-за условия).
         */
        SKIPPED,
        /**
         * Тест сломан из-за ошибки в самом тестовом коде, инфраструктуре или окружении.
         */
        BROKEN
    }

    /**
     * Уникальный идентификатор тестового запуска (обычно UUID).
     */
    @Id
    private String id;

    /**
     * Полное имя класса, содержащего тестовый метод.
     */
    private String testClass;

    /**
     * Имя выполненного тестового метода.
     */
    private String testMethod;

    /**
     * Точное время начала выполнения теста.
     */
    private LocalDateTime startTime;

    /**
     * Точное время окончания выполнения теста.
     */
    private LocalDateTime endTime;

    /**
     * Общая длительность выполнения теста в миллисекундах.
     */
    private long durationMillis;

    /**
     * Временная метка завершения теста. Используется для сортировки и фильтрации.
     * Обычно совпадает с {@code endTime}.
     */
    private LocalDateTime timestamp;

    /**
     * Финальный статус выполнения теста.
     */
    @Enumerated(EnumType.STRING)
    private TestStatus status;

    /**
     * Тип исключения (exception), если тест завершился сбоем.
     */
    @Column(length = 255)
    private String exceptionType;

    /**
     * Сообщение, связанное с исключением.
     */
    @Column(length = 2000)
    private String exceptionMessage;

    /**
     * Полный стек-трейс ошибки.
     */
    @Column(length = 4000)
    private String stackTrace;

    /**
     * Встраиваемый объект с деталями о шаге, на котором произошел сбой.
     * Заполняется, если тестовый фреймворк передает эту информацию.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "action", column = @Column(name = "failed_step_action")),
            @AttributeOverride(name = "locatorStrategy", column = @Column(name = "failed_step_locator_strategy")),
            @AttributeOverride(name = "locatorValue", column = @Column(name = "failed_step_locator_value", length = 512)),
            @AttributeOverride(name = "confidenceScore", column = @Column(name = "failed_step_confidence_score")),
            @AttributeOverride(name = "result", column = @Column(name = "failed_step_result")),
            @AttributeOverride(name = "stepNumber", column = @Column(name = "failed_step_number")),
            @AttributeOverride(name = "interactedText", column = @Column(name = "failed_step_interacted_text")),
            @AttributeOverride(name = "errorMessage", column = @Column(name = "failed_step_error_message", length = 1000)),
            @AttributeOverride(name = "stepStartTime", column = @Column(name = "failed_step_start_time")),
            @AttributeOverride(name = "stepEndTime", column = @Column(name = "failed_step_end_time")),
            @AttributeOverride(name = "stepDurationMillis", column = @Column(name = "failed_step_duration_millis")),
            @AttributeOverride(name = "additionalStepData", column = @Column(name = "failed_step_additional_data", length = 1000))
    })
    private AiDecisionMetadata failedStep;

    /**
     * Полный путь выполнения теста, представленный в виде упорядоченного списка шагов.
     * Используется для визуализации и детального анализа.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "execution_path", joinColumns = @JoinColumn(name = "test_run_id"))
    @OrderColumn(name = "step_index")
    private List<AiDecisionMetadata> executionPath = new ArrayList<>();

    /**
     * Версия тестируемого приложения.
     */
    private String appVersion;

    /**
     * Название среды (окружения), в которой выполнялся тест (например, "QA", "STAGING").
     */
    private String environment;

    /**
     * Название тестового набора (suite), к которому относится тест.
     */
    private String testSuite;

    /**
     * Список тегов, присвоенных тесту (например, "smoke", "regression", "P0").
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "test_run_tags", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "tag_name")
    private List<String> testTags = new ArrayList<>();

    /**
     * Встраиваемый объект с деталями окружения (ОС, браузер, разрешение экрана и т.д.).
     */
    @Embedded
    private EnvironmentDetailsDTO environmentDetails;

    /**
     * Встраиваемый объект со ссылками на артефакты теста (скриншоты, видео, логи).
     */
    @Embedded
    private TestArtifactsDTO artifacts;

    /**
     * Произвольные метаданные, связанные с запуском, в формате "ключ-значение"
     * (например, ID задачи в Jira, номер сборки в CI).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "test_run_custom_metadata", joinColumns = @JoinColumn(name = "test_run_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> customMetadata;

    /**
     * Список результатов AI-анализа, связанных с этим запуском.
     * Связь "один-ко-многим" с сущностью {@link AnalysisResult}.
     */
    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisResult> analysisResults = new ArrayList<>();

    /**
     * Конфигурация, в которой выполнялся данный тест.
     * Связь "многие-к-одному" с сущностью {@link TestConfiguration}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private TestConfiguration configuration;

    /**
     * Вспомогательный метод для добавления результата анализа к текущему тестовому запуску.
     * Устанавливает двунаправленную связь между {@code TestRun} и {@code AnalysisResult}.
     *
     * @param result Результат анализа для добавления.
     */
    public void addAnalysisResult(AnalysisResult result) {
        if (analysisResults == null) {
            analysisResults = new ArrayList<>();
        }
        analysisResults.add(result);
        result.setTestRun(this);
    }
}
