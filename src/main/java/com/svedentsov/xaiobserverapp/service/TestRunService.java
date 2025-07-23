package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.model.TestConfiguration;
import com.svedentsov.xaiobserverapp.model.TestRun;
import com.svedentsov.xaiobserverapp.repository.TestConfigurationRepository;
import com.svedentsov.xaiobserverapp.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


/**
 * Сервис для обработки и управления жизненным циклом тестовых запусков.
 * Отвечает за сохранение событий тестов, их анализ, управление конфигурациями
 * и предоставление доступа к данным тестовых запусков.
 * Обновлен для обработки расширенных DTO.
 */
@Service
@RequiredArgsConstructor
public class TestRunService {
    private static final Logger logger = LoggerFactory.getLogger(TestRunService.class);
    private final TestRunRepository testRunRepository;
    private final TestConfigurationRepository testConfigurationRepository;
    private final RcaService rcaService;
    private final NotificationService notificationService;

    /**
     * Обрабатывает и сохраняет событие о завершении тестового запуска.
     * Включает валидацию DTO, преобразование в сущность {@link TestRun},
     * выполнение корневого анализа причин (RCA), управление конфигурацией теста,
     * сохранение в базу данных и отправку уведомлений в случае сбоя.
     *
     * @param event DTO, содержащий информацию о завершившемся тестовом событии.
     * @throws IllegalArgumentException Если переданный DTO недействителен или отсутствуют обязательные поля.
     */
    @Transactional
    public void processAndSaveTestEvent(FailureEventDTO event) {
        validateFailureEventDTO(event);
        TestRun testRun = convertToTestRun(event);
        // 1. Находим или создаем конфигурацию
        TestConfiguration config = findOrCreateConfiguration(event);
        testRun.setConfiguration(config);
        // 2. Первое сохранение TestRun для фиксации его ID в базе данных.
        testRunRepository.save(testRun);
        logger.info("Тестовый запуск с ID {} временно сохранен для получения ID.", testRun.getId());
        // 3. Выполняем RCA и связываем результаты.
        List<AnalysisResult> analysisResults = rcaService.analyzeTestRun(event);
        analysisResults.forEach(testRun::addAnalysisResult);
        // 4. Второе сохранение TestRun для персистентности результатов анализа.
        testRunRepository.save(testRun);
        logger.info("Тестовый запуск с ID {} окончательно сохранен вместе с результатами анализа.", testRun.getId());


        if (testRun.getStatus() == TestRun.TestStatus.FAILED) {
            notificationService.notifyAboutFailure(testRun);
        }
    }

    /**
     * Находит существующую тестовую конфигурацию или создает новую, если таковая не найдена.
     * Конфигурация определяется по комбинации версии приложения, среды, тестового набора,
     * а теперь и по деталям окружения.
     *
     * @param event DTO события, содержащий данные о конфигурации.
     * @return Найденная или созданная сущность {@link TestConfiguration}.
     */
    @Transactional
    private TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        String appVersion = StringUtils.hasText(event.getAppVersion()) ? event.getAppVersion() : "unknown";
        String testSuite = StringUtils.hasText(event.getTestSuite()) ? event.getTestSuite() : "default";

        String environmentName = "unknown";
        String environmentDetailsHash = "unknown_env";

        if (event.getEnvironmentDetails() != null) {
            environmentName = StringUtils.hasText(event.getEnvironmentDetails().getName()) ? event.getEnvironmentDetails().getName() : "unknown";
            // Убедитесь, что все поля, формирующие uniqueName, не null
            String osType = event.getEnvironmentDetails().getOsType() != null ? event.getEnvironmentDetails().getOsType() : "";
            String osVersion = event.getEnvironmentDetails().getOsVersion() != null ? event.getEnvironmentDetails().getOsVersion() : "";
            String browserType = event.getEnvironmentDetails().getBrowserType() != null ? event.getEnvironmentDetails().getBrowserType() : "";
            String browserVersion = event.getEnvironmentDetails().getBrowserVersion() != null ? event.getEnvironmentDetails().getBrowserVersion() : "";
            String screenResolution = event.getEnvironmentDetails().getScreenResolution() != null ? event.getEnvironmentDetails().getScreenResolution() : "";
            String deviceType = event.getEnvironmentDetails().getDeviceType() != null ? event.getEnvironmentDetails().getDeviceType() : "";
            String deviceName = event.getEnvironmentDetails().getDeviceName() != null ? event.getEnvironmentDetails().getDeviceName() : "";
            String driverVersion = event.getEnvironmentDetails().getDriverVersion() != null ? event.getEnvironmentDetails().getDriverVersion() : "";

            environmentDetailsHash = String.format("%s-%s-%s-%s-%s-%s-%s-%s-%s",
                    environmentName, osType, osVersion, browserType, browserVersion,
                    screenResolution, deviceType, deviceName, driverVersion
            ).toLowerCase().replaceAll("[^a-zA-Z0-9-]", "_"); // Замените небуквенно-цифровые символы на '_' или удалите
        }

