package com.svedentsov.xaiobserverapp.service;

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
 * <p>
 * Отвечает за поиск существующих и создание новых конфигураций,
 * обеспечивая их уникальность.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigurationService {

    private final TestConfigurationRepository testConfigurationRepository;

    /**
     * Находит существующую или создает новую конфигурацию на основе данных из события.
     *
     * @param event DTO события завершения теста.
     * @return Сущность {@link TestConfiguration}, либо новая, либо уже существующая в БД.
     */
    public TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        String uniqueName = buildUniqueName(event);
        Optional<TestConfiguration> existingConfig = this.findByUniqueName(uniqueName);
        return existingConfig.orElseGet(() -> createConfiguration(event, uniqueName));
    }

    /**
     * Находит конфигурацию по ее уникальному имени.
     *
     * @param uniqueName Уникальное имя.
     * @return Optional с найденной конфигурацией.
     */
    @Transactional(readOnly = true)
    public Optional<TestConfiguration> findByUniqueName(String uniqueName) {
        return testConfigurationRepository.findByUniqueName(uniqueName);
    }

    /**
     * Создает и сохраняет новую конфигурацию в новой транзакции.
     * Обрабатывает возможные состояния гонки при одновременном создании одинаковых конфигураций.
     *
     * @param event      DTO события.
     * @param uniqueName Уникальное имя для новой конфигурации.
     * @return Сохраненная сущность {@link TestConfiguration}.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TestConfiguration createConfiguration(FailureEventDTO event, String uniqueName) {
        try {
            log.info("Creating new configuration for uniqueName: {}", uniqueName);
            TestConfiguration newConfig = new TestConfiguration();
            newConfig.setAppVersion(StringUtils.hasText(event.getAppVersion()) ? event.getAppVersion() : "unknown");
            newConfig.setTestSuite(StringUtils.hasText(event.getTestSuite()) ? event.getTestSuite() : "default");
            newConfig.setEnvironment(event.getEnvironmentDetails() != null && StringUtils.hasText(event.getEnvironmentDetails().getName()) ? event.getEnvironmentDetails().getName() : "unknown");
            newConfig.setUniqueName(uniqueName);
            return testConfigurationRepository.saveAndFlush(newConfig);
        } catch (DataIntegrityViolationException e) {
            log.warn("Race condition detected while creating configuration for uniqueName: {}. Fetching existing one.", uniqueName);
            return testConfigurationRepository.findByUniqueName(uniqueName)
                    .orElseThrow(() -> new IllegalStateException("FATAL: Could not find configuration for " + uniqueName + " after a race condition."));
        }
    }

    /**
     * Строит уникальное имя для конфигурации на основе версии, набора тестов и окружения.
     *
     * @param event DTO события.
     * @return Строка с уникальным именем.
     */
    private String buildUniqueName(FailureEventDTO event) {
        String appVersion = StringUtils.hasText(event.getAppVersion()) ? event.getAppVersion() : "unknown";
        String testSuite = StringUtils.hasText(event.getTestSuite()) ? event.getTestSuite() : "default";
        String environmentName = "unknown";
        if (event.getEnvironmentDetails() != null && StringUtils.hasText(event.getEnvironmentDetails().getName())) {
            environmentName = event.getEnvironmentDetails().getName();
        }
        return String.format("%s-%s-%s", appVersion, testSuite, environmentName).toLowerCase();
    }
}
