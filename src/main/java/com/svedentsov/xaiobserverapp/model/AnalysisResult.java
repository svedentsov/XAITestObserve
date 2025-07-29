package com.svedentsov.xaiobserverapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.svedentsov.xaiobserverapp.service.FeedbackService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность, представляющая результат AI-анализа для одного тестового запуска.
 * <p>
 * Содержит сгенерированную гипотезу о причине сбоя, предлагаемое решение,
 * уровень уверенности AI и другие связанные данные.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "analysis_result")
public class AnalysisResult {

    /**
     * Уникальный идентификатор результата анализа (генерируется как UUID).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Тестовый запуск, к которому относится данный анализ.
     * Устанавливает связь "многие-к-одному" с сущностью {@link TestRun}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    /**
     * Тип проведенного анализа (например, "Анализ по типу исключения", "Анализ шага сбоя").
     */
    private String analysisType;

    /**
     * Предполагаемая причина сбоя, сгенерированная AI.
     */
    @Column(length = 2000)
    private String suggestedReason;

    /**
     * Предлагаемое решение проблемы, сгенерированное AI.
     */
    @Column(length = 4000)
    private String solution;

    /**
     * Уровень уверенности AI в данном анализе (от 0.0 до 1.0).
     */
    private Double aiConfidence;

    /**
     * Временная метка, когда был выполнен анализ.
     */
    private LocalDateTime analysisTimestamp;

    /**
     * "Сырые" данные, на основе которых был сделан вывод (например, тип исключения, стек-трейс).
     */
    @Column(length = 4000)
    private String rawData;

    /**
     * Флаг, подтвержденный пользователем. {@code true}, если пользователь согласился с анализом.
     * Обновляется через {@link FeedbackService}.
     */
    private Boolean userConfirmedCorrect;

    /**
     * Список отзывов, оставленных пользователями для этого результата анализа.
     * Устанавливает связь "один-ко-многим" с сущностью {@link AnalysisFeedback}.
     */
    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Игнорируем при сериализации, чтобы избежать циклических зависимостей
    private List<AnalysisFeedback> feedback;
}
