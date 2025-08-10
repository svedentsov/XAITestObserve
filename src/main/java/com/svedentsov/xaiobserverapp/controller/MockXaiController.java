package com.svedentsov.xaiobserverapp.controller;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Контроллер-заглушка (mock), имитирующий работу внешнего XAI-сервиса.
 * Используется для локальной разработки и тестирования интеграции с ML-моделью,
 * когда реальный сервис недоступен. Принимает данные о сбое и возвращает
 * предопределенный фиктивный результат анализа.
 */
@RestController
@Tag(name = "XAI Service Mock", description = "Имитация внешнего Python XAI сервиса для тестирования интеграции")
public class MockXaiController {

    @Operation(summary = "Имитация предиктивного анализа",
            description = "Принимает данные о сбое и возвращает фиктивный результат анализа, как это делал бы реальный ML-сервис. Возвращает структурированные данные в поле `explanationData`.")
    @PostMapping("/mock/xai/predict")
    public AnalysisResult mockXaiPrediction(@RequestBody FailureEventDTO event) {
        var result = new AnalysisResult();
        result.setAnalysisType("ML-based XAI Analysis (Mock)");
        result.setAiConfidence(0.78);
        result.setAnalysisTimestamp(LocalDateTime.now());

        Map<String, Object> explanationData = new LinkedHashMap<>();

        if (event.exceptionType() != null && event.exceptionType().contains("Timeout")) {
            result.setSuggestedReason("Модель предсказывает, что таймаут связан с медленным ответом от бэкенда или долгой загрузкой элемента.");
            result.setSolution("Проверьте API-вызовы, которые происходят на этом шаге. Возможно, стоит увеличить таймауты на стороне клиента или использовать более надежные условия ожидания.");
            explanationData.put("type", "LIME");
            explanationData.put("prediction", "Backend Slowdown");
            Map<String, Double> featureImportance = Map.of(
                    "exceptionType_TimeoutException", 0.6,
                    "durationMillis_>_30000", 0.2,
                    "failedStep_action_wait", 0.15,
                    "environment_qa", 0.05
            );
            explanationData.put("feature_importances", featureImportance);
        } else {
            result.setSuggestedReason("Общая ML-гипотеза: сбой вызван нестабильностью тестового окружения или неактуальным локатором.");
            result.setSolution("Перезапустите тест в стабильном окружении. Проверьте локатор на последней версии приложения. Проверьте логи инфраструктуры.");
            explanationData.put("type", "SHAP");
            explanationData.put("prediction", "Environment/Locator Issue");
            Map<String, Double> featureImportance = Map.of(
                    "environment_qa", 0.5,
                    "testTags_contains_flaky", 0.3,
                    "appVersion_changed", 0.1,
                    "exceptionType_NoSuchElementException", 0.1
            );
            explanationData.put("feature_importances", featureImportance);
        }
        result.setExplanationData(explanationData);
        return result;
    }
}
