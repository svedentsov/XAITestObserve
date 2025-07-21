package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
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
import java.util.List;
import java.util.Optional;

/**
 * Сервис для обработки и управления жизненным циклом тестовых запусков.
 * Отвечает за сохранение событий тестов, их анализ, управление конфигурациями
 * и предоставление доступа к данным тестовых запусков.
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
        // Выполняем автоматический анализ причин сбоя
        testRun.setAnalysisResults(rcaService.analyzeTestRun(event));

        // Получаем или создаем конфигурацию, связанную с этим тестовым запуском
        TestConfiguration config = findOrCreateConfiguration(event);
        testRun.setConfiguration(config);

        testRunRepository.save(testRun);
        logger.info("Тестовый запуск с ID {} успешно сохранен.", testRun.getId());

        // Отправляем уведомление, если тест упал
        if (testRun.getStatus() == TestRun.TestStatus.FAILED) {
            notificationService.notifyAboutFailure(testRun);
        }
    }

    /**
     * Находит существующую тестовую конфигурацию или создает новую, если таковая не найдена.
     * Конфигурация определяется по комбинации версии приложения, среды и тестового набора.
     *
     * @param event DTO события, содержащий данные о конфигурации.
     * @return Найденная или созданная сущность {@link TestConfiguration}.
     */
    private TestConfiguration findOrCreateConfiguration(FailureEventDTO event) {
        String appVersion = StringUtils.hasText(event.getAppVersion()) ? event.getAppVersion() : "unknown";
        String environment = StringUtils.hasText(event.getEnvironment()) ? event.getEnvironment() : "unknown";
        String testSuite = StringUtils.hasText(event.getTestSuite()) ? event.getTestSuite() : "default";

        String uniqueName = String.format("%s-%s-%s", appVersion, environment, testSuite).toLowerCase();

        return testConfigurationRepository.findByUniqueName(uniqueName)
                .orElseGet(() -> {
                    TestConfiguration newConfig = new TestConfiguration();
                    newConfig.setAppVersion(appVersion);
                    newConfig.setEnvironment(environment);
                    newConfig.setTestSuite(testSuite);
                    logger.info("Создана новая конфигурация: {}", uniqueName);
                    return testConfigurationRepository.save(newConfig);
                });
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
        if (event.getTimestamp() <= 0) throw new IllegalArgumentException("Timestamp должен быть положительным.");
    }

    /**
     * Преобразует объект {@link FailureEventDTO} в сущность {@link TestRun}.
     * Выполняет маппинг полей, включая преобразование Unix-времени в {@link LocalDateTime}.
     *
     * @param event DTO для преобразования.
     * @return Новый объект сущности {@link TestRun}.
     */
    private TestRun convertToTestRun(FailureEventDTO event) {
        TestRun testRun = new TestRun();
        testRun.setId(event.getTestRunId());
        testRun.setTestClass(event.getTestClass());
        testRun.setTestMethod(event.getTestMethod());
        testRun.setTimestamp(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getTimestamp()),
                ZoneId.systemDefault()
        ));
        testRun.setStatus(TestRun.TestStatus.valueOf(event.getStatus().toUpperCase()));
        testRun.setExceptionType(event.getExceptionType());
        testRun.setStackTrace(event.getStackTrace());
        testRun.setFailedStep(event.getFailedStep());
        if (event.getExecutionPath() != null) {
            testRun.setExecutionPath(event.getExecutionPath());
        }
        return testRun;
    }
}
