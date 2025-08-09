package com.svedentsov.xaiobserverapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Встраиваемый класс (Embeddable) для хранения ссылок на артефакты тестового запуска.
 * Ссылки на коллекции (например, скриншоты) хранятся в отдельных таблицах.
 */
@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddableTestArtifacts {

    /**
     * Список URL-адресов скриншотов, сделанных во время теста.
     */
    @ElementCollection
    @CollectionTable(name = "artifact_screenshots", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "url", length = 1024)
    private List<String> screenshotUrls;

    /**
     * URL-адрес видеозаписи выполнения теста.
     */
    @Column(length = 1024)
    private String videoUrl;

    /**
     * Список URL-адресов лог-файлов приложения.
     */
    @ElementCollection
    @CollectionTable(name = "artifact_app_logs", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "url", length = 1024)
    private List<String> appLogUrls;

    /**
     * URL-адрес лога консоли браузера.
     */
    @Column(length = 1024)
    private String browserConsoleLogUrl;

    /**
     * URL-адрес HAR-файла с записью сетевой активности.
     */
    @Column(length = 1024)
    private String harFileUrl;
}
