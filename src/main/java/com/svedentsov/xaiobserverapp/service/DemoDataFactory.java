package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.EnvironmentDetailsDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestArtifactsDTO;
import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис-фабрика для создания демонстрационных данных.
 * Генерирует случайные события о завершении тестов для наполнения
 * системы данными при демонстрации или тестировании.
 */
@Service
public class DemoDataFactory {

    private record ExecutionDetails(List<AiDecisionMetadata> path, AiDecisionMetadata failedStep) {}

    private static final Map<String, List<String>> TEST_METHODS_BY_CLASS = Map.of(
            "com.example.tests.CartTests", List.of("testAddToCart", "testEmptyCart", "testRemoveFromCart", "testUpdateQuantity"),
            "com.example.tests.CheckoutTests", List.of("testGuestCheckout", "testRegisteredCheckout", "testPaymentFailure", "testShippingError"),
            "com.example.tests.LoginTests", List.of("testInvalidPassword", "testLoginWithEmptyFields", "testSuccessfulLogin", "testUserNotFound"),
            "com.example.tests.ProfileTests", List.of("testUpdateProfile", "testChangeAvatar", "testIncorrectPasswordChange"),
            "com.example.tests.SearchTests", List.of("testSearchValidProduct", "testSearchNoResults", "testSearchSpecialChars")
    );
    private static final List<String> ALL_TEST_CLASSES = new ArrayList<>(TEST_METHODS_BY_CLASS.keySet());
    private static final List<String> EXCEPTION_TYPES = List.of("org.openqa.selenium.NoSuchElementException", "org.openqa.selenium.TimeoutException", "org.openqa.selenium.StaleElementReferenceException", "java.lang.AssertionError", "java.lang.NullPointerException", "org.openqa.selenium.WebDriverException");
    private static final List<String> EXCEPTION_MESSAGES = List.of("Элемент 'submit button' не найден на странице.", "Элемент не доступен после 30 секунд ожидания.", "Элемент устарел: страница была обновлена.", "Ожидаемый заголовок 'Dashboard' не найден, фактический 'Login Page'.", "Попытка доступа к неинициализированному объекту.", "Ошибка при взаимодействии с драйвером браузера.");

    private static final List<String> OS_TYPES = List.of("Windows", "macOS", "Linux");
    private static final Map<String, List<String>> OS_VERSIONS = Map.of("Windows", List.of("10", "11"), "macOS", List.of("Sonoma", "Ventura"), "Linux", List.of("Ubuntu 22.04"));
    private static final List<String> BROWSER_TYPES = List.of("Chrome", "Firefox", "Edge", "Safari");
    private static final Map<String, List<String>> BROWSER_VERSIONS = Map.of("Chrome", List.of("126.0.6478.127", "125.0.6422.113"), "Firefox", List.of("127.0.1", "126.0"), "Edge", List.of("126.0.2592.87"), "Safari", List.of("17.5"));
    private static final List<String> RESOLUTIONS = List.of("1920x1080", "2560x1440", "1366x768", "390x844");
    private static final List<String> DEVICE_TYPES = List.of("Desktop", "Mobile");
    private static final List<String> DEVICE_NAMES = List.of("iPhone 15 Pro", "Samsung Galaxy S24", "Google Pixel 8");
    private static final List<String> ENVIRONMENTS = List.of("QA", "STAGING", "DEV-01");
    private static final List<String> APP_VERSIONS = List.of("2.1.0", "2.1.1", "2.2.0-beta", "3.0.0");
    private static final List<String> TEST_SUITES = List.of("Regression", "Smoke", "E2E", "API");
    private static final List<String> TAG_POOL = List.of("critical-path", "new-feature", "legacy", "performance", "security", "ui");

