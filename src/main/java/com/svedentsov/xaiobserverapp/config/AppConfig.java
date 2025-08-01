package com.svedentsov.xaiobserverapp.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    /**
     * Создает и настраивает бин RestTemplate для выполнения HTTP-запросов.
     * Использование RestTemplateBuilder является рекомендуемой практикой.
     *
     * @param builder Строитель, предоставляемый Spring Boot.
     * @return Настроенный экземпляр RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // Устанавливаем таймаут на установку соединения (например, 5 секунд)
                .setConnectTimeout(Duration.ofSeconds(5))
                // Устанавливаем таймаут на чтение ответа (например, 10 секунд)
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
