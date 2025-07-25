package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.service.TestRunService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * MVC-контроллер для отображения страниц пользовательского интерфейса дашборда.
 * Отвечает за рендеринг HTML-страниц, отображающих информацию о тестовых запусках.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TestRunService testRunService;

    /**
     * Обрабатывает GET-запросы к корневому URL ("/") для отображения главной страницы дашборда.
     * Загружает все тестовые запуски, отсортированные по времени создания в убывающем порядке,
     * и передает их в модель для отображения.
     *
     * @param model Объект {@link Model} для передачи данных в представление.
     * @return Название HTML-шаблона для главной страницы дашборда.
     */
    @GetMapping("/")
    public String getDashboard(Model model) {
        log.debug("Запрос на отображение главного дашборда.");
        model.addAttribute("testRuns", testRunService.getAllTestRunsOrderedByTimestampDesc());
        return "dashboard";
    }

    /**
     * Обрабатывает GET-запросы для отображения страницы с деталями конкретного тестового запуска.
     * Если тестовый запуск с указанным ID не найден, возвращает статус 404 (Not Found).
     *
     * @param id    Уникальный идентификатор тестового запуска.
     * @param model Объект {@link Model} для передачи данных в представление.
     * @return Название HTML-шаблона для страницы деталей тестового запуска.
     * @throws ResponseStatusException Если тестовый запуск с заданным {@code id} не найден.
     */
    @GetMapping("/test/{id}")
    public String getTestDetail(@PathVariable String id, Model model) {
        log.debug("Запрос деталей тестового запуска с ID: {} для UI.", id);
        TestRun testRun = testRunService.getTestRunById(id)
                .orElseThrow(() -> {
                    log.warn("Тестовый запуск с ID: {} не найден.", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Тестовый запуск не найден");
                });
        model.addAttribute("testRun", testRun);
        return "test-detail";
    }
}
