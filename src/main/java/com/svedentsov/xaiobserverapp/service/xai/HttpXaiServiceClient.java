package com.svedentsov.xaiobserverapp.service.xai;

import com.svedentsov.xaiobserverapp.dto.FailureEventDTO;
import com.svedentsov.xaiobserverapp.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Реализация клиента для взаимодействия с внешним XAI-сервисом по HTTP.
 * Использует современный {@link RestClient} для выполнения запросов.
 */
@Slf4j
@Service
public class HttpXaiServiceClient implements XaiServiceClient {

    private final RestClient restClient;
    private final String xaiServiceUrl;

    /**
     * Конструктор для создания клиента.
     *
     * @param restClientBuilder билдер для создания RestClient.
     * @param xaiServiceUrl     URL внешнего XAI-сервиса, берется из `application.properties`.
     *                          По умолчанию используется URL мок-контроллера.
     */
    public HttpXaiServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${xai.analysis.service.url:http://localhost:8080/mock/xai/predict}") String xaiServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(xaiServiceUrl)
                .defaultHeader("Accept", "application/json")
                .build();
        this.xaiServiceUrl = xaiServiceUrl;
    }

    /**
     * Отправляет POST-запрос с данными о сбое во внешний сервис и получает результат анализа.
     *
     * @param event DTO с информацией о сбое.
     * @return {@link Optional} с {@link AnalysisResult}, если сервис ответил успешно и результат валиден.
     * В случае ошибки или невалидного ответа возвращает пустой Optional.
     */
    @Override
    public Optional<AnalysisResult> getPrediction(FailureEventDTO event) {
        log.info("Calling external XAI service at URL: {}", xaiServiceUrl);
        try {
            AnalysisResult result = restClient.post()
                    .uri("") // Base URL is already set
                    .body(event)
                    .retrieve()
                    .body(AnalysisResult.class);

            if (result == null || result.getSuggestedReason() == null) {
                log.warn("XAI service returned a null or incomplete result.");
                return Optional.empty();
            }
            log.info("Received a valid prediction from XAI service.");
            return Optional.of(result);

        } catch (RestClientException e) {
            log.error("Error calling XAI service at {}: {}", xaiServiceUrl, e.getMessage());
            return Optional.empty();
        }
    }
}
