package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.EnvironmentDetailsDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import com.svedentsov.xaiobserverapp.repository.TestConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Сервис для управления сущностями {@link TestConfiguration}.
 * Отвечает за поиск существующих и создание новых конфигураций,
 * обеспечивая их уникальность в конкурентной среде.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigurationService {

    private final TestConfigurationRepository testConfigurationRepository;

    /**
     * Находит существующую конфигурацию или создает новую, если она не найдена.
     * Этот метод является потокобезопасным и решает проблему "get-or-create".
     *
     * @param event DTO события завершения теста.
     * @return Существующая или только что созданная сущность {@link TestConfiguration}.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        final String uniqueName = buildUniqueName(event);
        return testConfigurationRepository.findByUniqueName(uniqueName)
                .orElseGet(() -> createConfigurationWithRaceConditionHandling(event, uniqueName));
    }

    /**
     * Этот приватный метод инкапсулирует логику создания новой конфигурации.
     * Он выполняется в НОВОЙ транзакции (REQUIRES_NEW), чтобы немедленно зафиксировать
     * новую запись и сделать её видимой для других параллельных запросов.
     * Если другой поток успевает создать такую же конфигурацию, наш `saveAndFlush`
     * вызовет {@link DataIntegrityViolationException} из-за нарушения unique constraint.
     * Мы перехватываем это исключение и просто запрашиваем уже созданную запись из БД.
     *
     * @param event      DTO для извлечения данных.
     * @param uniqueName Уникальное имя для новой конфигурации.
     * @return Сохраненная сущность.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected TestConfiguration createConfigurationWithRaceConditionHandling(FailureEventDTO event, String uniqueName) {
        // Повторная проверка внутри новой транзакции (Double-checked locking pattern)
        // минимизирует вероятность DataIntegrityViolationException.
        return testConfigurationRepository.findByUniqueName(uniqueName).orElseGet(() -> {
            try {
                log.info("Attempting to create a new test configuration for uniqueName: {}", uniqueName);
                var newConfig = new TestConfiguration();
                newConfig.setAppVersion(Optional.ofNullable(event.appVersion()).filter(s -> !s.isBlank()).orElse("unknown"));
                newConfig.setTestSuite(Optional.ofNullable(event.testSuite()).filter(s -> !s.isBlank()).orElse("default"));
                String environment = Optional.ofNullable(event.environmentDetails())
                        .map(EnvironmentDetailsDTO::name)
                        .filter(s -> !s.isBlank())
                        .orElse("unknown");
                newConfig.setEnvironment(environment);
                newConfig.setUniqueName(uniqueName);
                return testConfigurationRepository.saveAndFlush(newConfig);
            } catch (DataIntegrityViolationException e) {
                log.warn("Race condition detected while creating configuration for uniqueName: {}. Re-fetching existing one.", uniqueName);
                // Если произошла ошибка целостности, значит, другой поток уже создал запись.
                // Мы уверены, что теперь она существует, и можем смело ее запрашивать.
                return testConfigurationRepository.findByUniqueName(uniqueName)
                        .orElseThrow(() -> new IllegalStateException("FATAL: Could not find configuration for " + uniqueName + " after a race condition. This should not happen."));
            }
        });
    }

    /**
     * Генерирует уникальное строковое имя для конфигурации на основе
     * версии приложения, тестового набора и окружения.
     *
     * @param event DTO события.
     * @return Уникальная строка в нижнем регистре.
     */
    private String buildUniqueName(FailureEventDTO event) {
        String appVersion = Optional.ofNullable(event.appVersion()).filter(s -> !s.isBlank()).orElse("unknown");
        String testSuite = Optional.ofNullable(event.testSuite()).filter(s -> !s.isBlank()).orElse("default");
        String environmentName = Optional.ofNullable(event.environmentDetails())
                .map(EnvironmentDetailsDTO::name)
                .filter(s -> !s.isBlank())
                .orElse("unknown");

        return String.join(":", appVersion, testSuite, environmentName).toLowerCase();
    }
}
