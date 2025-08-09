package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления информации о медленно выполняющемся тесте.
 * Используется в статистических отчетах.
 *
 * @param testName              Полное имя теста (класс + метод).
 * @param averageDurationMillis Средняя длительность выполнения в миллисекундах.
 */
@Schema(description = "Информация о медленном тесте")
public record SlowTestDTO(

        @Schema(description = "Полное имя теста", example = "com.tests.Profile.testFullProfileUpdate")
        String testName,

        @Schema(description = "Средняя длительность выполнения в миллисекундах", example = "45789.5")
        double averageDurationMillis
) {
}
