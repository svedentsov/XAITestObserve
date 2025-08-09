package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.AnalysisStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Стратегия анализа, специализирующаяся на {@code org.openqa.selenium.StaleElementReferenceException}.
 * Этот анализатор обрабатывает случаи, когда ссылка на ранее найденный элемент DOM стала недействительной,
 * как правило, из-за динамического обновления страницы (AJAX).
 */
@Component
@Order(10) // Высокий приоритет для этого типа исключения
public class StaleElementReferenceExceptionAnalyzer implements AnalysisStrategy {

    private static final String EXCEPTION_NAME = "StaleElementReferenceException";

    @Override
    public Optional<AnalysisResult> analyze(FailureEventDTO event) {
        String exceptionType = event.exceptionType();
        if (StringUtils.hasText(exceptionType) && exceptionType.contains(EXCEPTION_NAME)) {
            AnalysisResult ar = new AnalysisResult();
            ar.setAnalysisType("Анализ по типу исключения (StaleElementReferenceException)");
            ar.setAiConfidence(0.95);
            ar.setSuggestedReason("Элемент, с которым пытались взаимодействовать, устарел. Это происходит, когда DOM-структура страницы динамически изменяется (например, через AJAX), и ссылка на элемент становится недействительной.");
            ar.setSolution("Не сохраняйте WebElement в переменную для долгого использования. Вместо этого, находите элемент заново непосредственно перед каждым взаимодействием. Используйте паттерн Page Object Model для инкапсуляции логики поиска элементов.");
            ar.setExplanationData(Map.of("evidence", "Exception Type: " + event.exceptionType()));
            return Optional.of(ar);
        }
        return Optional.empty();
    }
}
