package com.svedentsov.xaiobserverapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Конфигурация безопасности Spring Security для приложения.
 * <p>
 * Определяет правила доступа к URL, конфигурацию формы входа,
 * обработку CSRF и настройки пользователей в памяти.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Определяет основную цепочку фильтров безопасности.
     *
     * @param http объект {@link HttpSecurity} для конфигурации.
     * @return сконфигурированная цепочка фильтров.
     * @throws Exception если при конфигурации возникает ошибка.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 1. Разрешаем доступ к статическим ресурсам и странице входа для ВСЕХ
                .requestMatchers(
                    "/css/**", "/js/**", "/webjars/**", "/favicon.ico",
                    "/login", "/error", "/h2-console/**"
                ).permitAll()
                // 2. Разрешаем доступ к эндпоинтам для ВНЕШНИХ СИСТЕМ (CI/CD, тестовые агенты)
                // Они должны быть публичными, т.к. аутентификация для них может быть по токену, а не по сессии
                .requestMatchers(
                    "/api/v1/events/test-finished", // Эндпоинт для приема результатов тестов
                    "/demo/**", 
                    "/mock/xai/**",
                    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                    "/ws/**"
                ).permitAll()
                // 3. ВСЕ ОСТАЛЬНЫЕ запросы (включая '/', и все остальные /api/v1/**) ТРЕБУЮТ АУТЕНТИФИКАЦИИ
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll() // Важно, чтобы сама страница входа была доступна
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            )
            .csrf(csrf -> csrf
                // Отключаем CSRF-защиту для эндпоинтов, принимающих POST/DELETE от внешних систем или JS
                .ignoringRequestMatchers(
                    "/api/**", 
                    "/demo/**", 
                    "/mock/xai/**", 
                    "/h2-console/**", 
                    "/ws/**"
                )
            )
            .headers(headers -> headers
                // Разрешаем отображение H2 консоли во фреймах
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

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
     *
     * @param passwordEncoder кодировщик для шифрования паролей.
     * @return менеджер пользователей.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var user = User.withUsername("user")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();
        var admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN", "USER")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
