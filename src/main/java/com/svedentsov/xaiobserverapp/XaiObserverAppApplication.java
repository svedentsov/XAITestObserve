package com.svedentsov.xaiobserverapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Главный класс приложения XAI Observer.
 * <p>
 * Этот класс является точкой входа для Spring Boot приложения. Он инициализирует
 * все компоненты, включает поддержку асинхронных операций и кэширования.
 * Также здесь определяется основная информация для OpenAPI (Swagger) документации.
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@OpenAPIDefinition(info = @Info(
        title = "XAI Observer API",
        version = "1.0.0",
        description = "API для системы анализа и хранения результатов UI-тестов. Позволяет регистрировать тестовые запуски, получать по ним детальную информацию и статистику."))
public class XaiObserverAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(XaiObserverAppApplication.class, args);
    }
}
