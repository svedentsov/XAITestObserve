package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO, представляющий конфигурацию тестового запуска (версия, окружение, набор тестов).
 */
@Data
@Schema(description = "Конфигурация тестового запуска")
public class TestConfigurationDTO {

    @Schema(description = "Версия приложения", example = "2.1.0-release")
    private String appVersion;

    @Schema(description = "Окружение", example = "QA-STAND")
    private String environment;

    @Schema(description = "Тестовый набор", example = "Regression")
    private String testSuite;

    public static TestConfigurationDTO fromEntity(TestConfiguration config) {
        TestConfigurationDTO dto = new TestConfigurationDTO();
        dto.setAppVersion(config.getAppVersion());
        dto.setEnvironment(config.getEnvironment());
        dto.setTestSuite(config.getTestSuite());
        return dto;
    }
}
