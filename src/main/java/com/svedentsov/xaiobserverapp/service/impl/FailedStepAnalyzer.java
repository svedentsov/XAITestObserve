package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Анализатор, который фокусируется на данных о конкретном шаге, на котором произошел сбой.
 * <p>
 * Этот анализатор используется, когда тестовый фреймворк предоставляет подробную
 * информацию о проваленном шаге, включая действие, локатор и уровень уверенности AI.
 * Его приоритет ниже, чем у анализаторов конкретных исключений, так как информация
 * об исключении часто бывает более точной.
 */
@Component
@Order(20)
public class FailedStepAnalyzer implements FailureAnalyzer {

    /**
     * Проверяет, содержит ли событие информацию о проваленном шаге.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если поле {@code failedStep} не равно {@code null}, иначе {@code false}.
     */
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getFailedStep() != null;
    }

    /**
     * Анализирует данные из проваленного шага для формирования гипотезы о причине сбоя.
     * <p>
     * Учитывает действие, локатор и уверенность AI, чтобы предположить, почему шаг
     * мог провалиться.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с анализом на основе данных шага.
     */
    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ шага сбоя");
        ar.setAiConfidence(event.getFailedStep().getConfidenceScore() * 0.9);
        String action = event.getFailedStep().getAction();
        String locator = String.format("%s='%s'", event.getFailedStep().getLocatorStrategy(), event.getFailedStep().getLocatorValue());
        ar.setSuggestedReason(String.format("Сбой на шаге '%s' при попытке взаимодействия с элементом (%s). Низкая уверенность AI (%.2f) в этом шаге могла стать причиной выбора неверного элемента или действия.", action, locator, event.getFailedStep().getConfidenceScore()));
        ar.setSolution(String.format("Проверьте, что локатор %s является корректным и стабильным. Убедитесь, что страница полностью загрузилась перед выполнением действия. Рассмотрите возможность улучшения AI-модели для более точного определения элементов.", locator));
        ar.setRawData(String.format("Failed Step Details: Action='%s', Locator='%s', Confidence=%.2f", action, locator, event.getFailedStep().getConfidenceScore()));
        return ar;
    }
}
