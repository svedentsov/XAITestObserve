package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Анализатор для сбоев, вызванных исключением {@code NoSuchElementException}.
 * <p>
 * Это одна из самых распространенных ошибок в UI-автоматизации, которая возникает,
 * когда WebDriver не может найти элемент на странице по указанному локатору.
 * Причиной может быть как неверный локатор, так и проблема синхронизации (элемент
 * еще не успел появиться на странице).
 */
@Component
@Order(12)
public class NoSuchElementExceptionAnalyzer implements FailureAnalyzer {

    /**
     * Проверяет, является ли тип исключения {@code NoSuchElementException}.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если тип исключения содержит "NoSuchElementException", иначе {@code false}.
     */
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("NoSuchElementException");
    }

    /**
     * Предоставляет анализ, указывающий на проблему с поиском элемента.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с рекомендациями по проверке локатора и добавлению ожиданий.
     */
    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.90); // Высокая уверенность, так как причина обычно кроется в локаторе или ожидании
        ar.setSuggestedReason("Элемент не был найден на странице. Это самая частая причина падений в UI-тестах. Вероятно, локатор устарел, или элемент не успел появиться на странице.");
        ar.setSolution("1. Проверьте правильность локатора. 2. Добавьте явное ожидание (WebDriverWait) перед взаимодействием с элементом. 3. Убедитесь, что тест не пытается найти элемент до того, как страница полностью загрузится.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
