package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(13)
public class TimeoutExceptionAnalyzer implements FailureAnalyzer {
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("TimeoutException");
    }

    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.85);
        ar.setSuggestedReason("Операция не была завершена за отведенное время. Это может быть связано с медленной загрузкой страницы, медленным ответом от бэкенда или слишком коротким таймаутом в тесте.");
        ar.setSolution("1. Увеличьте время ожидания (timeout) в тесте. 2. Проверьте производительность приложения и сетевые задержки. 3. Оптимизируйте условия ожидания, чтобы они были более гибкими.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
