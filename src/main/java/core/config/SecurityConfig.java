package core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Публичные эндпоинты
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/users/name/**").permitAll()

                        // Эндпоинты для администраторов
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/functions/**").hasRole("ADMIN")
                        .requestMatchers("/api/tabulated-points/**").hasRole("ADMIN")
                        .requestMatchers("/api/operations/**").hasRole("ADMIN")
                        .requestMatchers("/api/integration/**").hasRole("ADMIN")
                        .requestMatchers("/api/differentiation/**").hasRole("ADMIN")
                        .requestMatchers("/api/composite-functions").hasRole("ADMIN")
                        .requestMatchers("/api/admin/search/**").hasRole("ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}) // Включаем Basic Auth
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Игнорируем CSRF для API
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}