package com.svedentsov.xaiobserverapp.service.xai;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import java.util.Optional;

/**
 * Абстракция для взаимодействия с внешним XAI-сервисом (например, Python-сервисом).
 * Определяет контракт для получения предиктивного анализа.
 */
public interface XaiServiceClient {
    /**
     * Отправляет данные о сбое во внешний сервис и получает предиктивный анализ.
     *
     * @param event DTO с информацией о сбое.
     * @return Optional с результатом анализа от ML-модели.
     */
    Optional<AnalysisResult> getPrediction(FailureEventDTO event);
}
