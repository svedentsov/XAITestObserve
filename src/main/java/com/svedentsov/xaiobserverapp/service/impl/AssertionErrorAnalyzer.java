package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.stereotype.Component;

/**
 * Анализатор для исключения {@code AssertionError}.
 */
@Component
public class AssertionErrorAnalyzer implements FailureAnalyzer {

    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("AssertionError");
    }

    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.80);
        ar.setSuggestedReason("Сработало утверждение (assertion), что означает несоответствие фактического результата ожидаемому. Это указывает на баг в приложении или ошибку в логике самого теста.");
        ar.setSolution("Проанализируйте, какое именно утверждение не выполнилось. Сравните фактическое и ожидаемое значения. Это может быть как реальный дефект, так и неверно заданные ожидания в тесте.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
