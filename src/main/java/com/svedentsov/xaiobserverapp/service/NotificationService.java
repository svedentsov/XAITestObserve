package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.model.TestRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки уведомлений.
 * <p>
 * В текущей реализации эмулирует отправку уведомлений путем логирования.
 * Может быть расширен для интеграции с реальными системами (Slack, email и т.д.).
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * Отправляет уведомление о сбое теста.
     *
     * @param testRun Сущность проваленного тестового запуска.
     */
    public void notifyAboutFailure(TestRun testRun) {
        String testName = String.format("%s.%s", testRun.getTestClass(), testRun.getTestMethod());
        log.warn("!!! УВЕДОМЛЕНИЕ О СБОЕ !!! Тест '{}' провален. ID запуска: {}. Пожалуйста, проверьте дашборд.", testName, testRun.getId());
    }
}
