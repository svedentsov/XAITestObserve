package com.svedentsov.xaiobserverapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/events/**", "/demo/create").permitAll() // API для приема событий
                        .requestMatchers("/css/**", "/js/**", "/webjars/**").permitAll() // Статические ресурсы
                        .requestMatchers("/login").permitAll() // Страница логина
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Наша кастомная страница
                        .defaultSuccessUrl("/", true) // Перенаправлять на главную после успеха
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // Перенаправлять на страницу логина с сообщением
                        .permitAll()
                )
                // ВАЖНО: для API, принимающего POST-запросы, нужно отключить CSRF или настроить его
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/events/**", "/demo/create"));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Для начала используем In-Memory пользователей. В будущем заменить на JDBC/LDAP.
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                    .username("admin")
                .password("admin")
                .roles("ADMIN", "USER")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}