    /**
     * Генерирует полностью заполненное случайное событие о завершении теста.
     * @return DTO {@link FailureEventDTO} с реалистичными данными.
     */
    public FailureEventDTO generateRandomEvent() {
        var random = ThreadLocalRandom.current();
        String testRunId = UUID.randomUUID().toString();
        boolean isFailed = random.nextDouble() < 0.6; // 60% шанс падения
        String status = isFailed ? "FAILED" : (random.nextDouble() < 0.1 ? "SKIPPED" : "PASSED");

        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - random.nextLong(5000, 120000);
        long endTime = currentTime;
        long durationMillis = endTime - startTime;

        String testClass = getRandomElement(ALL_TEST_CLASSES);
        String testMethod = getRandomElement(TEST_METHODS_BY_CLASS.get(testClass));
        String appVersion = getRandomElement(APP_VERSIONS);
        String envName = getRandomElement(ENVIRONMENTS);
        String testSuite = getRandomElement(TEST_SUITES);

        Set<String> tagsSet = new HashSet<>();
        tagsSet.add(isFailed ? "flaky" : "stable");
        tagsSet.add(getRandomElement(TAG_POOL));
        tagsSet.add(testSuite.toLowerCase());
        List<String> testTags = new ArrayList<>(tagsSet);

        String osType = getRandomElement(OS_TYPES);
        String osVersion = getRandomElement(OS_VERSIONS.get(osType));
        String browserType = getRandomElement(BROWSER_TYPES);
        String browserVersion = getRandomElement(BROWSER_VERSIONS.get(browserType));
        String deviceType = getRandomElement(DEVICE_TYPES);
        String deviceName = "Mobile".equals(deviceType) ? getRandomElement(DEVICE_NAMES) : null;
        
        var envDetails = new EnvironmentDetailsDTO(
            envName, osType, osVersion, browserType, browserVersion,
            getRandomElement(RESOLUTIONS), deviceType, deviceName, browserVersion,
            "https://" + envName.toLowerCase() + ".myapp.com"
        );

        String artifactBaseUrl = "http://artifacts.example.com/" + testRunId;
        var artifacts = new TestArtifactsDTO(
            isFailed ? List.of(artifactBaseUrl + "/screenshot_fail.png") : Collections.emptyList(),
            artifactBaseUrl + "/video.mp4", List.of(artifactBaseUrl + "/app.log"),
            artifactBaseUrl + "/console.log", isFailed ? artifactBaseUrl + "/network.har" : null
        );

        Map<String, String> customMetadata = Map.of(
            "jiraTicket", "PROJ-" + random.nextInt(100, 1000),
            "buildNumber", "build-" + random.nextInt(500, 600)
        );

        String exceptionType = null;
        String exceptionMessage = null;
        String stackTrace = null;

        if ("FAILED".equals(status)) {
            int exceptionIndex = random.nextInt(EXCEPTION_TYPES.size());
            exceptionType = EXCEPTION_TYPES.get(exceptionIndex);
            exceptionMessage = EXCEPTION_MESSAGES.get(exceptionIndex);
            stackTrace = generateStackTrace(testClass, testMethod, exceptionType);
        }

        ExecutionDetails executionDetails = generateExecutionPath(status, exceptionMessage);

        return new FailureEventDTO(
                testRunId, testClass, testMethod, startTime, endTime, durationMillis,
                status, exceptionType, exceptionMessage, stackTrace, executionDetails.failedStep(),
                executionDetails.path(), appVersion, envDetails, testSuite, testTags, artifacts, customMetadata
        );
    }

    /**
     * Генерирует реалистичный путь выполнения теста из нескольких шагов.
     * @param finalStatus Финальный статус теста ("PASSED", "FAILED", "SKIPPED").
     * @param errorMessage Сообщение об ошибке для последнего шага, если тест упал.
     * @return Объект {@link ExecutionDetails}, содержащий полный путь и шаг сбоя.
     */
    private ExecutionDetails generateExecutionPath(String finalStatus, String errorMessage) {
        List<AiDecisionMetadata> path = new ArrayList<>();
        AiDecisionMetadata failedStep = null;

        var stepActions = List.of(
            new String[]{"Переход на главную страницу", "body"},
            new String[]{"Ввод 'тестовый запрос' в поле 'Поиск'", "input#search"},
            new String[]{"Клик по кнопке 'Найти'", "button.search-submit"},
            new String[]{"Ожидание появления результатов", "div.results-container"},
            new String[]{"Клик по первому товару в списке", "div.product-card:first-child"},
            new String[]{"Проверка заголовка товара", "h1.product-title"},
            new String[]{"Добавление товара в корзину", "button.add-to-cart"}
        );

        int totalSteps = ThreadLocalRandom.current().nextInt(3, stepActions.size() + 1);
        long stepStartTime = System.currentTimeMillis() - (totalSteps * 3000L);

        for (int i = 0; i < totalSteps; i++) {
            boolean isLastStep = (i == totalSteps - 1);
            String result = "SUCCESS";
            String stepErrorMessage = null;
            long stepDuration = ThreadLocalRandom.current().nextLong(500, 3000);
            
            if (isLastStep && "FAILED".equals(finalStatus)) {
                result = "FAILURE";
                stepErrorMessage = errorMessage;
                stepDuration = 30000L; // Имитация таймаута
            } else if (isLastStep && "SKIPPED".equals(finalStatus)) {
                result = "SKIPPED";
                stepErrorMessage = "Тест был пропущен по условию.";
            }

            AiDecisionMetadata step = createStep(i + 1, stepActions.get(i)[0], "css", stepActions.get(i)[1], result, stepErrorMessage, 0.95, stepStartTime, stepDuration);
            path.add(step);
            if ("FAILURE".equals(result)) {
                failedStep = step;
            }
            stepStartTime += stepDuration + ThreadLocalRandom.current().nextLong(100, 500);
        }
        return new ExecutionDetails(path, failedStep);
    }

    private AiDecisionMetadata createStep(int number, String action, String locatorStrategy, String locatorValue, String result, String errorMessage, double confidence, long startTime, long duration) {
        AiDecisionMetadata step = new AiDecisionMetadata();
        step.setStepNumber(number);
        step.setAction(action);
        step.setLocatorStrategy(locatorStrategy);
        step.setLocatorValue(locatorValue);
        step.setResult(result);
        step.setErrorMessage(errorMessage);
        step.setConfidenceScore(confidence);
        step.setStepStartTime(startTime);
        step.setStepEndTime(startTime + duration);
        step.setStepDurationMillis(duration);
        return step;
    }

    private <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private String generateStackTrace(String testClass, String testMethod, String exceptionType) {
        return String.format("%s: %s\n\tat %s.%s(TestClass.java:42)\n\tat org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:73)\n\tat org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:38)\n\tat org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)", exceptionType, getRandomElement(EXCEPTION_MESSAGES), testClass, testMethod);
    }
}
