package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.model.TestRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки уведомлений о событиях тестовых запусков.
 * В текущей реализации просто логирует предупреждение о сбое теста.
 * В будущем может быть расширен для отправки уведомлений по email, Slack и т.д.
 */
@Slf4j
@Service
public class NotificationService {
    /**
     * Уведомляет о проваленном тестовом запуске.
     * Записывает предупреждение в лог, указывая имя теста и ID запуска.
     *
     * @param testRun Объект {@link TestRun}, содержащий информацию о проваленном тесте.
     */
    public void notifyAboutFailure(TestRun testRun) {
        String testName = String.format("%s.%s", testRun.getTestClass(), testRun.getTestMethod());
        log.warn("!!! УВЕДОМЛЕНИЕ О СБОЕ !!! Тест '{}' провален. ID запуска: {}. Пожалуйста, проверьте дашборд.", testName, testRun.getId());
    }
}
