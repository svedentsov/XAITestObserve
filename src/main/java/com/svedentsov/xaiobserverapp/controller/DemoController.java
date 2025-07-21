package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import com.svedentsov.xaiobserverapp.service.TestRunService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MVC-контроллер для создания демонстрационных тестовых запусков.
 * Используется для генерации случайных данных о тестовых событиях
 * с целью демонстрации функциональности приложения без реального запуска тестов.
 */
@Controller
@RequiredArgsConstructor
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private final TestRunService testRunService;

    private static final List<String> TEST_CLASSES = List.of(
            "com.example.tests.LoginTests",
            "com.example.tests.CartTests",
            "com.example.tests.CheckoutTests"
    );

    private static final List<String> EXCEPTION_TYPES = List.of(
            "org.openqa.selenium.NoSuchElementException: Элемент не найден",
            "org.openqa.selenium.TimeoutException: Время ожидания истекло",
            "org.openqa.selenium.StaleElementReferenceException: Ссылка на элемент устарела",
            "java.lang.AssertionError: Ожидалось 'true', но было 'false'"
    );

    /**
     * Обрабатывает POST-запросы для создания одной демонстрационной записи тестового запуска.
     * Генерирует случайное событие о завершении теста (успешное или неуспешное) и сохраняет его.
     * После успешного создания перенаправляет пользователя на главную страницу дашборда
     * с сообщением о результате.
     *
     * @param redirectAttributes Объект {@link RedirectAttributes} для добавления flash-атрибутов
     *                           (сообщений, которые будут доступны после перенаправления).
     * @return Строка перенаправления на корневой URL ("/").
     */
    @PostMapping("/demo/create")
    public String createDemoTestRun(RedirectAttributes redirectAttributes) {
        logger.info("Запрос на создание демонстрационной записи тестового запуска.");
        try {
            FailureEventDTO event = generateRandomEvent();
            testRunService.processAndSaveTestEvent(event);

            redirectAttributes.addFlashAttribute("message",
                    String.format("Демо-запись '%s' успешно создана со статусом '%s'!",
                            event.getTestMethod(), event.getStatus()));
            logger.info("Демонстрационная запись '{}' создана.", event.getTestMethod());

        } catch (Exception e) {
            logger.error("Ошибка при создании демонстрационной записи:", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/";
    }

    /**
     * Генерирует случайное событие о завершении теста ({@link FailureEventDTO})
     * для демонстрационных целей.
     * Случайным образом определяет, будет ли тест провален (60% шанс) или успешен,
     * а также генерирует метаданные для шагов выполнения, включая информацию о сбойном шаге
     * и типе исключения в случае провала.
     *
     * @return Сгенерированный объект {@link FailureEventDTO}.
     */
    private FailureEventDTO generateRandomEvent() {
        boolean isFailed = ThreadLocalRandom.current().nextDouble() < 0.6; // 60% шанс на FAILED
        FailureEventDTO event = new FailureEventDTO();

        event.setTestRunId(UUID.randomUUID().toString());
        event.setTestClass(TEST_CLASSES.get(ThreadLocalRandom.current().nextInt(TEST_CLASSES.size())));
        event.setTestMethod((isFailed ? "testFailedScenario_" : "testPassedScenario_") + System.currentTimeMillis() % 1000);
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus(isFailed ? "FAILED" : "PASSED");

        // Демо-конфигурация
        event.setAppVersion("1." + ThreadLocalRandom.current().nextInt(1, 5) + ".0");
        event.setEnvironment(ThreadLocalRandom.current().nextBoolean() ? "QA" : "STAGING");
        event.setTestSuite("Regression");

        // Генерация шагов
        AiDecisionMetadata step1 = new AiDecisionMetadata("Навигация на страницу входа", "url", "/login", 0.99, "SUCCESS");
        AiDecisionMetadata step2 = new AiDecisionMetadata("Ввод логина", "id", "username", 0.95, "SUCCESS");
        AiDecisionMetadata step3 = new AiDecisionMetadata("Ввод пароля", "name", "password", 0.97, "SUCCESS");
        AiDecisionMetadata step4 = new AiDecisionMetadata("Нажатие кнопки входа", "xpath", "//button[text()='Войти']", 0.98, "SUCCESS");

        if (isFailed) {
            AiDecisionMetadata failedStep = new AiDecisionMetadata("Проверка заголовка дашборда", "css", "h1.main-title", 0.75, "FAILURE");
            event.setFailedStep(failedStep);
            event.setExceptionType(EXCEPTION_TYPES.get(ThreadLocalRandom.current().nextInt(EXCEPTION_TYPES.size())));
            event.setStackTrace("Пример стектрейса...\n\tat " + event.getTestClass() + "." + event.getTestMethod() + "(DemoTest.java:42)\n\t...");
            event.setExecutionPath(Arrays.asList(step1, step2, step3, step4, failedStep));
        } else {
            AiDecisionMetadata finalStep = new AiDecisionMetadata("Проверка успешного входа", "css", "div.user-profile", 0.99, "SUCCESS");
            event.setExecutionPath(Arrays.asList(step1, step2, step3, step4, finalStep));
        }
        return event;
    }
}
