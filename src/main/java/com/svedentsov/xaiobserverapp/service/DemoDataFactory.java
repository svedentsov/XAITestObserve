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
    private static final List<String> TEST_CLASSES = List.of(
            "com.example.tests.CartTests",
            "com.example.tests.CheckoutTests",
            "com.example.tests.LoginTests",
            "com.example.tests.ProfileTests",
            "com.example.tests.SearchTests");
    private static final List<String> TEST_METHODS_LOGIN = List.of(
            "testInvalidPassword",
            "testLoginWithEmptyFields",
            "testSuccessfulLogin",
            "testUserNotFound");
    private static final List<String> TEST_METHODS_CART = List.of(
            "testAddToCart",
            "testEmptyCart",
            "testRemoveFromCart",
            "testUpdateQuantity");
    private static final List<String> TEST_METHODS_CHECKOUT = List.of(
            "testGuestCheckout",
            "testRegisteredCheckout",
            "testPaymentFailure",
            "testShippingError");
    private static final List<String> TEST_METHODS_SEARCH = List.of(
            "testSearchValidProduct",
            "testSearchNoResults",
            "testSearchSpecialChars");
    private static final List<String> EXCEPTION_TYPES = List.of(
            "org.openqa.selenium.NoSuchElementException",
            "org.openqa.selenium.TimeoutException",
            "org.openqa.selenium.StaleElementReferenceException",
            "java.lang.AssertionError",
            "java.lang.NullPointerException",
            "org.openqa.selenium.WebDriverException");
    private static final List<String> EXCEPTION_MESSAGES = List.of(
            "Элемент 'submit button' не найден на странице.",
            "Элемент не доступен после 30 секунд ожидания.",
            "Элемент устарел: страница была обновлена.",
            "Ожидаемый заголовок 'Dashboard' не найден, фактический 'Login Page'.",
            "Попытка доступа к неинициализированному объекту.",
            "Ошибка при взаимодействии с драйвером браузера.");

    private static final List<String> OS_TYPES = List.of("Windows", "macOS", "Linux");
    private static final List<String> OS_VERSIONS = List.of("11", "12", "Ventura", "Ubuntu 22.04");
    private static final List<String> BROWSER_TYPES = List.of("Chrome", "Firefox", "Edge", "Safari");
    private static final List<String> BROWSER_VERSIONS = List.of("125", "126", "127");
    private static final List<String> SCREEN_RESOLUTIONS = List.of("1920x1080", "1366x768", "2560x1440");
    private static final List<String> DEVICE_TYPES = List.of("Desktop", "Tablet", "Mobile");
    private static final List<String> ENVIRONMENTS = List.of("QA", "STAGING", "DEV");
    private static final List<String> APP_VERSIONS = List.of("2.1.0", "2.1.1", "2.2.0-beta", "3.0.0");
    private static final List<String> TEST_SUITES = List.of("Regression", "Smoke", "E2E", "API");
    private static final List<String> PRIORITY_TAGS = List.of("P0", "P1", "P2");
    private static final List<String> FEATURE_TAGS = List.of("login", "cart", "checkout", "search", "profile", "api-auth");
    private static final List<String> JIRA_TICKETS = List.of("PROJ-123", "PROJ-456", "PROJ-789", "QA-101", "DEV-555");
    private static final List<String> GIT_BRANCHES = List.of("main", "develop", "feature/new-login-flow", "hotfix/urgent-bug-PROJ-123");
    private static final List<String> TRIGGERED_BY = List.of("Jenkins CI", "GitHub Actions", "Manual Run (user: admin)");

    /**
     * Генерирует случайное событие о завершении теста (DTO).
     *
     * @return {@link FailureEventDTO} с рандомизированными данными.
     */
    public FailureEventDTO generateRandomEvent() {
        // Основные данные
        String testRunId = UUID.randomUUID().toString();
        boolean isFailed = ThreadLocalRandom.current().nextDouble() < 0.6;
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - ThreadLocalRandom.current().nextLong(5000, 60000);
        long endTime = currentTime;
        long durationMillis = endTime - startTime;
        String status = isFailed ? "FAILED" : "PASSED";

        // Данные теста
        String testClass = getRandomElement(TEST_CLASSES);
        String testMethod;
        if (testClass.contains("Login")) {
            testMethod = getRandomElement(TEST_METHODS_LOGIN);
        } else if (testClass.contains("Cart")) {
            testMethod = getRandomElement(TEST_METHODS_CART);
        } else if (testClass.contains("Checkout")) {
            testMethod = getRandomElement(TEST_METHODS_CHECKOUT);
        } else {
            testMethod = getRandomElement(TEST_METHODS_SEARCH);
        }

        // Конфигурация
        String appVersion = getRandomElement(APP_VERSIONS);
        String envName = getRandomElement(ENVIRONMENTS);
        String testSuite = getRandomElement(TEST_SUITES);
        Set<String> tagsSet = new HashSet<>();
        tagsSet.add(getRandomElement(PRIORITY_TAGS));
        tagsSet.add(getRandomElement(FEATURE_TAGS));
        tagsSet.add(isFailed ? "flaky" : "stable");
        tagsSet.add("automated");
        List<String> testTags = new ArrayList<>(tagsSet);

        // Environment Details
        EnvironmentDetailsDTO envDetails = new EnvironmentDetailsDTO(
                envName, getRandomElement(OS_TYPES), getRandomElement(OS_VERSIONS),
                getRandomElement(BROWSER_TYPES), getRandomElement(BROWSER_VERSIONS),
                getRandomElement(SCREEN_RESOLUTIONS), getRandomElement(DEVICE_TYPES),
                "Desktop".equals(getRandomElement(DEVICE_TYPES)) ? "Desktop PC" : "iPhone 15 Pro",
                "126.0.6478.127", "https://" + envName.toLowerCase() + ".myapp.com"
        );

        // Artifacts
        String artifactBaseUrl = "http://artifacts.example.com/" + testRunId;
        TestArtifactsDTO artifacts = new TestArtifactsDTO(
                List.of(artifactBaseUrl + "/screenshot_start.png", artifactBaseUrl + (isFailed ? "/screenshot_fail.png" : "/screenshot_end.png")),
                artifactBaseUrl + "/video.mp4", List.of(artifactBaseUrl + "/app.log"),
                ThreadLocalRandom.current().nextBoolean() ? artifactBaseUrl + "/console.log" : null,
                ThreadLocalRandom.current().nextBoolean() ? artifactBaseUrl + "/network.har" : null
        );

        // Metadata
        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("buildNumber", "build-" + ThreadLocalRandom.current().nextInt(1000, 5000));
        customMetadata.put("jenkinsJobUrl", "http://jenkins.example.com/job/" + testSuite + "/" + customMetadata.get("buildNumber"));
        customMetadata.put("jiraTicket", getRandomElement(JIRA_TICKETS));
        customMetadata.put("gitBranch", getRandomElement(GIT_BRANCHES));

        // Execution Path & Failure Details
        List<AiDecisionMetadata> executionPath = new ArrayList<>();
        long currentStepTime = startTime;
        executionPath.add(createStep(1, "Навигация на страницу", "url", "/login", "SUCCESS", null, null, 0.99, currentStepTime, 1500));
        currentStepTime += 1500;
        executionPath.add(createStep(2, "Ввод логина", "id", "username", "SUCCESS", "user123", null, 0.98, currentStepTime, 500));

        AiDecisionMetadata failedStep = null;
        String exceptionType = null;
        String exceptionMessage = null;
        String stackTrace = null;

        if (isFailed) {
            exceptionType = getRandomElement(EXCEPTION_TYPES);
            exceptionMessage = getRandomElement(EXCEPTION_MESSAGES);
            stackTrace = generateStackTrace(testClass, testMethod, exceptionType);
            failedStep = createStep(3, "Клик по кнопке Войти", "css", ".login-btn", "FAILURE", null, exceptionMessage, 0.7, currentStepTime, 5000);
            executionPath.add(failedStep);
        } else {
            executionPath.add(createStep(3, "Клик по кнопке Войти", "css", ".login-btn", "SUCCESS", null, null, 0.99, currentStepTime, 800));
        }

        return new FailureEventDTO(
                testRunId, testClass, testMethod, startTime, endTime, durationMillis,
                status, exceptionType, exceptionMessage, stackTrace, failedStep,
                executionPath, appVersion, envDetails, testSuite, testTags, artifacts, customMetadata
        );
    }

    private AiDecisionMetadata createStep(int number, String action, String locatorStrategy, String locatorValue,
                                          String result, String interactedText, String errorMessage, double confidence,
                                          long startTime, long duration) {
        AiDecisionMetadata step = new AiDecisionMetadata();
        step.setStepNumber(number);
        step.setAction(action);
        step.setLocatorStrategy(locatorStrategy);
        step.setLocatorValue(locatorValue);
        step.setResult(result);
        step.setInteractedText(interactedText);
        step.setErrorMessage(errorMessage);
        step.setConfidenceScore(confidence);
        step.setStepStartTime(startTime);
        step.setStepEndTime(startTime + duration);
        step.setStepDurationMillis(duration);
        step.setAdditionalStepData("{\"element_visible\": true}");
        return step;
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private String generateStackTrace(String testClass, String testMethod, String exceptionType) {
        return String.format("%s: %s\n\tat %s.%s(TestClass.java:42)", exceptionType, getRandomElement(EXCEPTION_MESSAGES), testClass, testMethod);
    }
}
