package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.model.TestRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    public void notifyAboutFailure(TestRun testRun) {
        String testName = String.format("%s.%s", testRun.getTestClass(), testRun.getTestMethod());
        log.warn("!!! УВЕДОМЛЕНИЕ О СБОЕ !!! Тест '{}' провален. ID запуска: {}. Пожалуйста, проверьте дашборд.", testName, testRun.getId());
    }
}
