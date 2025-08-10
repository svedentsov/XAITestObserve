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
 * Стратегия анализа, специализирующаяся на {@link AssertionError}.
 * Этот анализатор срабатывает, когда тест падает из-за невыполненного утверждения (assert),
 * что обычно указывает на функциональный баг в приложении или некорректные ожидания в тесте.
 */
@Component
@Order(20)
public class AssertionErrorAnalyzer implements AnalysisStrategy {

    private static final String EXCEPTION_NAME = "AssertionError";

    @Override
    public Optional<AnalysisResult> analyze(FailureEventDTO event) {
        String exceptionType = event.exceptionType();
        if (StringUtils.hasText(exceptionType) && exceptionType.contains(EXCEPTION_NAME)) {
            AnalysisResult ar = new AnalysisResult();
            ar.setAnalysisType("Анализ по типу исключения (AssertionError)");
            ar.setAiConfidence(0.80);
            ar.setSuggestedReason("Сработало утверждение (assertion), что означает несоответствие фактического результата ожидаемому. Это указывает на баг в приложении или ошибку в логике самого теста.");
            ar.setSolution("Проанализируйте, какое именно утверждение не выполнилось. Сравните фактическое и ожидаемое значения. Это может быть как реальный дефект, так и неверно заданные ожидания в тесте.");
            ar.setExplanationData(Map.of("evidence", "Exception Type: " + event.exceptionType()));
            return Optional.of(ar);
        }
        return Optional.empty();
    }
}
