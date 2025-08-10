package com.svedentsov.xaiobserverapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Главный класс приложения XAI Observer.
 * <p>
 * Аннотации:
 * <ul>
 *   <li>{@code @SpringBootApplication} - основная аннотация Spring Boot, включающая автоконфигурацию, сканирование компонентов и конфигурацию.</li>
 *   <li>{@code @EnableAsync} - включает поддержку асинхронных методов, аннотированных {@code @Async}.</li>
 *   <li>{@code @EnableCaching} - включает механизм кэширования Spring для повышения производительности.</li>
 *   <li>{@code @OpenAPIDefinition} - настраивает метаданные для Swagger/OpenAPI документации.</li>
 *   <li>{@code @EntityScan} - явно указывает Spring, где искать JPA-сущности.</li>
 * </ul>
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@OpenAPIDefinition(info = @Info(
        title = "XAI Observer API",
        version = "1.0.0",
        description = "API для системы анализа и хранения результатов UI-тестов. Позволяет регистрировать тестовые запуски, получать по ним детальную информацию и статистику."))
@EntityScan("com.svedentsov.xaiobserverapp.model")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class XaiObserverAppApplication {
    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки.
     */
    public static void main(String[] args) {
        SpringApplication.run(XaiObserverAppApplication.class, args);
    }
}
