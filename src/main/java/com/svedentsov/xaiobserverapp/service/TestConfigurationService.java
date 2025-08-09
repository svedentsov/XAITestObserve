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
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Сервис для управления сущностями {@link TestConfiguration}.
 * Отвечает за поиск существующих и создание новых конфигураций,
 * обеспечивая их уникальность.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigurationService {

    private final TestConfigurationRepository testConfigurationRepository;

    /**
     * Находит существующую конфигурацию на основе данных из DTO события или создает новую.
     * Использует кэширование и принцип "get-or-create".
     *
     * @param event DTO события завершения теста.
     * @return Существующая или только что созданная сущность {@link TestConfiguration}.
     */
    public TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        String uniqueName = buildUniqueName(event);
        return testConfigurationRepository.findByUniqueName(uniqueName)
                .orElseGet(() -> createConfiguration(event, uniqueName));
    }

    /**
     * Создает и сохраняет новую сущность {@link TestConfiguration}.
     * Метод выполняется в новой транзакции (REQUIRES_NEW) для предотвращения проблем
     * с параллельным созданием одинаковых конфигураций (race condition).
     *
     * @param event      DTO события для извлечения данных.
     * @param uniqueName Уникальное имя для новой конфигурации.
     * @return Сохраненная сущность.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TestConfiguration createConfiguration(FailureEventDTO event, String uniqueName) {
        try {
            log.info("Creating new configuration for uniqueName: {}", uniqueName);
            TestConfiguration newConfig = new TestConfiguration();
            newConfig.setAppVersion(StringUtils.hasText(event.appVersion()) ? event.appVersion() : "unknown");
            newConfig.setTestSuite(StringUtils.hasText(event.testSuite()) ? event.testSuite() : "default");

            String environment = Optional.ofNullable(event.environmentDetails())
                    .map(EnvironmentDetailsDTO::name)
                    .filter(StringUtils::hasText)
                    .orElse("unknown");
            newConfig.setEnvironment(environment);

            newConfig.setUniqueName(uniqueName);
            return testConfigurationRepository.saveAndFlush(newConfig);
        } catch (DataIntegrityViolationException e) {
            log.warn("Race condition detected while creating configuration for uniqueName: {}. Fetching existing one.", uniqueName);
            return testConfigurationRepository.findByUniqueName(uniqueName)
                    .orElseThrow(() -> new IllegalStateException("FATAL: Could not find configuration for " + uniqueName + " after a race condition."));
        }
    }

    /**
     * Генерирует уникальное строковое имя для конфигурации на основе
     * версии приложения, тестового набора и окружения.
     *
     * @param event DTO события.
     * @return Уникальная строка в нижнем регистре.
     */
    private String buildUniqueName(FailureEventDTO event) {
        String appVersion = StringUtils.hasText(event.appVersion()) ? event.appVersion() : "unknown";
        String testSuite = StringUtils.hasText(event.testSuite()) ? event.testSuite() : "default";

        String environmentName = Optional.ofNullable(event.environmentDetails())
                .map(EnvironmentDetailsDTO::name)
                .filter(StringUtils::hasText)
                .orElse("unknown");

        return String.format("%s-%s-%s", appVersion, testSuite, environmentName).toLowerCase();
    }
}
