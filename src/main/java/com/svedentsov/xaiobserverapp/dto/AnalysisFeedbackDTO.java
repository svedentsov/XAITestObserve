package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;

/**
 * Объект передачи данных (DTO) для обратной связи по результатам анализа.
 * Используется для получения данных от клиента при отправке фидбека
 * о корректности автоматического анализа.
 */
@Data
public class AnalysisFeedbackDTO {
    /**
     * Флаг, указывающий, был ли анализ корректным (true) или нет (false).
     */
    private boolean isCorrect;
    /**
     * Комментарий пользователя относительно анализа. Может быть пустым.
     */
    private String userComment;
    /**
     * Имя пользователя, отправившего обратную связь.
     */
    private String username;
}
