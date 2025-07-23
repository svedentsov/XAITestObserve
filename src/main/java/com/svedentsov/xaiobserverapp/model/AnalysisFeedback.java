package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность для хранения обратной связи пользователя по результатам автоматического анализа.
 * Это крайне важно для обучения и улучшения ML-моделей в MLOps.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь с результатом анализа, к которому относится эта обратная связь.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;

    /**
     * Идентификатор пользователя, предоставившего обратную связь.
     */
    private String userId;

    /**
     * Временная метка, когда была предоставлена обратная связь.
     */
    private LocalDateTime feedbackTimestamp;

    /**
     * Было ли предложенное решение AI корректным? (true/false)
     */
    private Boolean isAiSuggestionCorrect;

    /**
     * Пользовательская причина, если предложенная AI была некорректной или неполной.
     */
    @Column(length = 2000)
    private String userProvidedReason;

    /**
     * Пользовательское решение, если предложенное AI было некорректным или неполным.
     */
    @Column(length = 2000)
    private String userProvidedSolution;

    /**
     * Комментарии пользователя.
     */
    @Column(length = 1000)
    private String comments;
}
