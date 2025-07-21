package com.svedentsov.xaiobserverapp.dto;

import com.svedentsov.xaiobserverapp.model.AiDecisionMetadata;
import com.svedentsov.xaiobserverapp.model.TestRun;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Объект передачи данных (DTO) для детальной информации о тестовом запуске.
 * Содержит все подробности о конкретном выполнении теста, включая
 * результаты анализа и конфигурацию.
 */
@Data
public class TestRunDetailDTO {
    /**
     * Уникальный идентификатор тестового запуска.
     */
    private String id;
    /**
     * Полное имя класса, в котором выполнялся тест.
     */
    private String testClass;
    /**
     * Имя тестового метода.
     */
    private String testMethod;
    /**
     * Временная метка завершения теста.
     */
    private LocalDateTime timestamp;
    /**
     * Статус завершения теста (например, "PASSED", "FAILED", "SKIPPED").
     */
    private String status;
    /**
     * Тип исключения, вызвавшего сбой теста (если применимо).
     */
    private String exceptionType;
    /**
     * Полный стек-трейс исключения (если применимо).
     */
    private String stackTrace;
    /**
     * Метаданные о шаге, на котором произошел сбой, включая действие, локатор и уверенность AI.
     */
    private AiDecisionMetadata failedStep;
    /**
     * Список метаданных, описывающих последовательность шагов выполнения теста.
     */
    private List<AiDecisionMetadata> executionPath;
    /**
     * Список результатов анализа, связанных с этим тестовым запуском.
     */
    private List<AnalysisResultDTO> analysisResults;
    /**
     * Конфигурация тестового запуска (версия приложения, окружение и т.д.).
     */
    private TestConfigurationDTO configuration;

    /**
     * Статический фабричный метод для создания {@code TestRunDetailDTO}
     * из сущности {@link TestRun}.
     * Преобразует связанные сущности {@link AnalysisResultDTO} и {@link TestConfigurationDTO}
     * в соответствующие DTO.
     *
     * @param testRun Сущность {@link TestRun}, из которой создается DTO.
     * @return Новый экземпляр {@link TestRunDetailDTO}.
     */
    public static TestRunDetailDTO fromEntity(TestRun testRun) {
        TestRunDetailDTO dto = new TestRunDetailDTO();
        dto.setId(testRun.getId());
        dto.setTestClass(testRun.getTestClass());
        dto.setTestMethod(testRun.getTestMethod());
        dto.setTimestamp(testRun.getTimestamp());
        dto.setStatus(testRun.getStatus().name()); // Преобразуем Enum в String
        dto.setExceptionType(testRun.getExceptionType());
        dto.setStackTrace(testRun.getStackTrace());
        dto.setFailedStep(testRun.getFailedStep());
        dto.setExecutionPath(testRun.getExecutionPath());
        if (testRun.getAnalysisResults() != null) {
            dto.setAnalysisResults(testRun.getAnalysisResults().stream()
                    .map(AnalysisResultDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        if (testRun.getConfiguration() != null) {
            dto.setConfiguration(TestConfigurationDTO.fromEntity(testRun.getConfiguration()));
        }
        return dto;
    }
}
