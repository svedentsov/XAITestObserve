package com.svedentsov.xaiobserverapp.service.impl;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import com.svedentsov.xaiobserverapp.service.FailureAnalyzer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class StaleElementReferenceExceptionAnalyzer implements FailureAnalyzer {
    @Override
    public boolean canAnalyze(FailureEventDTO event) {
        return event.getExceptionType() != null && event.getExceptionType().contains("StaleElementReferenceException");
    }

    @Override
    public AnalysisResult analyze(FailureEventDTO event) {
        AnalysisResult ar = new AnalysisResult();
        ar.setAnalysisType("Анализ по типу исключения");
        ar.setAiConfidence(0.95);
        ar.setSuggestedReason("Элемент, с которым пытались взаимодействовать, устарел. Это происходит, когда DOM-структура страницы динамически изменяется (например, через AJAX), и ссылка на элемент становится недействительной.");
        ar.setSolution("Не сохраняйте WebElement в переменную для долгого использования. Вместо этого, находите элемент заново непосредственно перед каждым взаимодействием. Используйте паттерн Page Object Model для инкапсуляции логики поиска элементов.");
        ar.setRawData("Exception Type: " + event.getExceptionType() + "\nStack Trace:\n" + event.getStackTrace());
        return ar;
    }
}
