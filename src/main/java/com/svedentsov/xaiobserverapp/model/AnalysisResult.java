package com.svedentsov.xaiobserverapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Сущность, представляющая результат анализа причин сбоя (RCA) для одного тестового запуска.
 * Один тестовый запуск может иметь несколько результатов анализа от разных стратегий.
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"testRun", "feedback"})
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "analysis_result")
public class AnalysisResult {

    /**
     * Уникальный идентификатор результата анализа (UUID).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Тестовый запуск, к которому относится этот анализ.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    /**
     * Тип анализатора, сгенерировавшего этот результат (например, "Анализ по типу исключения").
     */
    private String analysisType;

    /**
     * Предполагаемая причина сбоя, предложенная анализатором.
     */
    @Column(length = 2000)
    private String suggestedReason;

    /**
     * Предлагаемое решение проблемы.
     */
    @Column(length = 4000)
    private String solution;

    /**
     * Уверенность AI в данном анализе (от 0.0 до 1.0).
     */
    private Double aiConfidence;

    /**
     * Временная метка проведения анализа.
     */
    private LocalDateTime analysisTimestamp;

    /**
     * Структурированные данные, объясняющие предсказание (например, от LIME/SHAP), хранятся в формате JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, Object> explanationData;

    /**
     * Флаг, указывающий, подтвердил ли пользователь корректность этого анализа.
     * Обновляется через сервис обратной связи {@link com.svedentsov.xaiobserverapp.service.FeedbackService}.
     */
    private Boolean userConfirmedCorrect;

    /**
     * Список отзывов от пользователей по данному результату анализа.
     */
    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AnalysisFeedback> feedback;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResult that = (AnalysisResult) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
