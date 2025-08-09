package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Основная JPA-сущность, представляющая один завершенный тестовый запуск.
 * Хранит всю информацию о тесте, его результате, контексте и результатах анализа.
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"analysisResults", "configuration", "executionPath", "testTags", "customMetadata"})
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {

    /**
     * Перечисление возможных статусов завершения теста.
     */
    public enum TestStatus {
        PASSED, FAILED, SKIPPED, BROKEN;

        /**
         * Безопасно преобразует строку в один из статусов.
         * Если строка пустая или не соответствует ни одному статусу, возвращается BROKEN.
         *
         * @param text строковое представление статуса.
         * @return объект TestStatus.
         */
        public static TestStatus fromString(String text) {
            if (!StringUtils.hasText(text)) {
                return BROKEN;
            }
            try {
                return TestStatus.valueOf(text.toUpperCase());
            } catch (IllegalArgumentException e) {
                return BROKEN;
            }
        }
    }

    /**
     * Уникальный идентификатор тестового запуска (UUID, предоставляется клиентом).
     */
    @Id
    private String id;

    /**
     * Полное имя класса, содержащего тест.
     */
    private String testClass;
    /**
     * Имя тестового метода.
     */
    private String testMethod;
    /**
     * Время начала теста.
     */
    private LocalDateTime startTime;
    /**
     * Время окончания теста.
     */
    private LocalDateTime endTime;
    /**
     * Длительность теста в миллисекундах.
     */
    private long durationMillis;
    /**
     * Временная метка завершения, используется для сортировки и фильтрации.
     */
    private LocalDateTime timestamp;

    /**
     * Финальный статус теста.
     */
    @Enumerated(EnumType.STRING)
    private TestStatus status;

    /**
     * Тип исключения (если тест упал).
     */
    @Column(length = 255)
    private String exceptionType;

    /**
     * Сообщение исключения.
     */
    @Column(length = 2000)
    private String exceptionMessage;

    /**
     * Полный стек-трейс ошибки.
     */
    @Column(length = 4000)
    private String stackTrace;

    /**
     * Встроенные метаданные о шаге, на котором произошел сбой.
     */
    @Embedded
    private AiDecisionMetadata failedStep;

    /**
     * Полный путь выполнения теста (список шагов), хранится в отдельной таблице.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "execution_path", joinColumns = @JoinColumn(name = "test_run_id"))
    @OrderColumn(name = "step_index")
    private List<AiDecisionMetadata> executionPath = new ArrayList<>();

    /**
     * Встроенные детали окружения, в котором выполнялся тест.
     */
    @Embedded
    private EmbeddableEnvironmentDetails environmentDetails;

    /**
     * Встроенные ссылки на артефакты теста.
     */
    @Embedded
    private EmbeddableTestArtifacts artifacts;

    /**
     * Список тегов, присвоенных тесту, хранится в отдельной таблице.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "test_run_tags", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "tag_name")
    private List<String> testTags = new ArrayList<>();

    /**
     * Произвольные метаданные (ключ-значение), хранятся в отдельной таблице.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "test_run_custom_metadata", joinColumns = @JoinColumn(name = "test_run_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> customMetadata;

    /**
     * Список результатов анализа, связанных с этим запуском.
     */
    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisResult> analysisResults = new ArrayList<>();

    /**
     * Конфигурация, в которой выполнялся тест.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_id", nullable = false)
    private TestConfiguration configuration;

    /**
     * Вспомогательный метод для добавления результата анализа к тестовому запуску,
     * обеспечивая двустороннюю связь.
     *
     * @param result результат анализа для добавления.
     */
    public void addAnalysisResult(AnalysisResult result) {
        if (analysisResults == null) {
            analysisResults = new ArrayList<>();
        }
        analysisResults.add(result);
        result.setTestRun(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRun testRun = (TestRun) o;
        return id != null && Objects.equals(id, testRun.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
