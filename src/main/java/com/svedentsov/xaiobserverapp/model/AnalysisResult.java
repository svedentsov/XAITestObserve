package com.svedentsov.xaiobserverapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность, представляющая результат автоматического анализа сбоя тестового запуска.
 * Содержит предложенные AI причину сбоя, решение и уровень уверенности.
 * Может иметь связанные записи обратной связи от пользователя.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "analysis_result")
public class AnalysisResult {
    /**
     * Уникальный идентификатор результата анализа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Связь Many-to-One с сущностью TestRun.
     * Колонка "test_run_id" в таблице analysis_result — внешний ключ.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    /**
     * Тип анализа (например, "AI_RCA", "MANUAL_REVIEW").
     */
    private String analysisType;

    /**
     * Предполагаемая корневая причина сбоя.
     */
    @Column(length = 2000)
    private String rootCause;

    /**
     * Показатель уверенности (0.0–1.0) в выявленной причине.
     */
    private Double confidenceScore;

    /**
     * Предполагаемое решение или действие для устранения проблемы.
     */
    @Column(length = 4000)
    private String suggestedSolution;

    /**
     * Временная метка, когда был выполнен анализ.
     */
    private LocalDateTime analysisTimestamp;

    /**
     * Предложенная причина сбоя на основе анализа.
     */
    @Column(length = 2000)
    private String suggestedReason;

    /**
     * Предложенное решение или рекомендации по устранению сбоя.
     */
    @Column(length = 2000)
    private String solution;

    /**
     * Уровень уверенности AI в предложенной причине/решении (0.0–1.0).
     */
    private Double aiConfidence;

    /**
     * Сырые данные для анализа или дополнительная информация.
     */
    @Column(length = 4000)
    private String rawData;

    /**
     * Флаг, подтверждает ли пользователь результат анализа как корректный.
     */
    private Boolean userConfirmedCorrect;

    /**
     * Обратная связь от пользователей по данному результату анализа.
     */
    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AnalysisFeedback> feedback;
}
