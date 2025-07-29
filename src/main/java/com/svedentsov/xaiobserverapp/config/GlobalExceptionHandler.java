package com.svedentsov.xaiobserverapp.config;

import com.svedentsov.xaiobserverapp.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Глобальный обработчик исключений для всего приложения.
 * <p>
 * Перехватывает исключения, выброшенные контроллерами, и формирует
 * стандартизированные ответы об ошибках в формате JSON.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения, когда запрашиваемый ресурс не найден.
     *
     * @param ex Исключение {@link ResourceNotFoundException}.
     * @return Ответ с кодом 404 (Not Found) и сообщением об ошибке.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает ошибки валидации DTO, полученных в теле запроса.
     *
     * @param ex Исключение {@link MethodArgumentNotValidException}, возникающее при ошибке валидации.
     * @return Ответ с кодом 400 (Bad Request) и первым сообщением об ошибке валидации.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<>(Map.of("error", "Invalid input: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает все остальные непредвиденные исключения.
     *
     * @param ex Любое необработанное исключение.
     * @return Ответ с кодом 500 (Internal Server Error) и общим сообщением об ошибке.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        return new ResponseEntity<>(Map.of("error", "An internal server error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
