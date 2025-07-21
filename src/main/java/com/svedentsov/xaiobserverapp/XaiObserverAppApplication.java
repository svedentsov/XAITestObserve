package com.svedentsov.xaiobserverapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения XAI Observer.
 * Точка входа для запуска Spring Boot приложения.
 */
@SpringBootApplication
public class XaiObserverAppApplication {
    /**
     * Основной метод для запуска приложения Spring Boot.
     *
     * @param args Аргументы командной строки, передаваемые при запуске приложения.
     */
    public static void main(String[] args) {
        SpringApplication.run(XaiObserverAppApplication.class, args);
    }
}
