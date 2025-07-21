package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для проведения автоматического корневого анализа причин (RCA)
 * сбоев тестовых запусков.
 * Анализирует предоставленные данные о событии сбоя (шаг, исключение)
 * и предлагает потенциальные причины и решения.
 * В текущей реализации использует простую логику на основе типа исключения
 * и информации о сбойном шаге.
 */
@Service
public class RcaService {
    /**
     * Выполняет анализ тестового запуска и генерирует список {@link AnalysisResult}.
     * Если тест пройден, создается сводка успешного запуска.
     * Если тест провален, анализирует информацию о сбойном шаге и типе исключения
     * для определения возможных причин и предложений решений.
     *
     * @param event DTO, содержащий информацию о событии завершения теста.
     * @return Список объектов {@link AnalysisResult}, представляющих результаты анализа.
     */
    public List<AnalysisResult> analyzeTestRun(FailureEventDTO event) {
        List<AnalysisResult> results = new ArrayList<>();
        if ("PASSED".equals(event.getStatus())) {
            results.add(createSuccessfulRunSummary());
            return results;
        }
        if (event.getFailedStep() != null) {
            results.add(analyzeFailedStep(event));
        }
        if (event.getExceptionType() != null && !event.getExceptionType().isEmpty()) {
            results.add(analyzeException(event));
        }
        // Если никаких конкретных причин не найдено, но статус FAILED, создаем общую сводку.
        if (results.isEmpty() && "FAILED".equals(event.getStatus())) {
            results.add(createGeneralFailureSummary(event));
        }
        return results;
    }

    /**
     * Создает сводный результат анализа для успешно пройденного тестового запуска.
     *
     * @return Объект {@link AnalysisResult} с информацией об успешном завершении.
     */
    private AnalysisResult createSuccessfulRunSummary() {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Резюме успешного запуска");
        ar.setAiConfidence(0.99);
        ar.setSuggestedReason("Тест успешно завершен. Все шаги выполнены корректно.");
        ar.setSolution("Никаких действий не требуется.");
        ar.setRawData("Статус: PASSED");
        return ar;
    }

    /**
     * Анализирует информацию о сбойном шаге тестового запуска.
     * Формирует предложенную причину и решение на основе действия, локатора
     * и уверенности AI в этом шаге.
     *
     * @param event DTO, содержащий информацию о событии сбоя, включая сбойный шаг.
     * @return Объект {@link AnalysisResult} с анализом сбойного шага.
     */
    private AnalysisResult analyzeFailedStep(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ шага сбоя");
        ar.setAiConfidence(event.getFailedStep().getConfidenceScore() * 0.9); // Уверенность зависит от уверенности шага

        String action = event.getFailedStep().getAction();
        String locator = String.format("%s='%s'", event.getFailedStep().getLocatorStrategy(), event.getFailedStep().getLocatorValue());

        ar.setSuggestedReason(String.format("Сбой на шаге '%s' при попытке взаимодействия с элементом (%s). Низкая уверенность AI (%.2f) в этом шаге могла стать причиной выбора неверного элемента или действия.", action, locator, event.getFailedStep().getConfidenceScore()));
        ar.setSolution(String.format("Проверьте, что локатор %s является корректным и стабильным. Убедитесь, что страница полностью загрузилась перед выполнением действия. Рассмотрите возможность улучшения AI-модели для более точного определения элементов.", locator));
        ar.setRawData(String.format("Failed Step Details: Action='%s', Locator='%s', Confidence=%.2f", action, locator, event.getFailedStep().getConfidenceScore()));
        return ar;
    }

    /**
     * Анализирует тип исключения, вызвавшего сбой теста.
     * Предоставляет специфичные причины и решения для часто встречающихся типов исключений
     * (например, {@code NoSuchElementException}, {@code TimeoutException}).
     *
     * @param event DTO, содержащий информацию о событии сбоя, включая тип исключения.
     * @return Объект {@link AnalysisResult} с анализом по типу исключения.
     */
    private AnalysisResult analyzeException(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        String exceptionType = event.getExceptionType();

        if (exceptionType.contains("NoSuchElementException")) {
            ar.setAiConfidence(0.90);
            ar.setSuggestedReason("Элемент не был найден на странице. Это самая частая причина падений в UI-тестах. Вероятно, локатор устарел, или элемент не успел появиться на странице.");
            ar.setSolution("1. Проверьте правильность локатора. 2. Добавьте явное ожидание (WebDriverWait) перед взаимодействием с элементом. 3. Убедитесь, что тест не пытается найти элемент до того, как страница полностью загрузится.");
        } else if (exceptionType.contains("TimeoutException")) {
            ar.setAiConfidence(0.85);
            ar.setSuggestedReason("Операция не была завершена за отведенное время. Это может быть связано с медленной загрузкой страницы, медленным ответом от бэкенда или слишком коротким таймаутом в тесте.");
            ar.setSolution("1. Увеличьте время ожидания (timeout) в тесте. 2. Проверьте производительность приложения и сетевые задержки. 3. Оптимизируйте условия ожидания, чтобы они были более гибкими.");
        } else if (exceptionType.contains("StaleElementReferenceException")) {
            ar.setAiConfidence(0.95);
            ar.setSuggestedReason("Элемент, с которым пытались взаимодействовать, устарел. Это происходит, когда DOM-структура страницы динамически изменяется (например, через AJAX), и ссылка на элемент становится недействительной.");
            ar.setSolution("Не сохраняйте WebElement в переменную для долгого использования. Вместо этого, находите элемент заново непосредственно перед каждым взаимодействием. Используйте паттерн Page Object Model для инкапсуляции логики поиска элементов.");
        } else if (exceptionType.contains("AssertionError")) {
            ar.setAiConfidence(0.80);
            ar.setSuggestedReason("Сработало утверждение (assertion), что означает несоответствие фактического результата ожидаемому. Это указывает на баг в приложении или ошибку в логике самого теста.");
            ar.setSolution("Проанализируйте, какое именно утверждение не выполнилось. Сравните фактическое и ожидаемое значения. Это может быть как реальный дефект, так и неверно заданные ожидания в тесте.");
        } else {
            ar.setAiConfidence(0.50);
            ar.setSuggestedReason("Произошло необработанное исключение: " + exceptionType);
            ar.setSolution("Это исключение не относится к наиболее частым. Проанализируйте полный стек-трейс для определения точной причины. Проверьте логи приложения на сервере на момент выполнения теста.");
        }
        ar.setRawData("Exception Type: " + exceptionType + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }

    /**
     * Создает общий сводный результат анализа для проваленного тестового запуска,
     * когда более специфичные причины не могут быть определены.
     *
     * @param event DTO, содержащий информацию о событии сбоя.
     * @return Объект {@link AnalysisResult} с общим анализом сбоя.
     */
    private AnalysisResult createGeneralFailureSummary(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Общий анализ сбоя");
        ar.setAiConfidence(0.40);
        ar.setSuggestedReason("Тест завершился со статусом FAILED, но не удалось определить конкретную причину на основе предоставленных данных (шаг сбоя или тип исключения).");
        ar.setSolution("Проверьте логи выполнения теста, скриншоты (если они есть) и состояние окружения. Возможно, проблема связана с инфраструктурой или внешними сервисами.");
        ar.setRawData("Статус: " + event.getStatus());
        return ar;
    }
}
