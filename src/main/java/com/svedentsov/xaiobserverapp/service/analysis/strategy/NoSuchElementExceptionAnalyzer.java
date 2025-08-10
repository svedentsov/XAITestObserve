package com.svedentsov.xaiobserverapp.service.analysis.strategy;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.AnalysisStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Стратегия анализа, специализирующаяся на {@code org.openqa.selenium.NoSuchElementException}.
 * Этот анализатор срабатывает, когда WebDriver не может найти элемент на странице,
 * что является одной из самых частых причин падения UI-тестов.
 */
@Component
@Order(15)
public class NoSuchElementExceptionAnalyzer implements AnalysisStrategy {

    private static final String EXCEPTION_NAME = "NoSuchElementException";

    @Override
    public Optional<AnalysisResult> analyze(FailureEventDTO event) {
        String exceptionType = event.exceptionType();
        if (StringUtils.hasText(exceptionType) && exceptionType.contains(EXCEPTION_NAME)) {
            AnalysisResult ar = new AnalysisResult();
            ar.setAnalysisType("Анализ по типу исключения (NoSuchElementException)");
            ar.setAiConfidence(0.90);
            ar.setSuggestedReason("Элемент не был найден на странице. Это самая частая причина падений в UI-тестах. Вероятно, локатор устарел, или элемент не успел появиться на странице.");
            ar.setSolution("1. Проверьте правильность локатора. 2. Добавьте явное ожидание (WebDriverWait) перед взаимодействием с элементом. 3. Убедитесь, что тест не пытается найти элемент до того, как страница полностью загрузится.");
            ar.setExplanationData(Map.of("evidence", "Exception Type: " + event.exceptionType()));
            return Optional.of(ar);
        }
        return Optional.empty();
    }
}
