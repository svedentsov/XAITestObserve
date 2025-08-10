package com.svedentsov.xaiobserverapp.config;

import com.svedentsov.xaiobserverapp.dto.ApiErrorResponse;
import com.svedentsov.xaiobserverapp.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Этот класс, аннотированный {@link ControllerAdvice}, централизованно перехватывает исключения,
 * возникающие в контроллерах, и формирует стандартизированные HTTP-ответы об ошибках.
 * Это позволяет избежать дублирования кода обработки ошибок в каждом контроллере (принцип DRY)
 * и обеспечивает консистентный формат API-ошибок.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение {@link ResourceNotFoundException}, которое выбрасывается при отсутствии ресурса.
     * Возвращает клиенту статус 404 NOT FOUND.
     *
     * @param ex Исключение, которое было выброшено.
     * @return {@link ResponseEntity} со статусом 404 и телом ошибки.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        var errorResponse = new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает исключение {@link MethodArgumentNotValidException}, возникающее при ошибках валидации DTO
     * (например, когда поля, аннотированные @NotBlank, пусты).
     * Возвращает клиенту статус 400 BAD REQUEST с детальным описанием ошибок валидации.
     *
     * @param ex Исключение с информацией об ошибках валидации.
     * @return {@link ResponseEntity} со статусом 400 и телом, содержащим детали ошибок.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        log.warn("Validation error: {}", errorMessage);
        var errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed: " + errorMessage, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает все остальные непредвиденные исключения как "fallback" механизм.
     * Логирует полную ошибку для последующего анализа и возвращает общий ответ
     * со статусом 500 INTERNAL_SERVER_ERROR, чтобы не раскрывать внутренние детали системы.
     *
     * @param ex Любое непредвиденное исключение.
     * @return {@link ResponseEntity} со статусом 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex) {
        log.error("An unexpected internal error occurred: ", ex);
        var errorResponse = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An internal server error occurred. Please check server logs for details.", LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
