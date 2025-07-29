package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;

/**
 * Интерфейс для анализаторов причин сбоев тестов.
 * <p>
 * Каждая реализация отвечает за анализ конкретного типа сбоя.
 * Анализаторы образуют цепочку ответственности (Chain of Responsibility).
 */
public interface FailureAnalyzer {

    /**
     * Проверяет, может ли данный анализатор обработать указанное событие сбоя.
     *
     * @param event DTO события сбоя.
     * @return {@code true}, если анализатор может обработать событие, иначе {@code false}.
     */
    boolean canAnalyze(FailureEventDTO event);

    /**
     * Выполняет анализ события и возвращает результат.
     *
     * @param event DTO события сбоя.
     * @return {@link AnalysisResult} с предположением о причине и решении.
     */
    AnalysisResult analyze(FailureEventDTO event);
}
