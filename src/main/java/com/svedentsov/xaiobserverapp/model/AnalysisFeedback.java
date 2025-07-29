package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая обратную связь от пользователя по результату AI-анализа.
 * <p>
 * Хранит оценку пользователя (был ли анализ корректным), его комментарии и
 * альтернативные предположения о причине и решении проблемы. Эти данные
 * могут быть использованы для дообучения аналитических моделей.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisFeedback {

    /**
     * Уникальный идентификатор записи обратной связи (генерируется автоматически).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Результат анализа, к которому относится данная обратная связь.
     * Устанавливает связь "многие-к-одному" с сущностью {@link AnalysisResult}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;

    /**
     * Идентификатор пользователя, оставившего отзыв (например, логин).
     * Может быть "anonymous", если пользователь не аутентифицирован.
     */
    private String userId;

    /**
     * Временная метка, когда была оставлена обратная связь.
     */
    private LocalDateTime feedbackTimestamp;

    /**
     * Оценка пользователя: {@code true}, если AI-анализ был верным, {@code false} - если неверным.
     */
    private Boolean isAiSuggestionCorrect;

    /**
     * Причина сбоя, указанная пользователем. Заполняется, если пользователь не согласен с AI.
     */
    @Column(length = 2000)
    private String userProvidedReason;

    /**
     * Решение проблемы, предложенное пользователем.
     */
    @Column(length = 2000)
    private String userProvidedSolution;

    /**
     * Свободный текстовый комментарий от пользователя.
     */
    @Column(length = 1000)
    private String comments;
}
