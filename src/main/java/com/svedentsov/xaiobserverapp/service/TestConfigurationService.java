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

@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigurationService {
    private final TestConfigurationRepository testConfigurationRepository;

    public TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        String uniqueName = buildUniqueName(event);
        Optional<TestConfiguration> existingConfig = this.findByUniqueName(uniqueName);
        return existingConfig.orElseGet(() -> createConfiguration(event, uniqueName));
    }

    @Transactional(readOnly = true)
    public Optional<TestConfiguration> findByUniqueName(String uniqueName) {
        return testConfigurationRepository.findByUniqueName(uniqueName);
    }

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
