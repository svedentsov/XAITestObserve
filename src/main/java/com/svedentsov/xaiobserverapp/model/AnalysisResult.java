package com.svedentsov.xaiobserverapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class AnalysisResult {
    /**
     * Уникальный идентификатор результата анализа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип проведенного анализа (например, "Анализ шага сбоя", "Анализ по типу исключения").
     */
    private String analysisType;

    /**
     * Предложенная причина сбоя на основе анализа.
     * Установлена длина столбца 2000 символов для возможности развернутого описания.
     */
    @Column(length = 2000)
    private String suggestedReason;

    /**
     * Предложенное решение или рекомендации по устранению сбоя.
     * Установлена длина столбца 2000 символов для возможности развернутого описания.
     */
    @Column(length = 2000)
    private String solution;

    /**
     * Уровень уверенности AI в предложенной причине/решении (от 0.0 до 1.0).
     */
    private Double aiConfidence;

    /**
     * Сырые данные, использованные для анализа, или дополнительная информация,
     * полезная для отладки или понимания анализа.
     * Установлена длина столбца 4000 символов для возможности хранения большого объема данных.
     */
    @Column(length = 4000)
    private String rawData;

    /**
     * Список записей обратной связи, связанных с данным результатом анализа.
     * {@code mappedBy = "analysisResult"} указывает, что связь управляется полем {@code analysisResult}
     * в сущности {@link AnalysisFeedback}.
     * {@code CascadeType.ALL} означает, что все операции (сохранение, удаление) будут каскадно применяться.
     * {@code orphanRemoval = true} удаляет дочерние сущности, если они отсоединяются от родителя.
     * {@code @JsonIgnore} предотвращает рекурсивную сериализацию при преобразовании в JSON.
     */
    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AnalysisFeedback> feedback;
}
