package com.svedentsov.xaiobserverapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DTO, содержащий ссылки на артефакты, связанные с тестовым запуском (скриншоты, видео, логи).
 * Этот класс является встраиваемым (Embeddable).
 */
@Data
@Schema(description = "Ссылки на артефакты, связанные с тестовым запуском")
public class TestArtifactsDTO {

    @Schema(description = "Список URL-адресов скриншотов", example = "[\"http://artifacts.example.com/run1/fail.png\"]")
    private List<String> screenshotUrls;

    @Schema(description = "URL-адрес видеозаписи теста", example = "http://artifacts.example.com/run1/video.mp4")
    private String videoUrl;

    @Schema(description = "Список URL-адресов логов приложения", example = "[\"http://artifacts.example.com/run1/app.log\"]")
    private List<String> appLogUrls;

    @Schema(description = "URL-адрес лога консоли браузера", example = "http://artifacts.example.com/run1/console.log")
    private String browserConsoleLogUrl;

    @Schema(description = "URL-адрес HAR-файла с записью сетевой активности", example = "http://artifacts.example.com/run1/network.har")
    private String harFileUrl;
}
