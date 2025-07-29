package com.svedentsov.xaiobserverapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, когда запрашиваемый ресурс не может быть найден в системе.
 * <p>
 * Аннотация {@code @ResponseStatus(HttpStatus.NOT_FOUND)} автоматически устанавливает
 * HTTP-статус 404 для ответа, если это исключение не перехвачено.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Конструктор, принимающий сообщение об ошибке.
     *
     * @param message Сообщение, описывающее, какой ресурс не был найден.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
