package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import lombok.Data;

@Data
public class TestConfigurationDTO {
    private String appVersion;
    private String environment;
    private String testSuite;

    public static TestConfigurationDTO fromEntity(TestConfiguration config) {
        TestConfigurationDTO dto = new TestConfigurationDTO();
        dto.setAppVersion(config.getAppVersion());
        dto.setEnvironment(config.getEnvironment());
        dto.setTestSuite(config.getTestSuite());
        return dto;
    }
}
