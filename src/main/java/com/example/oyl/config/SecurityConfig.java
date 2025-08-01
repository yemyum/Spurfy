package com.example.oyl.config;

import com.example.oyl.jwt.JwtFilter;
import com.example.oyl.repository.UserRepository;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API용)
                .cors(cors -> {}) // CORS 활성화!
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login",
                                "/api/images/**",
                                "/api/users/check-email",
                                "/api/mypage/check-nickname",
                                "/api/spa-services/**"
                        ).permitAll() // 허용
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public Filter jwtFilter() {
        return new JwtFilter(userRepository); // 전달
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
