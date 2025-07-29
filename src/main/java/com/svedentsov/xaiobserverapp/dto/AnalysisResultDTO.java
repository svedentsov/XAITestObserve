package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO, представляющий результат AI-анализа для конкретного тестового запуска.
 * Содержит предложенные причину и решение, а также уровень уверенности AI.
 */
@Data
@Schema(description = "Результат AI-анализа для конкретного тестового запуска")
public class AnalysisResultDTO {

    @Schema(description = "Уникальный ID результата анализа (UUID)", example = "f0a1b2c3-d4e5-f678-90a1-b2c3d4e5f678")
    private String id;

    @Schema(description = "Тип проведенного анализа", example = "Анализ по типу исключения")
    private String analysisType;

    @Schema(description = "Предполагаемая причина сбоя, определенная AI", example = "Элемент не был найден, т.к. локатор устарел после обновления UI.")
    private String suggestedReason;

    @Schema(description = "Предлагаемое решение проблемы", example = "1. Обновите локатор элемента. 2. Добавьте явное ожидание (WebDriverWait).")
    private String solution;

    @Schema(description = "Уверенность AI в данном анализе (от 0.0 до 1.0)", example = "0.95")
    private Double aiConfidence;

    @Schema(description = "Сырые данные, на основе которых был сделан вывод (например, тип исключения, шаг)", example = "Exception Type: org.openqa.selenium.StaleElementReferenceException")
    private String rawData;

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
