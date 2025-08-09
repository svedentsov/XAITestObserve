package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO для стандартизированного ответа об ошибке, возвращаемого API.
 * Используется {@link com.svedentsov.xaiobserverapp.config.GlobalExceptionHandler}.
 *
 * @param status    HTTP статус код.
 * @param message   Сообщение об ошибке.
 * @param timestamp Временная метка ошибки.
 */
@Schema(description = "Стандартизированный ответ об ошибке API")
public record ApiErrorResponse(

        @Schema(description = "HTTP статус код", example = "404")
        int status,

        @Schema(description = "Сообщение об ошибке", example = "TestRun with ID ... not found.")
        String message,

        @Schema(description = "Временная метка ошибки")
        LocalDateTime timestamp
) {
}
