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
 * Контроллер для отображения веб-страниц с использованием Thymeleaf.
 * <p>
 * Отвечает за рендеринг главной страницы дашборда и отдельных фрагментов.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TestRunService testRunService;

    /**
     * Отображает главную страницу дашборда со списком всех тестовых запусков.
     *
     * @param model Модель для передачи данных в шаблон Thymeleaf.
     * @return Имя шаблона "dashboard".
     */
    @GetMapping("/")
    public String getDashboard(Model model) {
        log.debug("Request to display the main dashboard.");
        model.addAttribute("testRuns", testRunService.getAllTestRunsOrderedByTimestampDesc());
        return "dashboard";
    }

    /**
     * Отображает страницу с детальной информацией о конкретном тесте.
     * (Примечание: в текущей реализации не используется, вся логика на одной странице).
     *
     * @param id    ID тестового запуска.
     * @param model Модель для передачи данных.
     * @return Имя шаблона "test-detail".
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

    /**
     * Возвращает HTML-фрагмент со списком тестовых запусков для динамического обновления.
     *
     * @param model Модель для передачи данных.
     * @return Ссылку на фрагмент Thymeleaf "dashboard :: testListBody".
     */
    @GetMapping("/test-list-fragment")
    public String getTestRunListFragment(Model model) {
        log.debug("Request for test list fragment.");
        model.addAttribute("testRuns", testRunService.getAllTestRunsOrderedByTimestampDesc());
        return "dashboard :: testListBody";
    }
}
