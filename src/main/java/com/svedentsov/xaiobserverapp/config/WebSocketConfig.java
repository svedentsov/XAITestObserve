package com.svedentsov.xaiobserverapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация для WebSocket с использованием STOMP поверх SockJS.
 * Включает и настраивает брокер сообщений для обмена данными в реальном времени
 * между сервером и клиентами (например, для уведомления о новых тестовых запусках).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Регистрирует эндпоинт "/ws", который клиенты будут использовать для подключения к WebSocket-серверу.
     * {@code withSockJS()} обеспечивает фолбэк для браузеров, не поддерживающих WebSocket нативно.
     *
     * @param registry реестр для регистрации эндпоинтов.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS(); // Эндпоинт для подключения
    }

    /**
     * Конфигурирует брокер сообщений.
     * <ul>
     *     <li>{@code enableSimpleBroker("/topic")} включает простой брокер в памяти для отправки сообщений клиентам
     *     по адресам, начинающимся с "/topic". Клиенты подписываются на эти топики.</li>
     *     <li>{@code setApplicationDestinationPrefixes("/app")} определяет префикс для сообщений,
     *     которые отправляются от клиента к серверу (например, для вызова методов, аннотированных @MessageMapping).</li>
     * </ul>
     *
     * @param registry реестр для конфигурации брокера.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Топики, на которые подписывается клиент
        registry.setApplicationDestinationPrefixes("/app"); // Префикс для сообщений от клиента к серверу
    }
}
