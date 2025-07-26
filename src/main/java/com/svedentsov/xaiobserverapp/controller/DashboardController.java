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

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final TestRunService testRunService;

    @GetMapping("/")
    public String getDashboard(Model model) {
        log.debug("Запрос на отображение главного дашборда.");
        model.addAttribute("testRuns", testRunService.getAllTestRunsOrderedByTimestampDesc());
        return "dashboard";
    }

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
