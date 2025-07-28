package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.EnvironmentDetailsDTO;
import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.dto.TestArtifactsDTO;
import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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

    public FailureEventDTO generateRandomEvent() {
        boolean isFailed = ThreadLocalRandom.current().nextDouble() < 0.6; // 60% шанс на FAILED
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - ThreadLocalRandom.current().nextLong(5000, 60000);
        long endTime = currentTime;
        long durationMillis = endTime - startTime;

        FailureEventDTO event = new FailureEventDTO();
        event.setTestRunId(UUID.randomUUID().toString());
        String testClass = getRandomElement(TEST_CLASSES);
        event.setTestClass(testClass);

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
        event.setTestMethod(testMethod);

        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setDurationMillis(durationMillis);
        event.setStatus(isFailed ? "FAILED" : "PASSED");

        // Демо-конфигурация
        event.setAppVersion(getRandomElement(APP_VERSIONS));
        String envName = getRandomElement(ENVIRONMENTS);
        event.setTestSuite(getRandomElement(TEST_SUITES));

        Set<String> tags = new HashSet<>();
        tags.add(getRandomElement(PRIORITY_TAGS));
        tags.add(getRandomElement(FEATURE_TAGS));
        tags.add(isFailed ? "flaky" : "stable");
        tags.add("automated");
        event.setTestTags(new ArrayList<>(tags));

        EnvironmentDetailsDTO envDetails = new EnvironmentDetailsDTO();
        envDetails.setName(envName);
        envDetails.setOsType(getRandomElement(OS_TYPES));
        envDetails.setOsVersion(getRandomElement(OS_VERSIONS));
        envDetails.setBrowserType(getRandomElement(BROWSER_TYPES));
        envDetails.setBrowserVersion(getRandomElement(BROWSER_VERSIONS));
        envDetails.setScreenResolution(getRandomElement(SCREEN_RESOLUTIONS));
        envDetails.setDeviceType(getRandomElement(DEVICE_TYPES));
        envDetails.setDeviceName(envDetails.getDeviceType().equals("Mobile") ? "iPhone 15 Pro" : "Desktop PC");
        envDetails.setDriverVersion("126.0.6478.127");
        envDetails.setAppBaseUrl("https://" + envName.toLowerCase() + ".myapp.com");
        event.setEnvironmentDetails(envDetails);

        TestArtifactsDTO artifacts = new TestArtifactsDTO();
        String artifactBaseUrl = "http://artifacts.example.com/" + event.getTestRunId();
        artifacts.setScreenshotUrls(List.of(
                artifactBaseUrl + "/screenshot_start.png",
                artifactBaseUrl + (isFailed ? "/screenshot_fail.png" : "/screenshot_end.png")
        ));
        artifacts.setVideoUrl(artifactBaseUrl + "/video.mp4");
        artifacts.setAppLogUrls(List.of(artifactBaseUrl + "/app.log", artifactBaseUrl + "/backend.log"));
        if (ThreadLocalRandom.current().nextBoolean()) {
            artifacts.setBrowserConsoleLogUrl(artifactBaseUrl + "/console.log");
        }
        if (ThreadLocalRandom.current().nextBoolean()) {
            artifacts.setHarFileUrl(artifactBaseUrl + "/network.har");
        }
        event.setArtifacts(artifacts);

        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("buildNumber", "build-" + ThreadLocalRandom.current().nextInt(1000, 5000));
        customMetadata.put("jenkinsJobUrl", "http://jenkins.example.com/job/" + event.getTestSuite() + "/" + customMetadata.get("buildNumber"));
        customMetadata.put("jiraTicket", getRandomElement(JIRA_TICKETS));
        customMetadata.put("gitBranch", getRandomElement(GIT_BRANCHES));
        customMetadata.put("commitHash", UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        customMetadata.put("triggeredBy", getRandomElement(TRIGGERED_BY));
        event.setCustomMetadata(customMetadata);

        List<AiDecisionMetadata> executionPath = new ArrayList<>();
        long currentStepTime = startTime;

        // Step 1: Navigate
        executionPath.add(createStep(1, "Навигация на страницу " + (testClass.contains("Login") ? "входа" : "главную"),
                "url", (testClass.contains("Login") ? "/login" : "/"), "SUCCESS", null,
                null, 0.99, currentStepTime, ThreadLocalRandom.current().nextLong(500, 2000),
                "{\"page_load_strategy\": \"normal\"}"));
        currentStepTime += executionPath.get(0).getStepDurationMillis();

        if (testClass.contains("Login")) {
            executionPath.add(createStep(2, "Ввод логина", "id", "username", "SUCCESS", "user123", null, 0.98,
                    currentStepTime, ThreadLocalRandom.current().nextLong(200, 800), "{\"element_visible\": true}"));
            currentStepTime += executionPath.get(1).getStepDurationMillis();

            executionPath.add(createStep(3, "Ввод пароля", "name", "password", "SUCCESS", "pa$$w0rd", null, 0.97,
                    currentStepTime, ThreadLocalRandom.current().nextLong(200, 800), "{\"element_visible\": true, \"is_masked\": true}"));
            currentStepTime += executionPath.get(2).getStepDurationMillis();
        } else {
            executionPath.add(createStep(2, "Клик по кнопке 'Добавить в корзину'", "css", ".add-to-cart", "SUCCESS", null, null, 0.96,
                    currentStepTime, ThreadLocalRandom.current().nextLong(300, 1500), "{\"element_visible\": true}"));
            currentStepTime += executionPath.get(1).getStepDurationMillis();
        }

        if (isFailed) {
            String randomExceptionType = getRandomElement(EXCEPTION_TYPES);
            String randomExceptionMessage = getRandomElement(EXCEPTION_MESSAGES);

            AiDecisionMetadata failedStep = createStep(executionPath.size() + 1, "Проверка заголовка страницы", "xpath",
                    "//h1[contains(text(), 'Dashboard')]", "FAILURE", null, randomExceptionMessage,
                    ThreadLocalRandom.current().nextDouble(0.4, 0.8), currentStepTime,
                    endTime - currentStepTime, "{\"expected_text\": \"Dashboard\", \"actual_text\": \"Login Page\"}");

            executionPath.add(failedStep);
            event.setFailedStep(failedStep);
            event.setExceptionType(randomExceptionType);
            event.setExceptionMessage(randomExceptionMessage);
            event.setStackTrace(generateStackTrace(event.getTestClass(), event.getTestMethod(), randomExceptionType));
        } else {
            executionPath.add(createStep(executionPath.size() + 1, "Проверка сообщения об успехе", "css", ".success-toast", "SUCCESS",
                    null, null, 0.99, currentStepTime, endTime - currentStepTime,
                    "{\"toast_message\": \"Операция выполнена успешно\"}"));
        }
        event.setExecutionPath(executionPath);
        return event;
    }

    private AiDecisionMetadata createStep(int number, String action, String locatorStrategy, String locatorValue,
                                          String result, String interactedText, String errorMessage, double confidence,
                                          long startTime, long duration, String additionalData) {
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
        step.setAdditionalStepData(additionalData);
        return step;
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private String generateStackTrace(String testClass, String testMethod, String exceptionType) {
        return String.format("%s: %s\n" +
                        "\tat %s.%s(TestClass.java:%d)\n" +
                        "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
                        "\tat org.openqa.selenium.remote.RemoteWebDriver.findElement(RemoteWebDriver.java:355)\n" +
                        "\tat com.example.pages.BasePage.clickElement(BasePage.java:42)\n" +
                        "\tat com.example.steps.LoginSteps.verifyDashboardHeader(LoginSteps.java:35)\n" +
                        "\tat %s.%s(TestClass.java:%d)\n" +
                        "\t...",
                exceptionType, getRandomElement(EXCEPTION_MESSAGES),
                testClass, testMethod, ThreadLocalRandom.current().nextInt(20, 50),
                testClass, testMethod, ThreadLocalRandom.current().nextInt(51, 100));
    }
}
