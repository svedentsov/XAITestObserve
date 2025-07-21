package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая обратную связь пользователя по результатам
 * автоматического анализа сбоя теста.
 * Позволяет собирать информацию о том, насколько точным был предложенный AI анализ.
 */
@Entity
@Data
@NoArgsConstructor
public class AnalysisFeedback {
    /**
     * Уникальный идентификатор записи обратной связи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на результат анализа, к которому относится данная обратная связь.
     * Использование {@code FetchType.LAZY} для отложенной загрузки, чтобы избежать
     * лишних запросов к базе данных, когда сам объект {@code AnalysisFeedback}
     * загружается без необходимости доступа к деталям {@code AnalysisResult}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;

    /**
     * Флаг, указывающий, был ли анализ корректным (true) или нет (false)
     * по мнению пользователя.
     */
    private boolean isCorrect;

    /**
     * Комментарий пользователя относительно анализа.
     * Установлена длина столбца 2000 символов для возможности развернутых комментариев.
     */
    @Column(length = 2000)
    private String userComment;

    /**
     * Имя пользователя, оставившего обратную связь.
     */
    private String username;

    /**
     * Временная метка создания обратной связи.
     */
    private LocalDateTime feedbackTimestamp;
}
