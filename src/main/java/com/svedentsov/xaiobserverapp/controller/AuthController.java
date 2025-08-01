package com.svedentsov.xaiobserverapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /**
     * Отображает кастомную страницу входа.
     *
     * @return Имя шаблона "login".
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // Это имя нашего будущего HTML-файла в /templates
    }
}
