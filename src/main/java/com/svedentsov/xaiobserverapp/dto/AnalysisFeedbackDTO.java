package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;

/**
 * Объект передачи данных (DTO) для обратной связи по результатам анализа.
 * Используется для получения данных от клиента при отправке фидбека
 * о корректности автоматического анализа, предоставляя детальный контекст
 * для обучения и улучшения AI-моделей.
 */
@Data
public class AnalysisFeedbackDTO {
    /**
     * Флаг, указывающий, было ли предложенное AI-решение корректным (true) или нет (false).
     */
    private Boolean isAiSuggestionCorrect;
    /**
     * Пользовательская причина, если предложенная AI была некорректной или неполной.
     * Ожидается заполнение, если 'isAiSuggestionCorrect' равно false.
     */
    private String userProvidedReason;
    /**
     * Пользовательское решение, если предложенное AI было некорректным или неполным.
     * Ожидается заполнение, если 'isAiSuggestionCorrect' равно false.
     */
    private String userProvidedSolution;
    /**
     * Общие комментарии пользователя относительно анализа. Может быть пустым.
     */
    private String comments;
    /**
     * Идентификатор пользователя, отправившего обратную связь (например, email или UUID).
     */
    private String userId;
}
