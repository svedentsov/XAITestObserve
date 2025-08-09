package com.svedentsov.xaiobserverapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security для приложения.
 * Определяет правила доступа к URL, конфигурацию формы входа,
 * обработку CSRF и настройки пользователей в памяти.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Публичные ресурсы, доступные всем
    private static final String[] PUBLIC_RESOURCES = {"/", // Главная страница дашборда
            "/css/**", "/js/**", "/webjars/**", "/favicon.ico", "/error"};

    // Эндпоинты, доступные для анонимных пользователей (включая API для дашборда и WebSocket)
    private static final String[] PUBLIC_ENDPOINTS = {"/login", "/h2-console/**", "/api/v1/**", // Разрешаем доступ ко всему API v1
            "/demo/create", "/mock/xai/predict", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/ws/**"};

    // Эндпоинты, для которых нужно отключить CSRF-защиту
    private static final String[] CSRF_EXCLUDED_ENDPOINTS = {"/api/**", "/demo/create", "/mock/xai/predict", "/h2-console/**", "/ws/**"};

    /**
     * Определяет основную цепочку фильтров безопасности.
     * Настраивает следующие аспекты:
     * <ul>
     *     <li>Разрешает анонимный доступ к статическим ресурсам, API, Swagger и H2-консоли.</li>
     *     <li>Требует аутентификацию для всех остальных запросов.</li>
     *     <li>Настраивает страницу входа (@"/login") и перенаправление после успешного входа.</li>
     *     <li>Настраивает выход из системы.</li>
     *     <li>Отключает CSRF-защиту для API и H2-консоли для упрощения взаимодействия.</li>
     *     <li>Разрешает отображение H2-консоли во фреймах.</li>
     * </ul>
     *
     * @param http объект для конфигурации безопасности HTTP.
     * @return сконфигурированная цепочка фильтров безопасности.
     * @throws Exception если при конфигурации возникает ошибка.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        // Разрешаем доступ к публичным ресурсам и эндпоинтам
                        .requestMatchers(PUBLIC_RESOURCES).permitAll().requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()).formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll()).logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll()).csrf(csrf -> csrf.ignoringRequestMatchers(CSRF_EXCLUDED_ENDPOINTS))
                // Разрешаем H2-консоли отображаться во фрейме
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /**
     * Создает бин {@link PasswordEncoder} для кодирования паролей.
     * Используется BCrypt - надежный адаптивный алгоритм хэширования.
     *
     * @return кодировщик паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает сервис для управления пользователями, хранящимися в памяти (In-Memory).
     * Создает двух пользователей по умолчанию:
     * <ul>
     *     <li><b>user</b> с паролем <b>password</b> и ролью USER.</li>
     *     <li><b>admin</b> с паролем <b>admin</b> и ролями ADMIN, USER.</li>
     * </ul>
     * Пароли хранятся в закодированном виде.
     *
     * @param passwordEncoder кодировщик для шифрования паролей.
     * @return менеджер пользователей.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder().username("user").password(passwordEncoder.encode("password")).roles("USER").build();
        UserDetails admin = User.builder().username("admin").password(passwordEncoder.encode("admin")).roles("ADMIN", "USER").build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
