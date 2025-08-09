package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.AnalysisStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Стратегия анализа, основанная на данных о шаге, на котором произошел сбой.
 * Эта стратегия используется, когда в событии о сбое присутствует информация о {@code failedStep},
 * часто предоставляемая AI-агентом. Анализ основывается на действии, локаторе и уверенности AI на этом шаге.
 */
@Component
@Order(50) // Более низкий приоритет, так как это общий анализ шага
public class FailedStepAnalyzer implements AnalysisStrategy {

    @Override
    public Optional<AnalysisResult> analyze(FailureEventDTO event) {
        if (event.failedStep() != null) {
            AnalysisResult ar = new AnalysisResult();
            ar.setAnalysisType("Анализ шага сбоя");
            // Уверенность зависит от уверенности AI на шаге
            ar.setAiConfidence(event.failedStep().getConfidenceScore() * 0.9);
            String action = event.failedStep().getAction();
            String locator = String.format("%s='%s'", event.failedStep().getLocatorStrategy(), event.failedStep().getLocatorValue());
            ar.setSuggestedReason(String.format("Сбой на шаге '%s' при попытке взаимодействия с элементом (%s). Низкая уверенность AI (%.2f) в этом шаге могла стать причиной выбора неверного элемента или действия.", action, locator, event.failedStep().getConfidenceScore()));
            ar.setSolution(String.format("Проверьте, что локатор %s является корректным и стабильным. Убедитесь, что страница полностью загрузилась перед выполнением действия. Рассмотрите возможность улучшения AI-модели для более точного определения элементов.", locator));
            String rawDetails = String.format("Failed Step Details: Action='%s', Locator='%s', Confidence=%.2f", action, locator, event.failedStep().getConfidenceScore());
            ar.setExplanationData(Map.of("evidence", rawDetails));
            return Optional.of(ar);
        }
        return Optional.empty();
    }
}
