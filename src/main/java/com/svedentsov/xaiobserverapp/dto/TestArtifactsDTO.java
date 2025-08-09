package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO для передачи ссылок на артефакты, связанные с тестовым запуском
 * (скриншоты, видео, логи).
 *
 * @param screenshotUrls       Список URL-адресов скриншотов.
 * @param videoUrl             URL-адрес видеозаписи теста.
 * @param appLogUrls           Список URL-адресов логов приложения.
 * @param browserConsoleLogUrl URL-адрес лога консоли браузера.
 * @param harFileUrl           URL-адрес HAR-файла с записью сетевой активности.
 */
@Schema(description = "Ссылки на артефакты, связанные с тестовым запуском")
public record TestArtifactsDTO(

        @Schema(description = "Список URL-адресов скриншотов", example = "[\"http://artifacts.example.com/run1/fail.png\"]")
        List<String> screenshotUrls,

        @Schema(description = "URL-адрес видеозаписи теста", example = "http://artifacts.example.com/run1/video.mp4")
        String videoUrl,

        @Schema(description = "Список URL-адресов логов приложения", example = "[\"http://artifacts.example.com/run1/app.log\"]")
        List<String> appLogUrls,

        @Schema(description = "URL-адрес лога консоли браузера", example = "http://artifacts.example.com/run1/console.log")
        String browserConsoleLogUrl,

        @Schema(description = "URL-адрес HAR-файла с записью сетевой активности", example = "http://artifacts.example.com/run1/network.har")
        String harFileUrl
) {
}
