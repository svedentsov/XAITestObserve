package com.svedentsov.xaiobserverapp.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO для хранения ссылок на артефакты, сгенерированные во время выполнения теста.
 * Эти ссылки могут указывать на файлы в S3, сетевой ресурс или другую систему хранения.
 */
@Data
public class TestArtifactsDTO {
    /**
     * Список URL-адресов скриншотов, сделанных в момент сбоя или на ключевых шагах.
     */
    private List<String> screenshotUrls;
    /**
     * URL-адрес видеозаписи выполнения теста.
     */
    private String videoUrl;
    /**
     * Список URL-адресов файлов логов приложения или клиента (например, браузерные логи).
     */
    private List<String> appLogUrls;
    /**
     * URL-адрес файла со всеми консольными логами браузера.
     */
    private String browserConsoleLogUrl;
    /**
     * URL-адрес HAR-файла (HTTP Archive), содержащего сетевую активность.
     */
    private String harFileUrl;
}