        String uniqueName = String.format("%s-%s-%s-%s", appVersion, testSuite, environmentName, environmentDetailsHash);
        synchronized (this) { // Блокировка на уровне объекта сервиса
            Optional<TestConfiguration> existingConfig = testConfigurationRepository.findByUniqueName(uniqueName);
            if (existingConfig.isPresent()) {
                logger.debug("Найдена существующая конфигурация: {}", uniqueName);
                return existingConfig.get();
            } else {
                TestConfiguration newConfig = new TestConfiguration();
                newConfig.setAppVersion(appVersion);
                newConfig.setTestSuite(testSuite);
                newConfig.setEnvironment(environmentName);
                newConfig.setUniqueName(uniqueName);
                logger.info("Создана новая конфигурация: {}", uniqueName);
                return testConfigurationRepository.save(newConfig);
            }
        }
    }

    /**
     * Получает все тестовые запуски, отсортированные по временной метке в убывающем порядке.
     * Эта операция выполняется в режиме "только чтение" для оптимизации производительности.
     *
     * @return Список объектов {@link TestRun}, представляющих все тестовые запуски.
     */
    @Transactional(readOnly = true)
    public List<TestRun> getAllTestRunsOrderedByTimestampDesc() {
        return testRunRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Получает тестовый запуск по его уникальному идентификатору.
     * Эта операция выполняется в режиме "только чтение".
     *
     * @param id Уникальный идентификатор тестового запуска.
     * @return {@link Optional}, содержащий найденный тестовый запуск, или пустой {@link Optional}, если не найден.
     */
    @Transactional(readOnly = true)
    public Optional<TestRun> getTestRunById(String id) {
        return testRunRepository.findById(id);
    }

    /**
     * Выполняет валидацию обязательных полей в {@link FailureEventDTO}.
     * Если какое-либо обязательное поле отсутствует или недействительно, выбрасывает {@link IllegalArgumentException}.
     *
     * @param event DTO для валидации.
     * @throws IllegalArgumentException Если DTO не прошел валидацию.
     */
    private void validateFailureEventDTO(FailureEventDTO event) {
        if (event == null) throw new IllegalArgumentException("Событие (FailureEventDTO) не может быть null.");
        if (!StringUtils.hasText(event.getTestRunId())) throw new IllegalArgumentException("TestRunId обязателен.");
        if (!StringUtils.hasText(event.getTestClass())) throw new IllegalArgumentException("TestClass обязателен.");
        if (!StringUtils.hasText(event.getTestMethod())) throw new IllegalArgumentException("TestMethod обязателен.");
        if (!StringUtils.hasText(event.getStatus())) throw new IllegalArgumentException("Status обязателен.");
        if (event.getStartTime() <= 0) throw new IllegalArgumentException("StartTime должен быть положительным.");
    }

    /**
     * Преобразует объект {@link FailureEventDTO} в сущность {@link TestRun}.
     * Выполняет маппинг всех доступных полей, включая временные метки и детали.
     *
     * @param event DTO для преобразования.
     * @return Новый объект сущности {@link TestRun}.
     */
    private TestRun convertToTestRun(FailureEventDTO event) {
        TestRun testRun = new TestRun();
        testRun.setId(event.getTestRunId());
        testRun.setTestClass(event.getTestClass());
        testRun.setTestMethod(event.getTestMethod());

        testRun.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getStartTime()), ZoneId.systemDefault()));
        testRun.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEndTime()), ZoneId.systemDefault()));
        testRun.setDurationMillis(event.getDurationMillis());
        testRun.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEndTime()), ZoneId.systemDefault()));

        testRun.setStatus(TestRun.TestStatus.valueOf(event.getStatus().toUpperCase()));
        testRun.setExceptionType(event.getExceptionType());
        testRun.setExceptionMessage(event.getExceptionMessage());
        testRun.setStackTrace(event.getStackTrace());
        testRun.setFailedStep(event.getFailedStep());
        if (event.getExecutionPath() != null) {
            testRun.setExecutionPath(event.getExecutionPath());
        }
        testRun.setAppVersion(event.getAppVersion());
        if (event.getEnvironmentDetails() != null && StringUtils.hasText(event.getEnvironmentDetails().getName())) {
            testRun.setEnvironment(event.getEnvironmentDetails().getName());
        } else {
            testRun.setEnvironment("unknown");
        }

        testRun.setTestSuite(event.getTestSuite());
        if (event.getTestTags() != null) {
            testRun.setTestTags(new ArrayList<>(event.getTestTags()));
        }
        testRun.setEnvironmentDetails(event.getEnvironmentDetails());
        testRun.setArtifacts(event.getArtifacts());
        if (event.getCustomMetadata() != null) {
            testRun.setCustomMetadata(new HashMap<>(event.getCustomMetadata()));
        }

        return testRun;
    }
}
