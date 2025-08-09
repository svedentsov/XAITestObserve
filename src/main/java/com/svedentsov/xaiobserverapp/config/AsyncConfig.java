package com.svedentsov.xaiobserverapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Конфигурация для асинхронных операций в приложении.
 * Включает поддержку асинхронного выполнения методов, аннотированных {@code @Async},
 * и настраивает основной пул потоков для этих задач. Это позволяет выполнять
 * ресурсоемкие операции (например, обработку событий) в фоновом режиме, не блокируя основной поток запроса.
 * Решает проблему "More than one TaskExecutor bean found...".
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Создает и настраивает основной пул потоков для выполнения @Async задач.
     * Аннотация @Primary указывает Spring Boot использовать именно этот TaskExecutor
     * по умолчанию, когда встречается @Async без указания конкретного исполнителя.
     *
     * @return настроенный TaskExecutor.
     */
    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Оптимальное количество потоков, обычно основанное на количестве ядер CPU.
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncTask-");
        executor.initialize();
        return executor;
    }
}
