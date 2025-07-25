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

@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigurationService {

    private final TestConfigurationRepository testConfigurationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        String uniqueName = buildUniqueName(event);

        return testConfigurationRepository.findByUniqueName(uniqueName)
                .orElseGet(() -> {
                    try {
                        log.info("Creating new configuration for uniqueName: {}", uniqueName);
                        TestConfiguration newConfig = createNewConfiguration(event, uniqueName);
                        return testConfigurationRepository.save(newConfig);
                    } catch (DataIntegrityViolationException e) {
                        // Эта ветка сработает, если другой поток успел создать такую же конфигурацию
                        // между нашим findByUniqueName и save. Это надежный способ обработки гонки состояний.
                        log.warn("Race condition detected. Another thread already created the configuration. Fetching existing one. UniqueName: {}", uniqueName);
                        return testConfigurationRepository.findByUniqueName(uniqueName)
                                .orElseThrow(() -> new IllegalStateException("Could not find configuration after race condition: " + uniqueName));
                    }
                });
    }

    private TestConfiguration createNewConfiguration(FailureEventDTO event, String uniqueName) {
        TestConfiguration newConfig = new TestConfiguration();
        newConfig.setAppVersion(StringUtils.hasText(event.getAppVersion()) ? event.getAppVersion() : "unknown");
        newConfig.setTestSuite(StringUtils.hasText(event.getTestSuite()) ? event.getTestSuite() : "default");
        newConfig.setEnvironment(event.getEnvironmentDetails() != null && StringUtils.hasText(event.getEnvironmentDetails().getName()) ? event.getEnvironmentDetails().getName() : "unknown");
        newConfig.setUniqueName(uniqueName);
        return newConfig;
    }

    private String buildUniqueName(FailureEventDTO event) {
        String appVersion = StringUtils.hasText(event.getAppVersion()) ? event.getAppVersion() : "unknown";
        String testSuite = StringUtils.hasText(event.getTestSuite()) ? event.getTestSuite() : "default";
        String environmentName = "unknown";
        String environmentDetailsHash = "unknown_env";

        if (event.getEnvironmentDetails() != null) {
            environmentName = StringUtils.hasText(event.getEnvironmentDetails().getName()) ? event.getEnvironmentDetails().getName() : "unknown";
            environmentDetailsHash = String.format("%s-%s-%s-%s-%s-%s-%s-%s-%s",
                    environmentName,
                    event.getEnvironmentDetails().getOsType(),
                    event.getEnvironmentDetails().getOsVersion(),
                    event.getEnvironmentDetails().getBrowserType(),
                    event.getEnvironmentDetails().getBrowserVersion(),
                    event.getEnvironmentDetails().getScreenResolution(),
                    event.getEnvironmentDetails().getDeviceType(),
                    event.getEnvironmentDetails().getDeviceName(),
                    event.getEnvironmentDetails().getDriverVersion()
            ).toLowerCase().replaceAll("[^a-z0-9-]", "_");
        }
        return String.format("%s-%s-%s-%s", appVersion, testSuite, environmentName, environmentDetailsHash);
    }
}
