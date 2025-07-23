package com.svedentsov.xaiobserverapp.service;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;

/**
 * Интерфейс для различных стратегий анализа сбоев тестовых запусков.
 * Каждая реализация этого интерфейса будет отвечать за определение и анализ конкретного типа сбоя.
 */
public interface FailureAnalyzer {

    /**
     * Определяет, может ли данный анализатор обработать предоставленное событие сбоя.
     *
     * @param event DTO, содержащий информацию о событии сбоя.
     * @return true, если анализатор может обработать событие, иначе false.
     */
    boolean canAnalyze(FailureEventDTO event);

    /**
     * Выполняет анализ события сбоя и возвращает результат {@link AnalysisResult}.
     * Этот метод должен быть вызван только после проверки методом {@code canAnalyze}.
     *
     * @param event DTO, содержащий информацию о событии сбоя.
     * @return Объект {@link AnalysisResult}, представляющий результат анализа.
     */
    AnalysisResult analyze(FailureEventDTO event);
}
