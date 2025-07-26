package com.svedentsov.xaiobserverapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class XaiObserverAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(XaiObserverAppApplication.class, args);
    }
}
