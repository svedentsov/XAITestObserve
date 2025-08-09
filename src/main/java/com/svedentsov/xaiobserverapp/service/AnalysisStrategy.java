package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;

import java.util.Optional;

/**
 * Интерфейс для стратегий анализа причин сбоев тестов.
 * Каждая реализация отвечает за анализ конкретного типа сбоя или сценария.
 */
public interface AnalysisStrategy {
    /**
     * Выполняет анализ события и возвращает результат, если стратегия применима.
     *
     * @param event DTO события сбоя.
     * @return {@link Optional} с {@link AnalysisResult}, если анализ был успешно проведен, иначе пустой Optional.
     */
    Optional<AnalysisResult> analyze(FailureEventDTO event);
}
