package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import lombok.Data;

/**
 * Объект передачи данных (DTO) для результатов автоматического анализа.
 * Используется для отображения информации об анализе в пользовательском интерфейсе.
 */
@Data
public class AnalysisResultDTO {
    /**
     * Уникальный идентификатор результата анализа.
     */
    private String id;
    /**
     * Тип проведенного анализа (например, "Анализ шага сбоя", "Анализ по типу исключения").
     */
    private String analysisType;
    /**
     * Предложенная причина сбоя на основе анализа.
     */
    private String suggestedReason;
    /**
     * Предложенное решение или рекомендации по устранению сбоя.
     */
    private String solution;
    /**
     * Уровень уверенности AI в предложенной причине/решении (от 0.0 до 1.0).
     */
    private Double aiConfidence;
    /**
     * Сырые данные, использованные для анализа, или дополнительная информация.
     */
    private String rawData;

    /**
     * Статический фабричный метод для создания {@code AnalysisResultDTO}
     * из сущности {@link AnalysisResult}.
     *
     * @param result Сущность {@link AnalysisResult}, из которой создается DTO.
     * @return Новый экземпляр {@link AnalysisResultDTO}.
     */
    public static AnalysisResultDTO fromEntity(AnalysisResult result) {
        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setId(result.getId());
        dto.setAnalysisType(result.getAnalysisType());
        dto.setSuggestedReason(result.getSuggestedReason());
        dto.setSolution(result.getSolution());
        dto.setAiConfidence(result.getAiConfidence());
        dto.setRawData(result.getRawData());
        return dto;
    }
}
