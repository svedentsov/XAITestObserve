package com.svedentsov.xaiobserverapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки запросов, связанных с аутентификацией.
 */
@Controller
public class AuthController {
    /**
     * Отображает страницу входа в систему.
     *
     * @return имя Thymeleaf-шаблона "login".
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
