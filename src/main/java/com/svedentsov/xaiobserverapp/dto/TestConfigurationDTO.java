package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import lombok.Data;

/**
 * Объект передачи данных (DTO) для конфигурации тестового запуска.
 * Используется для отображения и передачи информации о среде и версии приложения,
 * на которой выполнялся тест.
 */
@Data
public class TestConfigurationDTO {
    /**
     * Версия приложения, на которой выполнялся тест.
     */
    private String appVersion;
    /**
     * Среда выполнения теста (например, "QA", "STAGING").
     */
    private String environment;
    /**
     * Название тестового набора.
     */
    private String testSuite;

    /**
     * Статический фабричный метод для создания {@code TestConfigurationDTO}
     * из сущности {@link TestConfiguration}.
     *
     * @param config Сущность {@link TestConfiguration}, из которой создается DTO.
     * @return Новый экземпляр {@link TestConfigurationDTO}.
     */
    public static TestConfigurationDTO fromEntity(TestConfiguration config) {
        TestConfigurationDTO dto = new TestConfigurationDTO();
        dto.setAppVersion(config.getAppVersion());
        dto.setEnvironment(config.getEnvironment());
        dto.setTestSuite(config.getTestSuite());
        return dto;
    }
}
