package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.stereotype.Component; // Используем @Component для обнаружения Spring

/**
 * Анализатор для сбоев, произошедших на конкретном шаге выполнения теста.
 * Формирует предложенную причину и решение на основе действия, локатора и уверенности AI в этом шаге.
 */
@Component
public class FailedStepAnalyzer implements FailureAnalyzer {

    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getFailedStep() != null;
    }

    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
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
}
