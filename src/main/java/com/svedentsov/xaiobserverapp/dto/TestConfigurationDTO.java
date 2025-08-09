package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления конфигурации тестового запуска.
 *
 * @param appVersion  Версия приложения.
 * @param environment Окружение.
 * @param testSuite   Тестовый набор.
 */
@Schema(description = "Конфигурация тестового запуска")
public record TestConfigurationDTO(

        @Schema(description = "Версия приложения", example = "2.1.0-release")
        String appVersion,

        @Schema(description = "Окружение", example = "QA-STAND")
        String environment,

        @Schema(description = "Тестовый набор", example = "Regression")
        String testSuite
) {
}
