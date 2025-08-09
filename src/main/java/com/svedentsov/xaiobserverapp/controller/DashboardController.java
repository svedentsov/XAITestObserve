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
 * Контроллер для обработки UI-запросов и отображения HTML-страниц с помощью Thymeleaf.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TestRunService testRunService;

    /**
     * Отображает главную страницу дашборда.
     * Возвращает "каркас" страницы, который затем наполняется данными через API-запросы из JavaScript.
     *
     * @return имя Thymeleaf-шаблона "dashboard".
     */
    @GetMapping("/")
    public String getDashboard() {
        log.debug("Request to display the main dashboard skeleton.");
        return "dashboard";
    }

    /**
     * Отображает страницу с детальной информацией о конкретном тестовом запуске.
     * Этот эндпоинт может использоваться для прямых ссылок на детали теста,
     * хотя в основной логике дашборда детали подгружаются через API.
     *
     * @param id    Уникальный идентификатор тестового запуска.
     * @param model Модель для передачи данных в Thymeleaf-шаблон.
     * @return имя Thymeleaf-шаблона "test-detail".
     * @throws ResponseStatusException если тестовый запуск с указанным ID не найден.
     */
    @GetMapping("/test/{id}")
    public String getTestDetail(@PathVariable String id, Model model) {
        log.debug("Request for test run details with ID: {} for UI.", id);
        TestRun testRun = testRunService.getTestRunById(id)
                .orElseThrow(() -> {
                    log.warn("Test run with ID: {} not found.", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Test run not found");
                });
        model.addAttribute("testRun", testRun);
        return "test-detail";
    }
}
