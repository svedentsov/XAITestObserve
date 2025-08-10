package com.svedentsov.xaiobserverapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация для асинхронных операций в приложении.
 * Включает поддержку асинхронного выполнения методов, аннотированных {@code @Async},
 * и настраивает основной пул потоков для этих задач. Это позволяет выполнять
 * ресурсоемкие операции (например, обработку событий) в фоновом режиме, не блокируя основной поток запроса.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Создает и настраивает основной пул потоков для выполнения @Async задач.
     * Аннотация {@code @Primary} указывает Spring Boot использовать именно этот TaskExecutor
     * по умолчанию, когда встречается {@code @Async} без указания конкретного исполнителя.
     * Это решает проблему "More than one TaskExecutor bean found...".
     *
     * @return настроенный {@link Executor}.
     */
    @Bean
    @Primary
    public Executor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        // Оптимальное количество потоков, обычно основанное на количестве ядер CPU.
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncTask-");
        // Включаем graceful shutdown, чтобы задачи успели завершиться при остановке приложения
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
