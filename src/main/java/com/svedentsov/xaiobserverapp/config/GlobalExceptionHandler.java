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
 * Перехватывает исключения, возникающие в контроллерах, и формирует
 * стандартизированные ответы об ошибках в формате {@link ApiErrorResponse}.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение {@link ResourceNotFoundException}, которое выбрасывается при отсутствии ресурса.
     *
     * @param ex Исключение, которое было выброшено.
     * @return {@link ResponseEntity} со статусом 404 NOT_FOUND и телом ошибки.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        var errorResponse = new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает исключение {@link MethodArgumentNotValidException}, возникающее при ошибках валидации DTO.
     *
     * @param ex Исключение с информацией об ошибках валидации.
     * @return {@link ResponseEntity} со статусом 400 BAD_REQUEST и телом, содержащим детали ошибок.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Validation error: {}", errorMessage);
        var errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed: " + errorMessage, LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает все остальные непредвиденные исключения, не перехваченные другими обработчиками.
     * Логирует полную ошибку и возвращает общий ответ об ошибке сервера.
     *
     * @param ex Любое непредвиденное исключение.
     * @return {@link ResponseEntity} со статусом 500 INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        var errorResponse = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An internal server error occurred. Please check logs.", LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
