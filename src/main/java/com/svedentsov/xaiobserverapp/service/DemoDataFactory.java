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
    private static final List<String> OS_VERSIONS = List.of("10", "11", "Ventura", "Ubuntu 22.04");
    private static final List<String> BROWSER_TYPES = List.of("Chrome", "Firefox", "Edge");
    private static final List<String> BROWSER_VERSIONS = List.of("120", "121", "122");
    private static final List<String> SCREEN_RESOLUTIONS = List.of("1920x1080", "1366x768", "1440x900");
    private static final List<String> DEVICE_TYPES = List.of("Desktop", "Tablet", "Mobile");
    private static final List<String> ENVIRONMENTS = List.of("QA", "STAGING", "DEV");
    private static final List<String> APP_VERSIONS = List.of("1.0.0", "1.1.0", "1.2.1", "2.0.0");
    private static final List<String> TEST_SUITES = List.of("Regression", "Smoke", "E2E", "Performance");
    private static final List<String> TEST_TAGS_GENERAL = List.of("critical", "P1", "UI", "backend");

    public FailureEventDTO generateRandomEvent() {
        boolean isFailed = ThreadLocalRandom.current().nextDouble() < 0.6; // 60% шанс на FAILED
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - ThreadLocalRandom.current().nextLong(5000, 60000); // 5-60 секунд назад
        long endTime = currentTime;
        long durationMillis = endTime - startTime;

        FailureEventDTO event = new FailureEventDTO();

        event.setTestRunId(UUID.randomUUID().toString());
        String testClass = TEST_CLASSES.get(ThreadLocalRandom.current().nextInt(TEST_CLASSES.size()));
        event.setTestClass(testClass);

        String testMethod;
        if (testClass.contains("Login")) {
            testMethod = TEST_METHODS_LOGIN.get(ThreadLocalRandom.current().nextInt(TEST_METHODS_LOGIN.size()));
        } else if (testClass.contains("Cart")) {
            testMethod = TEST_METHODS_CART.get(ThreadLocalRandom.current().nextInt(TEST_METHODS_CART.size()));
        } else if (testClass.contains("Checkout")) {
            testMethod = TEST_METHODS_CHECKOUT.get(ThreadLocalRandom.current().nextInt(TEST_METHODS_CHECKOUT.size()));
        } else if (testClass.contains("Search")) {
            testMethod = TEST_METHODS_SEARCH.get(ThreadLocalRandom.current().nextInt(TEST_METHODS_SEARCH.size()));
        } else {
            testMethod = "testGenericScenario_" + UUID.randomUUID().toString().substring(0, 4);
        }
        event.setTestMethod(testMethod);

        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setDurationMillis(durationMillis);

        event.setStatus(isFailed ? "FAILED" : "PASSED");

        // Демо-конфигурация
        event.setAppVersion(APP_VERSIONS.get(ThreadLocalRandom.current().nextInt(APP_VERSIONS.size())));
        String envName = ENVIRONMENTS.get(ThreadLocalRandom.current().nextInt(ENVIRONMENTS.size()));
        event.setTestSuite(TEST_SUITES.get(ThreadLocalRandom.current().nextInt(TEST_SUITES.size())));
        event.setTestTags(List.of(
                TEST_TAGS_GENERAL.get(ThreadLocalRandom.current().nextInt(TEST_TAGS_GENERAL.size())),
                "automated"));


        // Генерация EnvironmentDetailsDTO
        EnvironmentDetailsDTO envDetails = new EnvironmentDetailsDTO();
        envDetails.setName(envName);
        envDetails.setOsType(OS_TYPES.get(ThreadLocalRandom.current().nextInt(OS_TYPES.size())));
        envDetails.setOsVersion(OS_VERSIONS.get(ThreadLocalRandom.current().nextInt(OS_VERSIONS.size())));
        envDetails.setBrowserType(BROWSER_TYPES.get(ThreadLocalRandom.current().nextInt(BROWSER_TYPES.size())));
        envDetails.setBrowserVersion(BROWSER_VERSIONS.get(ThreadLocalRandom.current().nextInt(BROWSER_VERSIONS.size())));
        envDetails.setScreenResolution(SCREEN_RESOLUTIONS.get(ThreadLocalRandom.current().nextInt(SCREEN_RESOLUTIONS.size())));
        envDetails.setDeviceType(DEVICE_TYPES.get(ThreadLocalRandom.current().nextInt(DEVICE_TYPES.size())));
        envDetails.setDeviceName(envDetails.getDeviceType().equals("Mobile") ? "iPhone X" : null);
        envDetails.setDriverVersion("120.0.1");
        envDetails.setAppBaseUrl("https://demo.app.com/" + envName.toLowerCase());
        event.setEnvironmentDetails(envDetails);

        // Генерация TestArtifactsDTO
        TestArtifactsDTO artifacts = new TestArtifactsDTO();
        String artifactBaseUrl = "http://artifacts.example.com/" + event.getTestRunId();
        artifacts.setScreenshotUrls(List.of(
                artifactBaseUrl + "/screenshot_start.png",
                artifactBaseUrl + (isFailed ? "/screenshot_fail.png" : "/screenshot_end.png")
        ));
        artifacts.setVideoUrl(artifactBaseUrl + "/video.mp4");
        artifacts.setAppLogUrls(List.of(artifactBaseUrl + "/app.log"));
        if (ThreadLocalRandom.current().nextBoolean()) {
            artifacts.setBrowserConsoleLogUrl(artifactBaseUrl + "/console.log");
        }
        if (ThreadLocalRandom.current().nextBoolean()) {
            artifacts.setHarFileUrl(artifactBaseUrl + "/network.har");
        }
        event.setArtifacts(artifacts);

        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("buildNumber", "build-" + ThreadLocalRandom.current().nextInt(100, 500));
        customMetadata.put("jenkinsJobUrl", "http://jenkins.example.com/job/" + event.getTestClass());
        event.setCustomMetadata(customMetadata);

        List<AiDecisionMetadata> executionPath = new ArrayList<>();
        long currentStepTime = startTime;

        AiDecisionMetadata step1 = new AiDecisionMetadata();
        step1.setStepNumber(1);
        step1.setAction("Навигация на страницу " + (testClass.contains("Login") ? "входа" : "главную"));
        step1.setLocatorStrategy("url");
        step1.setLocatorValue((testClass.contains("Login") ? "/login" : "/"));
        step1.setConfidenceScore(0.99);
        step1.setResult("SUCCESS");
        step1.setStepStartTime(currentStepTime);
        currentStepTime += ThreadLocalRandom.current().nextLong(500, 2000); // 0.5-2 сек
        step1.setStepEndTime(currentStepTime);
        step1.setStepDurationMillis(step1.getStepEndTime() - step1.getStepStartTime());
        executionPath.add(step1);

        if (testClass.contains("Login")) {
            AiDecisionMetadata step2 = new AiDecisionMetadata();
            step2.setStepNumber(2);
            step2.setAction("Ввод логина");
            step2.setLocatorStrategy("id");
            step2.setLocatorValue("username");
            step2.setInteractedText("user" + ThreadLocalRandom.current().nextInt(1, 100));
            step2.setConfidenceScore(0.95);
            step2.setResult("SUCCESS");
            step2.setStepStartTime(currentStepTime);
            currentStepTime += ThreadLocalRandom.current().nextLong(200, 800);
            step2.setStepEndTime(currentStepTime);
            step2.setStepDurationMillis(step2.getStepEndTime() - step2.getStepStartTime());
            executionPath.add(step2);

            AiDecisionMetadata step3 = new AiDecisionMetadata();
            step3.setStepNumber(3);
            step3.setAction("Ввод пароля");
            step3.setLocatorStrategy("name");
            step3.setLocatorValue("password");
            step3.setInteractedText("pa$$w0rd");
            step3.setConfidenceScore(0.97);
            step3.setResult("SUCCESS");
            step3.setStepStartTime(currentStepTime);
            currentStepTime += ThreadLocalRandom.current().nextLong(200, 800);
            step3.setStepEndTime(currentStepTime);
            step3.setStepDurationMillis(step3.getStepEndTime() - step3.getStepStartTime());
            executionPath.add(step3);
        } else if (testClass.contains("Cart")) {
            AiDecisionMetadata step2 = new AiDecisionMetadata();
            step2.setStepNumber(2);
            step2.setAction("Добавление товара в корзину");
            step2.setLocatorStrategy("css");
            step2.setLocatorValue(".product-card button.add-to-cart");
            step2.setInteractedText("Product " + ThreadLocalRandom.current().nextInt(1, 10));
            step2.setConfidenceScore(0.96);
            step2.setResult("SUCCESS");
            step2.setStepStartTime(currentStepTime);
            currentStepTime += ThreadLocalRandom.current().nextLong(300, 1500);
            step2.setStepEndTime(currentStepTime);
            step2.setStepDurationMillis(step2.getStepEndTime() - step2.getStepStartTime());
            executionPath.add(step2);
        }

        if (isFailed) {
            String randomExceptionType = EXCEPTION_TYPES.get(ThreadLocalRandom.current().nextInt(EXCEPTION_TYPES.size()));
            String randomExceptionMessage = EXCEPTION_MESSAGES.get(ThreadLocalRandom.current().nextInt(EXCEPTION_MESSAGES.size()));

            AiDecisionMetadata failedStep = new AiDecisionMetadata();
            failedStep.setStepNumber(executionPath.size() + 1);
            failedStep.setAction("Проверка элемента после действия");
            failedStep.setLocatorStrategy("xpath");
            failedStep.setLocatorValue("//div[@id='dashboard-header']");
            failedStep.setConfidenceScore(ThreadLocalRandom.current().nextDouble(0.4, 0.8));
            failedStep.setResult("FAILURE");
            failedStep.setErrorMessage(randomExceptionMessage);
            failedStep.setStepStartTime(currentStepTime);
            currentStepTime = endTime;
            failedStep.setStepEndTime(currentStepTime);
            failedStep.setStepDurationMillis(failedStep.getStepEndTime() - failedStep.getStepStartTime());
            executionPath.add(failedStep);

            event.setFailedStep(failedStep);
            event.setExceptionType(randomExceptionType);
            event.setExceptionMessage(randomExceptionMessage);
            event.setStackTrace(generateStackTrace(event.getTestClass(), event.getTestMethod(), randomExceptionType));
            event.setExecutionPath(executionPath);
        } else {
            AiDecisionMetadata finalStep = new AiDecisionMetadata();
            finalStep.setStepNumber(executionPath.size() + 1);
            finalStep.setAction("Финальная проверка успешного состояния");
            finalStep.setLocatorStrategy("css");
            finalStep.setLocatorValue(".success-message");
            finalStep.setConfidenceScore(0.99);
            finalStep.setResult("SUCCESS");
            finalStep.setStepStartTime(currentStepTime);
            finalStep.setStepEndTime(endTime);
            finalStep.setStepDurationMillis(finalStep.getStepEndTime() - finalStep.getStepStartTime());
            executionPath.add(finalStep);
            event.setExecutionPath(executionPath);
        }
        return event;
    }

    private String generateStackTrace(String testClass, String testMethod, String exceptionType) {
        return String.format("%s: %s\n" +
                        "\tat %s.%s(TestClass.java:%d)\n" +
                        "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
                        "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)\n" +
                        "\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
                        "\tat java.base/java.lang.reflect.Method.invoke(Method.java:568)\n" +
                        "\tat org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)\n" +
                        "\tat org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)\n" +
                        "\t...",
                exceptionType, EXCEPTION_MESSAGES.get(ThreadLocalRandom.current().nextInt(EXCEPTION_MESSAGES.size())),
                testClass, testMethod, ThreadLocalRandom.current().nextInt(20, 100));
    }
}
