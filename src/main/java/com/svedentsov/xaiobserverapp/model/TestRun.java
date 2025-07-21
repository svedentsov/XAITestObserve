package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность, представляющая отдельный тестовый запуск.
 * Содержит всю основную информацию о выполнении одного тестового метода,
 * включая его статус, время, детали исключения, шаги выполнения
 * и связанные результаты анализа.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {
    /**
     * Перечисление возможных статусов тестового запуска.
     */
    public enum TestStatus {
        PASSED,
        FAILED,
        SKIPPED
    }

    /**
     * Уникальный идентификатор тестового запуска.
     * Используется как первичный ключ.
     */
    @Id
    private String id;

    /**
     * Полное имя класса, в котором выполнялся тест.
     */
    private String testClass;

    /**
     * Имя тестового метода.
     */
    private String testMethod;

    /**
     * Временная метка завершения теста.
     */
    private LocalDateTime timestamp;

    /**
     * Статус завершения теста ({@code PASSED}, {@code FAILED}, {@code SKIPPED}).
     * Хранится как строка в базе данных.
     */
    @Enumerated(EnumType.STRING)
    private TestStatus status;

    /**
     * Тип исключения, вызвавшего сбой теста (если применимо).
     * Длина столбца 2000 символов.
     */
    @Column(length = 2000)
    private String exceptionType;

    /**
     * Полный стек-трейс исключения (если применимо).
     * Длина столбца 4000 символов.
     */
    @Column(length = 4000)
    private String stackTrace;

    /**
     * Встраиваемый объект, содержащий метаданные о шаге, на котором произошел сбой.
     */
    @Embedded
    private AiDecisionMetadata failedStep;

    /**
     * Коллекция встраиваемых объектов, представляющих последовательность шагов выполнения теста.
     * Хранится в отдельной таблице {@code execution_path}, связанной по {@code test_run_id}.
     * {@code @OrderColumn} сохраняет порядок элементов в списке.
     * {@code FetchType.LAZY} для отложенной загрузки.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "execution_path", joinColumns = @JoinColumn(name = "test_run_id"))
    @OrderColumn(name = "step_index")
    private List<AiDecisionMetadata> executionPath = new ArrayList<>();

    /**
     * Список результатов анализа, связанных с данным тестовым запуском.
     * {@code CascadeType.ALL} означает, что все операции (сохранение, удаление) будут каскадно применяться.
     * {@code orphanRemoval = true} удаляет дочерние сущности, если они отсоединяются от родителя.
     * {@code FetchType.EAGER} для немедленной загрузки.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "test_run_id")
    private List<AnalysisResult> analysisResults = new ArrayList<>();

    /**
     * Конфигурация тестового запуска (версия приложения, окружение и т.д.).
     * {@code FetchType.LAZY} для отложенной загрузки.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private TestConfiguration configuration;
}
